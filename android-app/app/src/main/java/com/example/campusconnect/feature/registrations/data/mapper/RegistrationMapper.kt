package com.example.campusconnect.feature.registrations.data.mapper

import com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationConfigRequest
import com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationConfigResponse
import com.example.campusconnect.feature.registrations.data.remote.dto.ParticipantRegistrationConfigDto
import com.example.campusconnect.feature.registrations.model.RegistrationParticipationRules
import com.example.campusconnect.feature.registrations.model.RegistrationSettings
import java.time.Instant
import java.time.format.DateTimeParseException

fun RegistrationConfigResponse.toRegistrationSettings(): RegistrationSettings {
    val deadline = registrationEndAt?.let {
        try { Instant.parse(it).toEpochMilli() } catch (_: DateTimeParseException) { null }
    }
    return RegistrationSettings(
        minMembers = minTeamMembers,
        maxMembers = maxTeamMembers,
        quickFieldMask = quickFieldMask,
        autoConfirm = autoConfirm ?: true,
        registrationCreatedAt = registrationCreatedAt,
        deadline = deadline,
    )
}

fun RegistrationSettings.toRegistrationConfigRequest(): RegistrationConfigRequest = RegistrationConfigRequest(
    minTeamMembers = minMembers,
    maxTeamMembers = maxMembers,
    quickFieldMask = quickFieldMask,
    autoConfirm = autoConfirm,
    registrationCreatedAt = registrationCreatedAt,
    registrationEndAt = deadline?.let { Instant.ofEpochMilli(it).toString() },
)

fun ParticipantRegistrationConfigDto.toParticipationRules(): RegistrationParticipationRules =
    RegistrationParticipationRules(
        allowSolo = (minTeamMembers ?: 1) <= 1,
        allowTeam = (maxTeamMembers ?: 1) > 1,
    )
