package com.vin.booking.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vin.booking.models.Room
import com.vin.booking.repositories.BookingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class RoomBookingViewModel @Inject constructor(
    private val repository: BookingRepository
) : ViewModel() {

    private val _rooms = MutableLiveData<List<Room>>()
    val rooms: LiveData<List<Room>> get() = _rooms

    private val _fetchRoomsError = MutableLiveData<String>()
    val fetchRoomsError: LiveData<String> get() = _fetchRoomsError

    fun fetchRooms() {
        viewModelScope.launch(Dispatchers.IO) {
            val r = repository.fetchRooms()
            r.takeIf { it.isNotEmpty() }?.let { rooms ->
                // Some validity check here assuming that
                // Rooms returned might not be entirely valid
                rooms.filter { it.isValid() }
                    .takeIf { it.isNotEmpty() }
                    ?.let {
                        _rooms.postValue(it)
                    }
            } ?: _fetchRoomsError.postValue("Unable to fetch rooms")
        }
    }
}