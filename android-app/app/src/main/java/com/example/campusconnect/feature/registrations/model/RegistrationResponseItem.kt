package com.example.campusconnect.feature.registrations.model

enum class RegistrationResponseType {
    INDIVIDUAL,
    TEAM,
    OTHER,
}

enum class RegistrationResponseStatus {
    CONFIRMED,
    PENDING,
    REJECTED,
    WAITLISTED,
    UNKNOWN,
}

data class RegistrationResponseItem(
    val id: Int,
    val name: String?,
    val email: String?,
    val type: RegistrationResponseType,
    val status: RegistrationResponseStatus?,
    val submittedAt: String?,
    val memberCount: Int? = null,
    val submittedAtEpochMillis: Long? = null,
    /** Present for participant fake records; existing host API responses may omit it. */
    val teamName: String? = null,
)
