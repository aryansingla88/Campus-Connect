package com.example.campusconnect.feature.registrations.data
import com.example.campusconnect.feature.registrations.model.Registration
import com.example.campusconnect.feature.registrations.model.RegistrationSettings

object FakeRegistrationService {
    const val EVENT_ID = 1

    val registration = Registration(
        eventId     = EVENT_ID,
        eventName   = "Android Workshop",
        title       = "Android Workshop Registration",
        isPublished = false,
        settings    = RegistrationSettings(
            allowIndividual     = true,
            allowTeam           = true,
            minMembers          = 2,
            maxMembers          = 4,
            quickFieldsSelected = 5,
        ),
    )
}
