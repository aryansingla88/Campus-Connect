package com.example.campusconnect.feature.map.data.mapper

import com.example.campusconnect.feature.map.data.remote.response.EventCategoryResponse
import com.example.campusconnect.feature.map.data.remote.response.EventHostResponse
import com.example.campusconnect.feature.map.data.remote.response.EventPreviewResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MapEventMapperTest {
    @Test
    fun previewMapperPreservesSupportedBackendFields() {
        val result = MapEventMapper.toInfo(
            EventPreviewResponse(
                id = 31,
                title = "Campus event",
                description = "Details",
                posterUrl = null,
                hostName = "Host",
                creatorName = "Creator",
                clubName = "Robotics",
                registrationCount = 18,
                eventType = "Workshop",
                categories = listOf(EventCategoryResponse(7, "Workshop")),
                startTime = "2026-10-20T09:00:00Z",
                endTime = "2026-10-20T10:00:00Z",
                venue = "Hall",
                registrationType = "TEAM",
                registrationLink = "https://example.invalid/register",
                isJoined = true,
                isReminderEnabled = false,
                priority = 2,
                hosts = listOf(EventHostResponse(41, "Host", "avatar", "ADMIN"))
            )
        )

        assertEquals(31, result.id)
        assertEquals("2026-10-20T09:00:00Z", result.date)
        assertEquals("2026-10-20T09:00:00Z", result.time)
        assertEquals("2026-10-20T10:00:00Z", result.endTime)
        assertEquals("Host", result.hostName)
        assertEquals("Creator", result.creatorName)
        assertEquals("Robotics", result.clubName)
        assertEquals(18, result.registrationCount)
        assertEquals("Workshop", result.eventType)
        assertEquals(listOf("Workshop"), result.categories.map { it.name })
        assertEquals(7, result.categories.single().id)
        assertEquals("TEAM", result.registrationType)
        assertEquals("https://example.invalid/register", result.registrationLink)
        assertFalse(result.isReminderEnabled!!)
        assertEquals(2, result.priority)
        assertEquals("ADMIN", result.hosts.single().role)
    }

    @Test
    fun missingOptionalPreviewFieldsStayEmptyOrNull() {
        val result = MapEventMapper.toInfo(EventPreviewResponse(id = 2, title = "No metadata"))

        assertEquals("", result.hostName)
        assertEquals("", result.date)
        assertEquals("", result.time)
        assertEquals("", result.description)
        assertTrue(result.hosts.isEmpty())
        assertTrue(result.categories.isEmpty())
        assertNull(result.registrationCount)
        assertNull(result.registrationType)
        assertNull(result.registrationLink)
    }
}
