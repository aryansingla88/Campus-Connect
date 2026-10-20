package com.example.campusconnect.feature.registrations.userflow.model

import com.example.campusconnect.feature.registrations.ui.screens.FormField
import com.example.campusconnect.feature.registrations.ui.screens.FieldType
import java.time.LocalDate

const val USER_REGISTRATION_TOTAL_STEPS = 3
const val TEAM_NAME_MIN_LENGTH = 3
const val TEAM_NAME_MAX_LENGTH = 100

fun teamNameLengthError(name: String): String? {
    val length = name.trim().length
    return when {
        length < TEAM_NAME_MIN_LENGTH -> "Use at least $TEAM_NAME_MIN_LENGTH characters."
        length > TEAM_NAME_MAX_LENGTH -> "Use no more than $TEAM_NAME_MAX_LENGTH characters."
        else -> null
    }
}

/** Screens of the participant-side registration flow, with the progress shown for each. */
enum class UserRegistrationStep(val progress: Int?) {
    CREATING(null),
    DETAILS(1),
    PARTICIPATION(2),
    TEAM_PORTAL(2),
}

enum class ParticipationMode { CREATE_TEAM, JOIN_TEAM, SOLO }

enum class TeamRole { LEADER, MEMBER }
enum class TeamMembershipStatus { NONE, REQUEST_PENDING, REQUEST_REJECTED, INVITED, INVITE_REJECTED, MEMBER, LEADER }

/** Result of "Creating registration": the questions plus the event's participation rules. */
data class RegistrationDraft(
    val registrationId: Int,
    val questions: List<FormField>,
    /** Answers prefilled from the participant's profile, by question id. */
    val initialAnswers: Map<Int, String> = emptyMap(),
    val allowSolo: Boolean = true,
    val allowTeam: Boolean = true,
    val teamId: Int? = null,
    val teamLeader: Boolean = false,
    val teamName: String? = null,
)

data class TeamSummary(
    val id: Int,
    val name: String,
    val leaderName: String,
    val memberCount: Int,
    val maxMembers: Int,
    val tags: List<String> = emptyList(),
    /** Members shown in the team overview popup; empty when the backend doesn't send them. */
    val members: List<TeamMember> = emptyList(),
) {
    val isFull: Boolean get() = memberCount >= maxMembers
}

data class TeamMember(
    val id: Int,
    val name: String,
    val email: String,
    val role: TeamRole = TeamRole.MEMBER,
    /** Course and graduation year shown under the name, e.g. "MCA · 2027". */
    val program: String = "",
    val registrationId: Int? = null,
)

data class TeamJoinRequest(
    val id: Int,
    val teamId: Int,
    val participantName: String,
    val email: String,
    val requestedAt: String = "Just now",
    val participantId: Int = -1,
    val program: String = "",
)

data class TeamInvitation(
    val id: Int,
    val teamId: Int,
    val email: String,
    val sentAt: String = "Just now",
    val registrationId: Int = -1,
    val participantName: String = email,
    val program: String = "",
)

data class TeamInvite(
    val id: Int,
    val teamName: String,
    val invitedBy: String,
    val memberCount: Int,
    val maxMembers: Int,
    /** Relative send time shown on the card, e.g. "2 hrs ago". */
    val sentAgo: String = "",
    /** Names of current members, used for the avatar stack. */
    val memberNames: List<String> = emptyList(),
    /** Team identifier used to continue registration after an invitation is accepted. */
    val teamId: Int = id,
    val participantId: Int = -1,
    /** Skill tags of the inviting team, shown as chips on the invitation card. */
    val tags: List<String> = emptyList(),
)

data class TeamInviteCandidate(
    val registrationId: Int,
    val participantId: Int,
    val name: String,
    val email: String,
    val program: String = "",
)

data class TeamPortalData(
    val teams: List<TeamSummary>,
    val invites: List<TeamInvite>,
    val eventName: String = "Campus Connect Demo Event",
    val myTeam: TeamSummary? = null,
    val myTeamRole: TeamRole? = null,
    val members: List<TeamMember> = emptyList(),
    val incomingJoinRequests: List<TeamJoinRequest> = emptyList(),
    val outgoingInvites: List<TeamInvitation> = emptyList(),
    val membershipStatus: TeamMembershipStatus = TeamMembershipStatus.NONE,
    val pendingTeam: TeamSummary? = null,
    /** The participant's open join request, if any. The backend permits only one pending request per registration. */
    val pendingTeamIds: Set<Int> = emptySet(),
    val inviteCandidates: List<TeamInviteCandidate> = emptyList(),
)

/**
 * Converts a backend question ([FormField], same shape as the organiser-side builder) into the
 * participant-side model. Field types the builder doesn't know arrive in [FormField.rawFieldType];
 * the names matched below are assumptions to align with the backend's actual type strings.
 */
/** Validation follows the host FormField type, required flag, and configured options. */
fun FormField.validationError(answer: String?): String? {
    val value = answer.orEmpty()
    if (readOnly) return null
    if (isRequired && value.isBlank()) return "This field is required"
    if (value.isBlank()) return null
    return when (fieldType) {
        FieldType.TEXT -> null
        FieldType.NUMBER -> if (runCatching { java.math.BigDecimal(value) }.isFailure) "Enter a valid number" else null
        FieldType.SELECT -> if (options.isNotEmpty() && value !in options) "Choose an available option" else null
        FieldType.MULTISELECT -> {
            val selected = value.split(',', '\n').map(String::trim).filter(String::isNotBlank)
            if (options.isNotEmpty() && selected.any { it !in options }) "Choose only available options" else null
        }
        FieldType.DATE -> if (runCatching { LocalDate.parse(value) }.isFailure) "Choose a valid date" else null
    }
}

/** Shared answer/error state helpers used by both participant question steps. */
data class QuestionAnswerState(
    val answers: Map<Int, String>,
    val validationErrors: Map<Int, String>,
)

fun List<FormField>.updateAnswer(
    answers: Map<Int, String>,
    validationErrors: Map<Int, String>,
    fieldId: Int,
    value: String,
): QuestionAnswerState {
    val nextAnswers = answers + (fieldId to value)
    val error = firstOrNull { it.id == fieldId }?.validationError(value)
    val nextErrors = if (error == null) validationErrors - fieldId else validationErrors + (fieldId to error)
    return QuestionAnswerState(nextAnswers, nextErrors)
}

fun List<FormField>.validateAnswers(answers: Map<Int, String>): Map<Int, String> =
    mapNotNull { field -> field.validationError(answers[field.id])?.let { field.id to it } }.toMap()
