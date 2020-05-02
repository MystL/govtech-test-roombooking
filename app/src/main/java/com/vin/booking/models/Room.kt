package com.vin.booking.models

import com.google.gson.annotations.SerializedName

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