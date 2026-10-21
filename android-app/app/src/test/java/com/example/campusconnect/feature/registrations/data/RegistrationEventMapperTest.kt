package com.example.campusconnect.feature.registrations.data

import com.example.campusconnect.feature.events.data.remote.response.EventResponse
import com.example.campusconnect.feature.registrations.data.mapper.RegistrationEventMapper
import org.junit.Assert.assertEquals
import org.junit.Test

class RegistrationEventMapperTest {
    @Test fun publicationMappingIsCaseInsensitiveAndKeepsUnapprovedEventsUnpublished() {
        assertEquals("Demo", RegistrationEventMapper.toHeader(event(" verified ")).title)
        assertEquals(true, RegistrationEventMapper.toHeader(event("approved")).isPublished)
        assertEquals(false, RegistrationEventMapper.toHeader(event("PENDING")).isPublished)
    }

    private fun event(approvalStatus: String) = EventResponse(
        id = 3,
        title = "Demo",
        description = null,
        latitude = 0.0,
        longitude = 0.0,
        startTime = "2026-01-01T00:00:00Z",
        endTime = null,
        createdBy = 10,
        clubId = null,
        hostName = "Campus",
        venue = "Hall",
        visibilityType = "PUBLIC",
        visibilityValue = null,
        registrationType = "THROUGH_APP",
        registrationLink = null,
        approvalStatus = approvalStatus,
        eventState = "UPCOMING",
        priority = 0,
        posterUrl = null,
        categoryId = null,
        categoryName = null,
    )
}
