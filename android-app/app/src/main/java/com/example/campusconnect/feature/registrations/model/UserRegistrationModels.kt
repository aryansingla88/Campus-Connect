package com.example.campusconnect.feature.registrations.model

const val USER_REGISTRATION_TOTAL_STEPS = 3

/** Screens of the participant-side registration flow, with the progress shown for each. */
enum class UserRegistrationStep(val progress: Int?) {
    CREATING(null),
    DETAILS(1),
    PARTICIPATION(2),
    TEAM_PORTAL(3),
    SUBMISSION(3),
}

enum class ParticipationMode { CREATE_TEAM, JOIN_TEAM, SOLO }

/** Small, stable identity for one participant registration journey. */
data class RegistrationSession(
    val eventId: Int,
    val registrationId: Int,
    val mode: ParticipationMode,
    val teamId: Int? = null,
    val progress: UserRegistrationStep = UserRegistrationStep.DETAILS,
)

/** Result of "Creating registration": the questions plus the event's participation rules. */
data class RegistrationDraft(
    val registrationId: Int,
    val questions: List<RegistrationQuestion>,
    /** Answers prefilled from the participant's profile, by question id. */
    val initialAnswers: Map<Int, String> = emptyMap(),
    val allowSolo: Boolean = true,
    val allowTeam: Boolean = true,
)

data class TeamAssignment(val registrationId: Int, val teamId: Int)

data class TeamSummary(
    val id: Int,
    val name: String,
    val leaderName: String,
    val memberCount: Int,
    val maxMembers: Int,
    val tags: List<String> = emptyList(),
    val leaderUserId: Int? = null,
    val minMembers: Int = 1,
    val registrationStatus: String = "PENDING",
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
    val currentTeam: TeamSummary? = null,
    val currentUserIsLeader: Boolean = false,
    val members: List<TeamMemberSummary> = emptyList(),
    val pendingJoinRequests: List<TeamJoinRequest> = emptyList(),
    val inviteCandidates: List<TeamInviteCandidate> = emptyList(),
    val pendingTeamRequestIds: Set<Int> = emptySet(),
)

data class TeamMemberSummary(val registrationId: Int, val userId: Int, val displayName: String, val status: String)
data class TeamJoinRequest(val requestId: Int, val registrationId: Int, val participantName: String)
data class TeamInviteCandidate(val registrationId: Int, val userId: Int, val displayName: String)
