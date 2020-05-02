package com.vin.booking.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vin.booking.R
import kotlinx.android.synthetic.main.item_room.view.textViewAvailability
import kotlinx.android.synthetic.main.item_room.view.textViewCapacity
import kotlinx.android.synthetic.main.item_room.view.textViewLevel
import kotlinx.android.synthetic.main.item_room.view.textViewRoomName

class RoomsDisplayAdapter : RecyclerView.Adapter<RoomItem>() {

    private val _dataSet = mutableListOf<RoomItemDisplay>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RoomItem(
            LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        )

    override fun getItemCount() = _dataSet.size

    override fun onBindViewHolder(holder: RoomItem, position: Int) {
        holder.onBind(_dataSet[position])
    }

    fun setData(dataSet: List<RoomItemDisplay>) {
        _dataSet.clear()
        _dataSet.addAll(dataSet)
        notifyDataSetChanged()
    }
}

class RoomItem(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val roomNameTextView = itemView.textViewRoomName
    private val levelTextView = itemView.textViewLevel
    private val availabilityTextView = itemView.textViewAvailability
    private val capacityTextView = itemView.textViewCapacity

    fun onBind(item: RoomItemDisplay) {
        roomNameTextView.text = item.name

        levelTextView.text = itemView.context.getString(R.string.level_display_format, item.level)

        capacityTextView.text =
            itemView.context.getString(R.string.capacity_display_format, item.capacity)

        item.available.let {
            if (it) {
                availabilityTextView.text = itemView.context.getString(R.string.lbl_available)
                availabilityTextView.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.availableGreen
                    )
                )
            } else {
                availabilityTextView.text = itemView.context.getString(R.string.lbl_not_available)
                availabilityTextView.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.lightGrey
                    )
                )
            }
        }
    }
}

data class RoomItemDisplay(
    val name: String,
    val capacity: String,
    val level: String,
    val available: Boolean
)
