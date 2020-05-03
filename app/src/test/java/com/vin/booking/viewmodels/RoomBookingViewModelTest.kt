package com.vin.booking.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.vin.booking.CoroutineContext
import com.vin.booking.adapters.RoomItemDisplay
import com.vin.booking.models.Room
import com.vin.booking.repositories.BookingRepository
import com.vin.booking.testing.BaseTestCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class RoomBookingViewModelTest : BaseTestCase() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    protected val testCoroutineContext = CoroutineContext(
        Dispatchers.Unconfined,
        Dispatchers.Unconfined,
        Dispatchers.Unconfined
    )

    @MockK
    private lateinit var bookingRepository: BookingRepository

    @MockK
    lateinit var roomsObserver: Observer<List<Room>>

    @MockK
    lateinit var errorObserver: Observer<String>

    @MockK
    lateinit var sortedRoomsObserver: Observer<List<RoomItemDisplay>>

    private lateinit var viewModel: RoomBookingViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        viewModel = RoomBookingViewModel(testCoroutineContext, bookingRepository)
    }

    @Test
    fun `test get rooms - all rooms valid`() {
        runBlocking {
            // Given
            val expected = listOf(
                Room(
                    "room1", "2", "4", mapOf(
                        Pair("time1", "0"),
                        Pair("time2", "1")
                    )
                ),
                Room(
                    "room2", "7", "5", mapOf(
                        Pair("time1", "0"),
                        Pair("time2", "1")
                    )
                )
            )
            coEvery { bookingRepository.fetchRooms() }.returns(expected)
            viewModel.rooms.observe(LIFECYCLE_OWNER, roomsObserver)
            viewModel.fetchRoomsError.observe(LIFECYCLE_OWNER, errorObserver)

            // When
            viewModel.fetchRooms()

            // Then
            coVerify(exactly = 1) { bookingRepository.fetchRooms() }
            verify { roomsObserver.onChanged(expected) }
            verify(inverse = true) { errorObserver.onChanged(any()) }
        }
    }

    @Test
    fun `test get rooms - some room invalid`() {
        runBlocking {
            // Given
            val validRoom1 = Room(
                "room1", "2", "4", mapOf(
                    Pair("time1", "0"),
                    Pair("time2", "1")
                )
            )
            val invalidRoom1 = Room("room2", "7", "5", null)
            val invalidRoom2 = Room(
                "", "7", "5", mapOf(
                    Pair("time1", "0"),
                    Pair("time2", "1")
                )
            )
            val input = listOf(validRoom1, invalidRoom1, invalidRoom2)
            val expected = listOf(validRoom1)

            coEvery { bookingRepository.fetchRooms() }.returns(input)
            viewModel.rooms.observe(LIFECYCLE_OWNER, roomsObserver)
            viewModel.fetchRoomsError.observe(LIFECYCLE_OWNER, errorObserver)

            // When
            viewModel.fetchRooms()

            // Then
            coVerify(exactly = 1) { bookingRepository.fetchRooms() }
            verify { roomsObserver.onChanged(expected) }
            verify(inverse = true) { errorObserver.onChanged(any()) }
        }
    }

    @Test
    fun `test get rooms - no valid rooms`() {
        runBlocking {
            // Given
            val invalidRoom1 = Room("room2", "7", "5", null)
            val invalidRoom2 = Room(
                "", "7", "5", mapOf(
                    Pair("time1", "0"),
                    Pair("time2", "1")
                )
            )
            val input = listOf(invalidRoom1, invalidRoom2)

            coEvery { bookingRepository.fetchRooms() }.returns(input)
            viewModel.rooms.observe(LIFECYCLE_OWNER, roomsObserver)
            viewModel.fetchRoomsError.observe(LIFECYCLE_OWNER, errorObserver)

            // When
            viewModel.fetchRooms()

            // Then
            coVerify(exactly = 1) { bookingRepository.fetchRooms() }
            verify(inverse = true) { roomsObserver.onChanged(any()) }
            verify { errorObserver.onChanged(any()) }
        }
    }

    @Test
    fun `test default rooms should be sorted by level in ascending order`() {
        val room1 = Room(
            "room1", "2", "5", mapOf(
                Pair("time1", "0"),
                Pair("time2", "1")
            )
        )
        val room2 = Room(
            "room2", "7", "6", mapOf(
                Pair("time1", "0"),
                Pair("time2", "1")
            )
        )
        val room3 = Room(
            "room2", "7", "2", mapOf(
                Pair("time1", "0"),
                Pair("time2", "1")
            )
        )

        val input = listOf(room1, room2, room3)
        coEvery { bookingRepository.fetchRooms() }.returns(input)
        viewModel.rooms.observe(LIFECYCLE_OWNER, roomsObserver)
        viewModel.fetchRoomsError.observe(LIFECYCLE_OWNER, errorObserver)
        val expected = listOf(room3, room1, room2)

        // When
        viewModel.fetchRooms()

        verify { roomsObserver.onChanged(expected) }
    }

    @Test
    fun `test set current date`() {
        // Given
        val cal = Calendar.getInstance(Locale.US)
        cal.set(2020, 10, 23)

        val expected = "23-11-2020" // because month starts from zero

        // When
        viewModel.setCurrentDate(cal)

        // Then
        assertNotNull(viewModel.currentDateDisplay.value)
        assertEquals(expected, viewModel.currentDateDisplay.value)
    }

    @Test
    fun `test get current date display format`() {
        // Given
        val cal = Calendar.getInstance(Locale.US)
        cal.set(2020, 10, 3)
        viewModel.setCurrentDate(cal)

        // When
        val actual = viewModel.getFormattedDateDisplay()

        // Then
        assertTrue(actual.isNotEmpty())
        assertEquals("3rd Nov 2020", actual)
    }

    @Test
    fun `test set current time`() {
        val cal = Calendar.getInstance(Locale.US)
        cal.set(Calendar.HOUR_OF_DAY, 14)
        cal.set(Calendar.MINUTE, 30)

        // When
        viewModel.setCurrentTime(cal)

        // Then
        assertNotNull(viewModel.currentTimeDisplay.value)
        assertTrue(viewModel.currentTimeDisplay.value!!.isNotEmpty())
        assertEquals("14:30", viewModel.currentTimeDisplay.value)
    }

    @Test
    fun `test get current time display - should only format to 30 mins interval`() {
        val cal = Calendar.getInstance(Locale.US)
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 12)
        viewModel.setCurrentTime(cal)

        // When
        val actual = viewModel.getFormattedTimeDisplay()

        // Then
        assertTrue(actual.isNotEmpty())
        assertEquals("12:00 PM", actual)
    }

    @Test
    fun `test get current time display - current time more than 15 mins`() {
        val cal = Calendar.getInstance(Locale.US)
        cal.set(Calendar.HOUR_OF_DAY, 8)
        cal.set(Calendar.MINUTE, 16)
        viewModel.setCurrentTime(cal)

        // When
        val actual = viewModel.getFormattedTimeDisplay()

        // Then
        assertTrue(actual.isNotEmpty())
        assertEquals("8:30 AM", actual)
    }

    @Test
    fun `test get rooms for specific time slot`() {
        runBlocking {
            // Given
            val room1 = Room(
                "room2", "7", "5", mapOf(
                    Pair("time1", "0"),
                    Pair("time4", "1")
                )
            )
            val room2 = Room(
                "room3", "7", "5", mapOf(
                    Pair("time1", "0"),
                    Pair("time2", "1")
                )
            )
            val input = listOf(room1, room2)

            coEvery { bookingRepository.fetchRooms() }.returns(input)
            viewModel.rooms.observe(LIFECYCLE_OWNER, roomsObserver)

            // When
            viewModel.fetchRooms()
            val actual = viewModel.getRoomsForTimeSlot("time1")

            assertEquals(2, actual.size)
            assertEquals(actual[0], room1)
            assertEquals(actual[1], room2)
        }
    }

    @Test
    fun `test get rooms for specific time slot - partial match`() {
        runBlocking {
            // Given
            val room1 = Room(
                "room2", "7", "5", mapOf(
                    Pair("time1", "0"),
                    Pair("time4", "1")
                )
            )
            val room2 = Room(
                "room3", "7", "5", mapOf(
                    Pair("time1", "0"),
                    Pair("time2", "1")
                )
            )
            val input = listOf(room1, room2)

            coEvery { bookingRepository.fetchRooms() }.returns(input)
            viewModel.rooms.observe(LIFECYCLE_OWNER, roomsObserver)

            // When
            viewModel.fetchRooms()
            val actual = viewModel.getRoomsForTimeSlot("time2")

            assertEquals(1, actual.size)
            assertEquals(actual[0], room2)
        }
    }

    @Test
    fun `test get rooms for time slot - no matching timeslot`() {
        runBlocking {
            // Given
            val room1 = Room(
                "room2", "7", "5", mapOf(
                    Pair("time1", "0"),
                    Pair("time4", "1")
                )
            )
            val room2 = Room(
                "room3", "7", "5", mapOf(
                    Pair("time1", "0"),
                    Pair("time2", "1")
                )
            )
            val input = listOf(room1, room2)

            coEvery { bookingRepository.fetchRooms() }.returns(input)
            viewModel.rooms.observe(LIFECYCLE_OWNER, roomsObserver)

            // When
            viewModel.fetchRooms()
            val actual = viewModel.getRoomsForTimeSlot("time5")

            assertTrue(actual.isEmpty())
        }
    }

    @Test
    fun `sort rooms by Capacity`() {
        runBlocking {
            // Given
            val room1 = RoomItemDisplay(
                "room1", "2", "5", true
            )
            val room2 = RoomItemDisplay(
                "room2", "8", "6", true
            )
            val room3 = RoomItemDisplay(
                "room3", "3", "2", true
            )
            val room4 = RoomItemDisplay(
                "room4", "10", "2", true
            )

            val input = listOf(room1, room2, room3, room4)
            val expected = listOf(room4, room2, room3, room1)
            val captor = slot<List<RoomItemDisplay>>()

            viewModel.sortedDisplayRooms.observe(LIFECYCLE_OWNER, sortedRoomsObserver)

            // When
            viewModel.sortRoomsBy(SortRules.CAPACITY, input)

            verify { sortedRoomsObserver.onChanged(capture(captor)) }

            assertEquals(expected, captor.captured)
        }
    }

    @Test
    fun `sort rooms by Availability`() {
        runBlocking {
            // Given
            val room1 = RoomItemDisplay(
                "room1", "2", "5", false
            )
            val room2 = RoomItemDisplay(
                "room2", "3", "6", true
            )
            val room3 = RoomItemDisplay(
                "room3", "1", "2", false
            )
            val room4 = RoomItemDisplay(
                "room4", "8", "2", true
            )

            val input = listOf(room1, room2, room3, room4)
            val expected = listOf(room2, room4, room1, room3)
            val captor = slot<List<RoomItemDisplay>>()

            viewModel.sortedDisplayRooms.observe(LIFECYCLE_OWNER, sortedRoomsObserver)

            // When
            viewModel.sortRoomsBy(SortRules.AVAILABILITY, input)

            verify { sortedRoomsObserver.onChanged(capture(captor)) }

            assertEquals(expected, captor.captured)
        }
    }

    @Test
    fun `sort rooms by Location`() {
        runBlocking {
            // Given
            val room1 = RoomItemDisplay(
                "Summer", "2", "5", false
            )
            val room2 = RoomItemDisplay(
                "Wonderland", "3", "6", true
            )
            val room3 = RoomItemDisplay(
                "Absolute", "1", "2", false
            )
            val room4 = RoomItemDisplay(
                "Bank", "0", "2", true
            )

            val input = listOf(room1, room2, room3, room4)
            val expected = listOf(room3, room4, room1, room2)
            val captor = slot<List<RoomItemDisplay>>()

            viewModel.sortedDisplayRooms.observe(LIFECYCLE_OWNER, sortedRoomsObserver)

            // When
            viewModel.sortRoomsBy(SortRules.LOCATION, input)

            verify { sortedRoomsObserver.onChanged(capture(captor)) }

            assertEquals(expected, captor.captured)
        }
    }


}
