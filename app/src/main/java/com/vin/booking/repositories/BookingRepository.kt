package com.vin.booking.repositories

import com.vin.booking.api.Api
import com.vin.booking.models.Room
import javax.inject.Inject

class BookingRepository @Inject constructor(private val api: Api) {

    // We can expand this function like error handling by returning
    // different types of Object which includes data and / error
    // for allowing ViewModel to handle the relevant scenario.
    // For now, particularly this exercise,
    // we are just assuming this function always returns a List of Rooms
    suspend fun fetchRooms(): List<Room> {
        return try {
            val r = api.fetchRooms()
            r?.takeIf { it.isNotEmpty() } ?: listOf()
        } catch (e: Exception) {
            listOf()
        }
    }
}