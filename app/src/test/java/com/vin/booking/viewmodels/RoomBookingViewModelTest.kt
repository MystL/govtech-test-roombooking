package com.vin.booking.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.vin.booking.models.Room
import com.vin.booking.repositories.BookingRepository
import com.vin.booking.testing.BaseTestCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RoomBookingViewModelTest : BaseTestCase() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var bookingRepository: BookingRepository

    @MockK
    lateinit var roomsObserver: Observer<List<Room>>

    @MockK
    lateinit var errorObserver: Observer<String>

    private lateinit var viewModel: RoomBookingViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        viewModel = RoomBookingViewModel(bookingRepository)
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
}
