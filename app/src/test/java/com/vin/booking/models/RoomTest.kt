package com.vin.booking.models

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.vin.booking.testing.BaseTestCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class RoomTest : BaseTestCase() {

    @Test
    fun `test serialization`() {
        val input = getResourceAsString("api-response/room_availability.json")

        val json = Gson().fromJson(input, JsonArray::class.java)
        val data = Gson().fromJson(Gson().toJson(json), Array<Room>::class.java)

        for (idx in 0 until json.size()) {
            val room = data[idx]
            assertTrue(room.isValid())
            assertEquals(json[idx].toString(), Gson().toJson(data[idx], Room::class.java))
        }
    }

    @Test
    fun `test serialization - no room name and availability`() {
        val input = getResourceAsString("api-response/room_availability_invalid.json")

        val json = Gson().fromJson(input, JsonArray::class.java)
        val data = Gson().fromJson(Gson().toJson(json), Array<Room>::class.java)

        for (idx in 0 until json.size()) {
            val room = data[idx]
            assertFalse(room.isValid())
        }
    }
}