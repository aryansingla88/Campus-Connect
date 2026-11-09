package com.example.campusconnect.feature.registrations.data

import com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationConfigRequest
import com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationConfigResponse
import com.example.campusconnect.feature.registrations.data.mapper.toRegistrationSettings
import com.example.campusconnect.feature.registrations.data.mapper.toRegistrationConfigRequest
import com.example.campusconnect.feature.registrations.model.RegistrationSettings
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class RegistrationConfigMappingTest {
    private val gson = Gson()

    @Test
    fun requestDefaultsAutoConfirmAndPreservesExplicitFalse() {
        val defaultRequest = RegistrationConfigRequest(1, 1, 0L)
        val defaultJson = gson.toJson(defaultRequest)
        assertEquals(true, gson.fromJson(defaultJson, Map::class.java)["auto_confirm"])

        val falseRequest = defaultRequest.copy(autoConfirm = false)
        val falseJson = gson.toJson(falseRequest)
        assertEquals(false, gson.fromJson(falseJson, Map::class.java)["auto_confirm"])
    }

    @Test
    fun responseMappingDefaultsMissingAutoConfirmToTrueAndPreservesFalse() {
        val legacyJson = """{"eventId":3,"minTeamMembers":1,"maxTeamMembers":1}"""
        val legacy = gson.fromJson(legacyJson, RegistrationConfigResponse::class.java)
        assertEquals(true, legacy.toRegistrationSettings().autoConfirm)

        val explicitFalse = gson.fromJson(
            """{"eventId":3,"minTeamMembers":1,"maxTeamMembers":1,"auto_confirm":false}""",
            RegistrationConfigResponse::class.java,
        )
        assertEquals(false, explicitFalse.toRegistrationSettings().autoConfirm)
    }

    @Test
    fun configMappingPreservesDeadlineAndIgnoresMalformedDates() {
        val settings = RegistrationSettings(
            minMembers = 2,
            maxMembers = 5,
            quickFieldMask = 12L,
            autoConfirm = false,
            registrationCreatedAt = "created",
            deadline = 1_767_323_045_000L,
        )
        val request = settings.toRegistrationConfigRequest()
        assertEquals("2026-01-02T03:04:05Z", request.registrationEndAt)
        assertEquals(settings, RegistrationConfigResponse(
            eventId = 3,
            minTeamMembers = 2,
            maxTeamMembers = 5,
            quickFieldMask = 12L,
            autoConfirm = false,
            registrationCreatedAt = "created",
            registrationEndAt = request.registrationEndAt,
        ).toRegistrationSettings())

        val malformed = RegistrationConfigResponse(3, 1, 1, registrationEndAt = "not-a-date")
        assertEquals(null, malformed.toRegistrationSettings().deadline)
    }
}
