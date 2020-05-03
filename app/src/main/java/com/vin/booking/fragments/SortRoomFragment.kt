package com.vin.booking.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vin.booking.R
import kotlinx.android.synthetic.main.layout_sort_room.applySortButton
import kotlinx.android.synthetic.main.layout_sort_room.resetButton
import kotlinx.android.synthetic.main.layout_sort_room.sortAvailability
import kotlinx.android.synthetic.main.layout_sort_room.sortCapacity
import kotlinx.android.synthetic.main.layout_sort_room.sortGroup
import kotlinx.android.synthetic.main.layout_sort_room.sortLocation

class SortRoomFragment : BottomSheetDialogFragment() {

    private var selectedSortRules: String = ""
    private lateinit var sortByLocationText: String
    private lateinit var sortByCapacityText: String
    private lateinit var sortByAvailabilityText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sortByLocationText = getString(R.string.lbl_sort_location)
        sortByCapacityText = getString(R.string.lbl_sort_capacity)
        sortByAvailabilityText = getString(R.string.lbl_sort_available)

        arguments?.let {
            selectedSortRules = it.getString(LAST_SELECTED_OPTION, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.layout_sort_room, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateSelection(selectedSortRules)

        sortGroup.setOnCheckedChangeListener { _, id ->
            when (id) {
                sortLocation.id -> {
                    selectedSortRules = sortByLocationText
                }
                sortCapacity.id -> {
                    selectedSortRules = sortByCapacityText
                }
                sortAvailability.id -> {
                    selectedSortRules = sortByAvailabilityText
                }
            }
        }

        resetButton.setOnClickListener {
            dismiss()
            // Cancel indicates reset
            targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, null)
        }

        applySortButton.setOnClickListener {
            dismiss()
            targetFragment?.let {
                it.onActivityResult(targetRequestCode, Activity.RESULT_OK, Intent().apply {
                    putExtra(SELECTED_ACTION_BUTTON, selectedSortRules)
                })
            }
        }
    }

    private fun updateSelection(selection: String) {
        when (selection) {
            sortByCapacityText -> {
                sortCapacity.isChecked = true
            }
            sortByLocationText -> {
                sortLocation.isChecked = true
            }
            sortByAvailabilityText -> {
                sortAvailability.isChecked = true
            }
        }
    }

    companion object {
        const val TAG = "SortRoomFragment"
        const val SELECTED_ACTION_BUTTON = "selected_sort_action_button"
        private const val LAST_SELECTED_OPTION = "last_selected_option"

        fun newInstance(lastSelected: String = "") = SortRoomFragment().apply {
            arguments = Bundle().apply {
                putString(LAST_SELECTED_OPTION, lastSelected)
            }
        }
    }
}