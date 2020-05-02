package com.vin.booking.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.vin.booking.R
import com.vin.booking.activities.MainActivity
import com.vin.booking.adapters.RoomsDisplayAdapter
import com.vin.booking.models.toRoomItemDisplay
import com.vin.booking.ui.VerticalSpaceItemDecoration
import com.vin.booking.viewmodels.RoomBookingViewModel
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.layout_room_booking.dateGroup
import kotlinx.android.synthetic.main.layout_room_booking.recyclerViewRoomsList
import kotlinx.android.synthetic.main.layout_room_booking.textViewSort
import kotlinx.android.synthetic.main.layout_room_booking.timeGroup
import javax.inject.Inject

class MainFragment : Fragment(), HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModel: RoomBookingViewModel

    private val mainActivity: MainActivity? get() = activity as? MainActivity

    private val roomsDisplayAdapter = RoomsDisplayAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.layout_room_booking, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity?.setTitle(getString(R.string.room_booking_title))

        viewModel.rooms.observe(this, Observer {
            // Handle UI Update here
            it?.takeIf { it.isNotEmpty() }?.let { rooms ->
                val mapped = rooms.map { r -> r.toRoomItemDisplay("10:30") }
                roomsDisplayAdapter.setData(mapped)
            } ?: run {
                // TODO - don't show the list
            }
        })

        dateGroup.setOnClickListener {
            val datePickerDialog = DatePickerDialog(requireContext())
            datePickerDialog.show()
        }

        timeGroup.setOnClickListener {
//            val timePickerDialog = TimePickerDialog(this)
        }

        textViewSort.setOnClickListener {
            // TODO - show Sort Selection
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
    }

    override fun androidInjector() = dispatchingAndroidInjector

    companion object {
        private const val VERTICAL_SPACING = 12 * 2

        fun newInstance() = MainFragment()
    }
}