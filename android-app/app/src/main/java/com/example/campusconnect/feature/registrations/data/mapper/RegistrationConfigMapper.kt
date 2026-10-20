package com.example.campusconnect.feature.registrations.data.mapper

import com.example.campusconnect.feature.registrations.data.remote.RegistrationConfigRequest
import com.example.campusconnect.feature.registrations.data.remote.RegistrationConfigResponse
import com.example.campusconnect.feature.registrations.model.RegistrationSettings
import java.time.Instant
import java.time.format.DateTimeParseException

object RegistrationConfigMapper {
    fun toModel(response: RegistrationConfigResponse): RegistrationSettings {
        val deadline = response.registrationEndAt?.let {
            try { Instant.parse(it).toEpochMilli() } catch (_: DateTimeParseException) { null }
        }
        return RegistrationSettings(
            minMembers = response.minTeamMembers,
            maxMembers = response.maxTeamMembers,
            quickFieldMask = response.quickFieldMask,
            autoConfirm = response.autoConfirm ?: true,
            registrationCreatedAt = response.registrationCreatedAt,
            deadline = deadline,
        )
    }

    fun toRequest(settings: RegistrationSettings): RegistrationConfigRequest = RegistrationConfigRequest(
        minTeamMembers = settings.minMembers,
        maxTeamMembers = settings.maxMembers,
        quickFieldMask = settings.quickFieldMask,
        autoConfirm = settings.autoConfirm,
        registrationCreatedAt = settings.registrationCreatedAt,
        registrationEndAt = settings.deadline?.let { Instant.ofEpochMilli(it).toString() },
    )
}
