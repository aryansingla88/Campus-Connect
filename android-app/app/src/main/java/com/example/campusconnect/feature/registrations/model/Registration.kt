package com.example.campusconnect.feature.registrations.model

data class Registration(
    val eventId: Int,
    val eventName: String,
    val title: String,
    val isPublished: Boolean,
    val settings: RegistrationSettings = RegistrationSettings(),
)

/** Host-configurable options shown on the Registration Setup screen. */
data class RegistrationSettings(
    val allowIndividual: Boolean = true,
    val allowTeam: Boolean = true,
    val minMembers: Int = MIN_TEAM_SIZE,
    val maxMembers: Int = 4,
    val quickFieldsSelected: Int = 0,
) {
    companion object {
        const val MIN_TEAM_SIZE = 2
        const val MAX_TEAM_SIZE = 20
    }
}
