package com.example.campusconnect.feature.events.registerations.data
import com.example.campusconnect.feature.events.registerations.model.Registration

object FakeRegistrationService {
    const val EVENT_ID = 1

    val registration = Registration(
        eventId     = EVENT_ID,
        title       = "Android Workshop Registration",
        isPublished = false,
    )
}