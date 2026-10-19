package com.example.campusconnect.feature.registrations.data

import com.example.campusconnect.feature.registrations.data.remote.RegistrationConfigRequest
import com.example.campusconnect.feature.registrations.data.remote.RegistrationConfigResponse
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
        val repository = RegistrationConfigRepository()
        assertEquals(true, with(repository) { legacy.toSettings() }.autoConfirm)

        val explicitFalse = gson.fromJson(
            """{"eventId":3,"minTeamMembers":1,"maxTeamMembers":1,"auto_confirm":false}""",
            RegistrationConfigResponse::class.java,
        )
        assertEquals(false, with(repository) { explicitFalse.toSettings() }.autoConfirm)
    }
}
