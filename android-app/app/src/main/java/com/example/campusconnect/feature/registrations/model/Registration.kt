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
    val minMembers: Int = 1,
    val maxMembers: Int = 4,
    val quickFieldMask: Long = 0L,
    val registrationCreatedAt: String? = null,
    val deadline: Long? = null,
) {
    val allowIndividual: Boolean
        get() = minMembers == 1

    val allowTeam: Boolean
        get() = maxMembers > 1

    companion object {
        const val MIN_TEAM_SIZE = 2
        const val MAX_TEAM_SIZE = 20
    }
}
