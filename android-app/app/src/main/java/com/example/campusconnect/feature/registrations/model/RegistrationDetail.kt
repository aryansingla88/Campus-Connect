package com.example.campusconnect.feature.registrations.model

/**
 * Detailed-response payload for one registration.
 *
 * - [teamAnswers]: shared answers. For a team these come from the leader's
 *   registration; for a solo registration they are that registration's
 *   shared-question answers.
 * - [members]: registered participants. Exactly one entry for an individual
 *   registration. For a team the first entry is treated as the leader
 *   (see [RegistrationDetailResponse.leaderUserId]).
 */
data class RegistrationDetailResponse(
    val teamAnswers: List<ResponseAnswer> = emptyList(),
    val members: List<RegistrationMemberDetail> = emptyList(),
) {
    /** The payload carries no leader flag, so the first member is the leader. */
    val leaderUserId: Int? get() = members.firstOrNull()?.userId

    val isEmpty: Boolean
        get() = teamAnswers.isEmpty() &&
                members.all { it.quickFields.isEmpty() && it.individualAnswers.isEmpty() }
}

data class RegistrationMemberDetail(
    val userId: Int,
    val quickFields: List<QuickFieldValue> = emptyList(),
    val individualAnswers: List<ResponseAnswer> = emptyList(),
)

data class QuickFieldValue(
    val field: String,
    val value: String,
)

data class ResponseAnswer(
    val fieldId: Int?,
    val fieldLabel: String?,
    val fieldType: String?,
    val answer: String?,
)

/** Display name for a member tab: first word of the "Full Name" quick field. */
fun RegistrationMemberDetail.shortName(position: Int): String =
    fullName()?.substringBefore(' ') ?: "Member ${position + 1}"

fun RegistrationMemberDetail.fullName(): String? =
    quickFields
        .firstOrNull { it.field.replace("_", " ").trim().equals("full name", ignoreCase = true) }
        ?.value
        ?.takeIf { it.isNotBlank() }
