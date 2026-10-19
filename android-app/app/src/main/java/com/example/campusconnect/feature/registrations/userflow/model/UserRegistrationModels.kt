package com.example.campusconnect.feature.registrations.userflow.model

import com.example.campusconnect.feature.registrations.ui.screens.FieldType
import com.example.campusconnect.feature.registrations.ui.screens.FormField

const val USER_REGISTRATION_TOTAL_STEPS = 3

/** Screens of the participant-side registration flow, with the progress shown for each. */
enum class UserRegistrationStep(val progress: Int?) {
    CREATING(null),
    DETAILS(1),
    PARTICIPATION(1),
    TEAM_PORTAL(2),
}

enum class ParticipationMode { CREATE_TEAM, JOIN_TEAM, SOLO }

enum class UserFieldKind { TEXT, LONG_TEXT, PHONE, NUMBER, DROPDOWN, MULTISELECT, DATE, FILE }

/**
 * One question as the participant sees it, for both the individual and the team step.
 * Backend questions arrive as [FormField]s and are converted with [toUserQuestion], so both
 * steps render through the same input components.
 */
data class UserQuestion(
    val id: Int,
    val label: String,
    val kind: UserFieldKind = UserFieldKind.TEXT,
    val required: Boolean = false,
    /** Prefilled from the profile and not editable (e.g. email). */
    val readOnly: Boolean = false,
    val placeholder: String = "",
    val options: List<String> = emptyList(),
    /** Character limit for [UserFieldKind.TEXT] / [UserFieldKind.LONG_TEXT] answers. */
    val maxLength: Int = 300,
    /** Size limit for [UserFieldKind.FILE] answers. */
    val maxFileBytes: Long = 10L * 1024 * 1024,
)

/** A required question is missing when it has no answer (file questions are answered by an attachment). */
fun UserQuestion.isMissing(answer: String?, attachment: AttachedFile? = null): Boolean =
    required && !readOnly && if (kind == UserFieldKind.FILE) attachment == null else answer.isNullOrBlank()

/** Result of "Creating registration": the questions plus the event's participation rules. */
data class RegistrationDraft(
    val registrationId: Int,
    val questions: List<UserQuestion>,
    /** Answers prefilled from the participant's profile, by question id. */
    val initialAnswers: Map<Int, String> = emptyMap(),
    val allowSolo: Boolean = true,
    val allowTeam: Boolean = true,
)

data class TeamSummary(
    val id: Int,
    val name: String,
    val leaderName: String,
    val memberCount: Int,
    val maxMembers: Int,
    val tags: List<String> = emptyList(),
) {
    val isFull: Boolean get() = memberCount >= maxMembers
}

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
)

data class TeamPortalData(
    val teams: List<TeamSummary>,
    val invites: List<TeamInvite>,
)

/**
 * Converts a backend question ([FormField], same shape as the organiser-side builder) into the
 * participant-side model. Field types the builder doesn't know arrive in [FormField.rawFieldType];
 * the names matched below are assumptions to align with the backend's actual type strings.
 */
fun FormField.toUserQuestion(readOnly: Boolean = false): UserQuestion = UserQuestion(
    id = id,
    label = label,
    kind = rawFieldType?.let(::kindFromRawType) ?: when (fieldType) {
        FieldType.TEXT -> UserFieldKind.TEXT
        FieldType.NUMBER -> UserFieldKind.NUMBER
        FieldType.SELECT -> UserFieldKind.DROPDOWN
        FieldType.MULTISELECT -> UserFieldKind.MULTISELECT
        FieldType.DATE -> UserFieldKind.DATE
    },
    required = isRequired,
    readOnly = readOnly,
    placeholder = placeholder,
    options = options,
)

private fun kindFromRawType(raw: String): UserFieldKind? = when (raw.trim().uppercase()) {
    "TEXTAREA", "LONG_TEXT", "PARAGRAPH" -> UserFieldKind.LONG_TEXT
    "FILE", "FILE_UPLOAD" -> UserFieldKind.FILE
    "PHONE" -> UserFieldKind.PHONE
    else -> null
}
