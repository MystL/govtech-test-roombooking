package com.vin.booking.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.vin.booking.R
import com.vin.booking.activities.MainActivity
import com.vin.booking.activities.WebViewActivity
import com.vin.booking.activities.WebViewActivity.Companion.WEB_VIEW_URL
import com.vin.booking.adapters.RoomsDisplayAdapter
import com.vin.booking.di.InjectableFragment
import com.vin.booking.models.toRoomItemDisplay
import com.vin.booking.ui.CustomTimePickerDialog
import com.vin.booking.ui.VerticalSpaceItemDecoration
import com.vin.booking.viewmodels.RoomBookingViewModel
import com.vin.booking.viewmodels.SortRules
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.layout_room_booking.dateGroup
import kotlinx.android.synthetic.main.layout_room_booking.progressBarRoomsList
import kotlinx.android.synthetic.main.layout_room_booking.recyclerViewRoomsList
import kotlinx.android.synthetic.main.layout_room_booking.textViewDateDisplay
import kotlinx.android.synthetic.main.layout_room_booking.textViewSort
import kotlinx.android.synthetic.main.layout_room_booking.textViewTimeDisplay
import kotlinx.android.synthetic.main.layout_room_booking.timeGroup
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class MainFragment : Fragment(), HasAndroidInjector, InjectableFragment {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModel: RoomBookingViewModel

    private val mainActivity: MainActivity? get() = activity as? MainActivity

    private val roomsDisplayAdapter = RoomsDisplayAdapter()
    private val calendar = Calendar.getInstance(Locale.US)
    private var datePickerListener: DatePickerDialog.OnDateSetListener? = null
    private var timePickerListener: TimePickerDialog.OnTimeSetListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.layout_room_booking, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity?.setTitle(getString(R.string.room_booking_title))
        setHasOptionsMenu(true)

        initSelectionView()

        viewModel.rooms.observe(this, Observer {
            it?.takeIf { it.isNotEmpty() }?.let { rooms ->
                val currentTime = viewModel.currentTimeDisplay.value ?: ""
                val filteredRooms = viewModel.getRoomsForTimeSlot(currentTime)
                val mapped = filteredRooms.map { r -> r.toRoomItemDisplay(currentTime) }
                // We will cache this to cater for the reset of sort
                viewModel.setUnsortedDisplayDataSet(mapped)
                recyclerViewRoomsList.visibility = View.VISIBLE
                toggleProgressBar(false)
                roomsDisplayAdapter.setData(mapped)
            }
        })

        viewModel.fetchRoomsError.observe(this, Observer {
            recyclerViewRoomsList.visibility = View.GONE
            showErrorDialog(
                it,
                getString(R.string.lbl_error_msg),
                getString(R.string.lbl_retry_btn),
                getString(R.string.lbl_cancel_btn),
                DialogInterface.OnClickListener { dialog, id ->
                    // If error, allows to re-fetching the rooms
                    viewModel.fetchRooms()
                    toggleProgressBar(true)
                })
        })

        viewModel.sortedDisplayRooms.observe(this, Observer {
            it?.takeIf { it.isNotEmpty() }?.let { sortedDisplay ->
                roomsDisplayAdapter.setData(sortedDisplay)
            } ?: run {
                roomsDisplayAdapter.setData(listOf())
                showErrorDialog(
                    getString(R.string.lbl_no_result),
                    getString(R.string.lbl_no_result_msg),
                    getString(R.string.lbl_ok_btn),
                    null,
                    null
                )
            }
        })

        viewModel.currentDateDisplay.observe(this, Observer {
            textViewDateDisplay.text = viewModel.getFormattedDateDisplay()
        })

        viewModel.currentTimeDisplay.observe(this, Observer {
            textViewTimeDisplay.text = viewModel.getFormattedTimeDisplay()
            viewModel.getRoomsForTimeSlot(viewModel.currentTimeDisplay.value ?: "")
        })

        datePickerListener = DatePickerDialog.OnDateSetListener { _, y, m, d ->
            calendar.set(y, m, d)
            viewModel.setCurrentDate(calendar)
        }

        timePickerListener = TimePickerDialog.OnTimeSetListener { _, hour, min ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, min)
            viewModel.setCurrentTime(calendar)
        }

        dateGroup.setOnClickListener {
            showDatePicker()
        }

        timeGroup.setOnClickListener {
            showTimePicker()
        }

        textViewSort.setOnClickListener {
            showSortFragment()
        }

        context?.let { ctx ->
            recyclerViewRoomsList.apply {
                layoutManager = LinearLayoutManager(ctx)
                adapter = roomsDisplayAdapter
                addItemDecoration(VerticalSpaceItemDecoration(VERTICAL_SPACING))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Ensure that the Data is re-fetched when this screen is returned
        viewModel.fetchRooms()
        toggleProgressBar(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.booking_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_scan) {
            mainActivity?.navigateToFragment(ScanQRFragment.newInstance().apply {
                listener = object : ScanQRFragment.ScannerResultListener {
                    override fun onResult(result: String) {
                        navigateToWebView(result)
                    }
                }
            })
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SORT_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_CANCELED -> {
                    viewModel.currentSortRules = "" // Assuming reset is clear all sorting
                    viewModel.resetSort()
                }
                Activity.RESULT_OK -> {
                    processSortOption(data)
                }
                else -> {
                    // do nothing
                }
            }
        }
    }

    override fun androidInjector() = dispatchingAndroidInjector

    private fun processSortOption(data: Intent?) {
        data?.extras?.getString(SortRoomFragment.SELECTED_ACTION_BUTTON)?.let {
            viewModel.currentSortRules = it
            when (it) {
                getString(R.string.lbl_sort_available) -> {
                    viewModel.sortRoomsBy(SortRules.AVAILABILITY, roomsDisplayAdapter.dataSet)
                }
                getString(R.string.lbl_sort_capacity) -> {
                    viewModel.sortRoomsBy(SortRules.CAPACITY, roomsDisplayAdapter.dataSet)
                }
                getString(R.string.lbl_sort_location) -> {
                    viewModel.sortRoomsBy(SortRules.LOCATION, roomsDisplayAdapter.dataSet)
                }
                else -> {
                    // do nothing
                }
            }
        }
    }

    private fun showErrorDialog(
        title: String,
        msg: String,
        positiveBtn: String?,
        negativeBtn: String?,
        listener: DialogInterface.OnClickListener?
    ) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(msg)
        if (positiveBtn != null) {
            dialog.setPositiveButton(
                getString(R.string.lbl_retry_btn),
                listener
            )
        }

        if (negativeBtn != null) {
            dialog.setNegativeButton(getString(R.string.lbl_cancel_btn), null)
        }
        dialog.show()
    }

    private fun navigateToWebView(url: String) {
        context?.startActivity(
            Intent().apply {
                setClass(requireContext(), WebViewActivity::class.java)
                putExtra(WEB_VIEW_URL, url)
            }
        )
    }

    private fun initSelectionView() {
        val currentTime = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMin = calendar.get(Calendar.MINUTE)
        if (calendar.get(Calendar.HOUR_OF_DAY) > 19
            && calendar.get(Calendar.MINUTE) > 30
        ) {
            Log.d("XXX" , "Over timing liao")
            calendar.set(Calendar.HOUR_OF_DAY, 19)
            calendar.set(Calendar.MINUTE, 30)
            viewModel.setCurrentDate(calendar)
            viewModel.setCurrentTime(calendar)
        }
    }

    private fun showDatePicker() {
        val datePickerDialog =
            DatePickerDialog(
                requireContext(),
                datePickerListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val timePickerDialog = CustomTimePickerDialog(
            requireContext(),
            timePickerListener,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun showSortFragment() {
        activity?.let { fa ->
            SortRoomFragment.newInstance(viewModel.currentSortRules).apply {
                setTargetFragment(this@MainFragment, SORT_REQUEST_CODE)
                show(fa.supportFragmentManager, SortRoomFragment.TAG)
            }
        }
    }

    private fun toggleProgressBar(show: Boolean) {
        if (show) {
            progressBarRoomsList.visibility = View.VISIBLE
            recyclerViewRoomsList.visibility = View.GONE
        } else {
            progressBarRoomsList.visibility = View.GONE
            recyclerViewRoomsList.visibility = View.VISIBLE
        }
    }

    companion object {
        const val SORT_REQUEST_CODE = 100

        private const val VERTICAL_SPACING = 12 * 2

        fun newInstance() = MainFragment()
    }
}