package com.vin.booking.models

import com.google.gson.annotations.SerializedName
import com.vin.booking.adapters.RoomItemDisplay

data class Room(
    @SerializedName("name") val name: String?,
    @SerializedName("capacity") val capacity: String?,
    @SerializedName("level") val level: String?,
    @SerializedName("availability") val availability: Map<String, String>?
) : Validity {

    override fun isValid(): Boolean {
        return name?.isNotEmpty() == true && availability?.isNotEmpty() == true
    }
}

fun Room.toRoomItemDisplay(time: String): RoomItemDisplay {
    return RoomItemDisplay(
        name ?: "",
        capacity ?: "",
        level ?: "",
        when (availability?.get(time)) {
            "1" -> true
            "0" -> false
            else -> false // assuming false if got something different
        }
    )
}