package com.vin.booking.repositories

import com.vin.booking.testing.BaseTestCase
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import org.junit.Test

class BookingRepositoryTest : BaseTestCase() {

    @Test
    fun `test init repo`() {
        // Given
        val repo = BookingRepository(getMockApi())

        // Then
        assertNotNull(repo)
    }

    @Test
    fun `test fetch rooms - success`() {
        // Given
        startMockServer(mockResponseFromFile("room_availability.json"))
        val repo = BookingRepository(getMockApi())

        // When
        val result = runBlocking { repo.fetchRooms() }

        // Then
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        val firstData = result.first()
        // Just to ensure the parsing works as it is
        assertEquals("Kopi-O", firstData.name)
        assertEquals("8", firstData.capacity)
        assertEquals("7", firstData.level)
    }

    @Test
    fun `test fetch rooms - success but empty data returned`() {
        // Given
        startMockServer(MockResponse().setBody(""))
        val repo = BookingRepository(getMockApi())

        // When
        val result = runBlocking { repo.fetchRooms() }

        // Then
        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test fetch rooms - failed , will return empty list for now`() {
        // Given
        startMockServer(MockResponse().setResponseCode(404))
        val repo = BookingRepository(getMockApi())

        // When
        val result = runBlocking { repo.fetchRooms() }

        // Then
        assertNotNull(result)
        assertTrue(result.isEmpty())
    }
}