package com.example.campusconnect.feature.registrations.userflow.model

enum class RegistrationQuestionScreenKind { INDIVIDUAL, TEAM }

/** Presentation context chosen with the question set by the owning registration ViewModel. */
data class RegistrationQuestionScreenConfig(
    val kind: RegistrationQuestionScreenKind,
    val subtitle: String,
    val progressStep: Int,
    val actionLabel: String,
)
