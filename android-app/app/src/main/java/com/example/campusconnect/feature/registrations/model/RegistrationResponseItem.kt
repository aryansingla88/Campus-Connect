package com.example.campusconnect.feature.registrations.model

enum class RegistrationResponseType {
    INDIVIDUAL,
    TEAM,
}

enum class RegistrationResponseStatus {
    CONFIRMED,
    PENDING,
    CANCELLED,
}

data class RegistrationResponseItem(
    val id: Int,
    val name: String,
    val email: String,
    val type: RegistrationResponseType,
    val status: RegistrationResponseStatus,
    val submittedAt: String,
    val memberCount: Int? = null,
)
