package com.vin.booking.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vin.booking.CoroutineContext
import com.vin.booking.adapters.RoomItemDisplay
import com.vin.booking.models.Room
import com.vin.booking.repositories.BookingRepository
import com.vin.booking.utils.extensions.getDayOfMonthSuffix
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class RoomBookingViewModel @Inject constructor(
    private val coroutineContext: CoroutineContext,
    private val repository: BookingRepository
) : ViewModel() {

    var currentSortRules: String = ""

    private val unsortedDisplayDataSet = mutableListOf<RoomItemDisplay>()

    private val _currentTimeDisplay = MutableLiveData<String>()
    val currentTimeDisplay: LiveData<String> get() = _currentTimeDisplay

    private val _currentDateDisplay = MutableLiveData<String>()
    val currentDateDisplay: LiveData<String> get() = _currentDateDisplay

    private val _rooms = MutableLiveData<List<Room>>()
    val rooms: LiveData<List<Room>> get() = _rooms

    private val _fetchRoomsError = MutableLiveData<String>()
    val fetchRoomsError: LiveData<String> get() = _fetchRoomsError

    private val _sortedDisplayRooms = MutableLiveData<List<RoomItemDisplay>>()
    val sortedDisplayRooms: LiveData<List<RoomItemDisplay>> get() = _sortedDisplayRooms

    fun fetchRooms() {
        viewModelScope.launch(coroutineContext.io) {
            val r = repository.fetchRooms()
            r.takeIf { it.isNotEmpty() }?.let { rooms ->
                // Some validity check here assuming that
                // Rooms returned might not be entirely valid
                rooms.sortedBy { it.level } // Requirements wanted to be in ascending
                    .filter { it.isValid() }
                    .takeIf { it.isNotEmpty() }
                    ?.let {
                        _rooms.postValue(it)
                    }
            } ?: _fetchRoomsError.postValue("Unable to fetch rooms")
        }
    }

    fun getRoomsForTimeSlot(timeSlot: String): List<Room> {
        return _rooms.value?.filter { it.availability?.containsKey(timeSlot) == true } ?: listOf()
    }

    fun sortRoomsBy(rules: SortRules, data: List<RoomItemDisplay>) {
        when (rules) {
            SortRules.CAPACITY -> {
                _sortedDisplayRooms.value =
                    data.sortedWith(compareByDescending { it.capacity.toInt() })
            }
            SortRules.AVAILABILITY -> {
                _sortedDisplayRooms.value =
                    data.sortedWith(compareByDescending { it.available })
            }
            SortRules.LOCATION -> {
                _sortedDisplayRooms.value = data.sortedBy { it.name }
            }
        }
    }

    fun setUnsortedDisplayDataSet(dataSet: List<RoomItemDisplay>) {
        unsortedDisplayDataSet.clear()
        unsortedDisplayDataSet.addAll(dataSet)
    }

    fun resetSort() {
        _sortedDisplayRooms.value = unsortedDisplayDataSet
    }

    fun setCurrentDate(calendar: Calendar) {
        val df = SimpleDateFormat(ACTUAL_DATE_FORMAT, Locale.US)
        _currentDateDisplay.value = df.format(calendar.time)
    }

    fun getFormattedDateDisplay(): String =
        _currentDateDisplay.value?.takeIf { it.isNotEmpty() }
            ?.let {
                val input = SimpleDateFormat(ACTUAL_DATE_FORMAT, Locale.US)
                val output = input.parse(it)
                output?.let {
                    val calendar = Calendar.getInstance(Locale.US)
                    calendar.time = output
                    val displayDateFormat = SimpleDateFormat(
                        "d'" +
                            getDayOfMonthSuffix(calendar.get(Calendar.DATE)) +
                            "' MMM yyyy", Locale.US
                    )
                    displayDateFormat.format(calendar.time)
                } ?: ""
            } ?: ""

    fun setCurrentTime(calendar: Calendar) {
        val currentMin = calendar.get(Calendar.MINUTE)
        // Forcing the display to be in 30 mins interval regardless of current time
        // since the availability is based on interval of 30 mins as well
        if (currentMin >= 0 && currentMin <= 15 || currentMin >= 45 && currentMin <= 59) {
            calendar.set(Calendar.MINUTE, 0)
        } else {
            calendar.set(Calendar.MINUTE, 30)
        }
        val tf = SimpleDateFormat(ACTUAL_TIME_FORMAT, Locale.US)
        _currentTimeDisplay.value = tf.format(calendar.time)
    }

    fun getFormattedTimeDisplay(): String =
        _currentTimeDisplay.value?.takeIf { it.isNotEmpty() }
            ?.let {
                val input = SimpleDateFormat(ACTUAL_TIME_FORMAT, Locale.US)
                val output = input.parse(it)
                output?.let {
                    val displayTimeFormat = SimpleDateFormat(DISPLAY_TIME_FORMAT, Locale.US)
                    displayTimeFormat.format(output.time)
                } ?: ""
            } ?: ""

    companion object {
        const val DISPLAY_TIME_FORMAT = "h:mm aa"
        const val ACTUAL_TIME_FORMAT = "HH:mm"
        const val ACTUAL_DATE_FORMAT = "dd-MM-yyyy"
    }
}

enum class SortRules {
    LOCATION,
    AVAILABILITY,
    CAPACITY
}