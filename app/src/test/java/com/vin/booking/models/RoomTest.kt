package com.vin.booking.models

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.vin.booking.adapters.RoomItemDisplay
import com.vin.booking.testing.BaseTestCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class RoomTest : BaseTestCase() {

    @Test
    fun `test serialization`() {
        // Given
        val input = getResourceAsString("api-response/room_availability.json")

        // When
        val json = Gson().fromJson(input, JsonArray::class.java)
        val data = Gson().fromJson(Gson().toJson(json), Array<Room>::class.java)

        // Then
        for (idx in 0 until json.size()) {
            val room = data[idx]
            assertTrue(room.isValid())
            assertEquals(json[idx].toString(), Gson().toJson(data[idx], Room::class.java))
        }
    }

    @Test
    fun `test serialization - no room name and availability`() {
        // Given
        val input = getResourceAsString("api-response/room_availability_invalid.json")

        // When
        val json = Gson().fromJson(input, JsonArray::class.java)
        val data = Gson().fromJson(Gson().toJson(json), Array<Room>::class.java)

        // Then
        for (idx in 0 until json.size()) {
            val room = data[idx]
            assertFalse(room.isValid())
        }
    }

    @Test
    fun `test convert to RoomItemDisplay, room not available`() {
        // Given
        val room = Room(
            "room",
            "8",
            "10",
            availability = mapOf(
                Pair("10:30", "0"),
                Pair("11:30", "1")
            )
        )

        val expected = RoomItemDisplay("room", "8", "10", false)

        // When
        val display = room.toRoomItemDisplay("10:30")

        // Then
        assertEquals(expected, display)
    }

    @Test
    fun `test convert to RoomItemDisplay, room available`() {
        // Given
        val room = Room(
            "room",
            "8",
            "10",
            availability = mapOf(
                Pair("10:30", "0"),
                Pair("11:30", "1")
            )
        )

        val expected = RoomItemDisplay("room", "8", "10", true)

        // When
        val display = room.toRoomItemDisplay("11:30")

        // Then
        assertEquals(expected, display)
    }

    @Test
    fun `test convert to RoomItemDisplay, given time not in availability`() {
        // Given
        val room = Room(
            "room",
            "8",
            "10",
            availability = mapOf(
                Pair("10:30", "0"),
                Pair("11:30", "1")
            )
        )

        val expected = RoomItemDisplay("room", "8", "10", false)

        // When
        val display = room.toRoomItemDisplay("12:30")

        // Then
        assertEquals(expected, display)
    }

    @Test
    fun `test convert to RoomItemDisplay, availability is null in Room`() {
        // Given
        val room = Room(
            "room",
            "8",
            "10",
            availability = null
        )

        val expected = RoomItemDisplay("room", "8", "10", false)

        // When
        val display = room.toRoomItemDisplay("12:30")

        // Then
        assertEquals(expected, display)
    }

    @Test
    fun `test convert to RoomItemDisplay, no time selection`() {
        // Given
        val room = Room(
            "room",
            "8",
            "10",
            availability = null
        )

        val expected = RoomItemDisplay("room", "8", "10", false)

        // When
        val display = room.toRoomItemDisplay("")

        // Then
        assertEquals(expected, display)
    }
}