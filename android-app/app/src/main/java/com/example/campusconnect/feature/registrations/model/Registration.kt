package com.example.campusconnect.feature.registrations.model

/** Host-configurable options shown on the Registration Setup screen. */
data class RegistrationSettings(
    val minMembers: Int = 1,
    val maxMembers: Int = 4,
    val quickFieldMask: Long = 0L,
    val autoConfirm: Boolean = true,
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

data class RegistrationParticipationRules(
    val allowSolo: Boolean,
    val allowTeam: Boolean,
)

data class RegistrationSnapshot(
    val eventId: Int,
    val registrationId: Int?,
    val status: String?,
    val teamId: Int?,
    val teamName: String?,
    val canResume: Boolean,
    val individualAnswers: Map<Int, String> = emptyMap(),
    val teamAnswers: Map<Int, String> = emptyMap(),
)

/** Minimal event context needed by organizer registration setup. */
data class RegistrationEventHeader(
    val title: String,
    val isPublished: Boolean,
)

sealed interface RegistrationConfigLoadResult {
    data class Existing(val settings: RegistrationSettings) : RegistrationConfigLoadResult
    data object Missing : RegistrationConfigLoadResult
    data class Failure(val message: String) : RegistrationConfigLoadResult
}

/** Stable bit mapping shared by the setup UI and registration data layer. */
object RegistrationQuickFields {
    private val bits = linkedMapOf(
        "full_name" to (1L shl 0), "email" to (1L shl 1), "phone" to (1L shl 2),
        "course" to (1L shl 3), "year_batch" to (1L shl 4), "roll_number" to (1L shl 5),
        "hostel" to (1L shl 6), "hometown" to (1L shl 7), "gender" to (1L shl 8),
        "date_of_birth" to (1L shl 9), "github" to (1L shl 10), "linkedin" to (1L shl 11),
    )

    fun ids(mask: Long): Set<String> = bits.filterValues { bit -> (mask and bit) != 0L }.keys

    fun mask(ids: Set<String>): Long = ids.mapNotNull(bits::get).fold(0L) { result, bit -> result or bit }
}
