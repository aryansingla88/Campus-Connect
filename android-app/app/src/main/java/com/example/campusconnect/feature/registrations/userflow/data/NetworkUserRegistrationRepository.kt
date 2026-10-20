package com.example.campusconnect.feature.registrations.userflow.data

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.registrations.data.mapper.RegistrationFieldMapper
import com.example.campusconnect.feature.registrations.data.remote.RegistrationApi
import com.example.campusconnect.feature.registrations.data.remote.ParticipantRegistrationResponse
import com.example.campusconnect.feature.registrations.data.remote.TeamRequestDto
import com.example.campusconnect.feature.registrations.userflow.model.*
import com.example.campusconnect.feature.registrations.model.RegistrationDetailResponse
import com.example.campusconnect.feature.registrations.model.RegistrationMemberDetail
import com.example.campusconnect.feature.registrations.model.RegistrationResponseItem
import com.example.campusconnect.feature.registrations.model.RegistrationResponseStatus
import com.example.campusconnect.feature.registrations.model.RegistrationResponseType
import com.example.campusconnect.feature.registrations.model.ResponseAnswer
import retrofit2.Response
import com.example.campusconnect.core.network.ApiResponse
import java.time.Instant
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/** Participant repository. The backend derives identity from the bearer token; test persona IDs are ignored. */
class NetworkUserRegistrationRepository(
    private val api: RegistrationApi = RetrofitClient.registrationApi,
) : UserRegistrationRepository {
    private val activeRegistrationByEvent = ConcurrentHashMap<Int, Int>()
    private val participantAnswersByEvent = ConcurrentHashMap<Int, Map<Int, String>>()
    private val teamByEvent = ConcurrentHashMap<Int, Int>()

    override fun selectParticipant(userId: Int?) = Unit

    override suspend fun createRegistration(eventId: Int): Result<RegistrationDraft> = runCatching {
        val existing = api.getParticipantRegistration(eventId).body()?.takeIf { it.success }?.data
        val response = (if (existing?.registrationId != null && existing.canResume == true) existing
            else requireData(api.initializeParticipantRegistration(eventId), "Could not start registration."))
            ?: error("The server returned no registration.")
        val registrationId = response.registrationId ?: error("The server did not return a registration ID.")
        activeRegistrationByEvent[eventId] = registrationId
        response.teamId?.let { teamByEvent[eventId] = it } ?: teamByEvent.remove(eventId)
        participantAnswersByEvent[eventId] = response.answers
            .filter { it.type.equals("INDIVIDUAL", ignoreCase = true) }
            .associate { it.fieldId to it.answer }
        val questions = requireData(api.getParticipantQuestions(eventId, true), "Could not load individual questions.")
            .orEmpty().sortedBy { it.fieldOrder }.map(RegistrationFieldMapper::toModel)
        RegistrationDraft(
            registrationId, questions, participantAnswersByEvent.getValue(eventId),
            teamId = response.teamId, teamLeader = response.teamLeader == true, teamName = response.teamName,
        )
    }

    override suspend fun isTeamNameAvailable(eventId: Int, name: String): Result<Boolean> = runCatching {
        requireData(api.isTeamNameAvailable(eventId, name.trim()), "Could not check team name availability.") ?: false
    }

    override suspend fun createTeam(eventId: Int, name: String): Result<Unit> = runCatching {
        val response = requireData(api.createParticipantTeam(
            registrationId(eventId), com.example.campusconnect.feature.registrations.data.remote.CreateParticipantTeamRequest(name.trim()),
        ), "Could not create team.") ?: error("The server returned no team registration.")
        teamByEvent[eventId] = response.teamId ?: error("The server did not return the new team ID.")
    }

    override suspend fun getTeams(eventId: Int): Result<TeamPortalData> = runCatching {
        val registration = requireData(api.getParticipantRegistration(eventId), "Could not load your registration.")
        registration?.registrationId?.let { activeRegistrationByEvent[eventId] = it }
        val teams = if (registration?.teamId == null) requireData(api.getAvailableTeams(eventId), "Could not load available teams.").orEmpty()
            .map { TeamSummary(it.teamId, it.teamName, it.leaderName, it.memberCount, it.capacity) } else emptyList()
        val requests = requireData(api.getMyTeamRequests(), "Could not load team requests.").orEmpty()
        val mine = requests.filter { it.registrationId == registration?.registrationId && it.status == "PENDING" }
        val incomingInvites = mine.filter { it.initiatedBy.equals("LEADER", true) }.map { it.toInvite() }
        val pendingJoinRequests = mine.filter { it.initiatedBy.equals("PARTICIPANT", true) }
        val teamId = registration?.teamId
        val leaderRequests = if (teamId == null) emptyList() else requireData(
            api.getTeamRequests(teamId), "Could not load your team's requests.",
        ).orEmpty()
        val members = if (teamId == null) emptyList() else requireData(
            api.getTeamMembers(teamId), "Could not load team members.",
        ).orEmpty().map { TeamMember(it.userId, it.name, it.email.orEmpty(), if (it.role.equals("LEADER", true)) TeamRole.LEADER else TeamRole.MEMBER, it.program.orEmpty(), it.registrationId) }
        val candidates = if (teamId == null) emptyList() else requireData(
            api.getTeamInviteCandidates(teamId), "Could not load invitation candidates.",
        ).orEmpty().map { TeamInviteCandidate(it.registrationId, it.participantId, it.name, it.email.orEmpty(), it.program.orEmpty()) }
        val myTeam = if (teamId == null) null else {
            val summary = requireData(api.getTeamSummary(teamId), "Could not load your team details.")
            TeamSummary(
                teamId, registration.teamName.orEmpty(), summary?.leaderName.orEmpty(),
                summary?.memberCount ?: members.size, summary?.capacity ?: members.size.coerceAtLeast(1), members = members,
            )
        }
        val outgoingInvites = leaderRequests.filter { it.initiatedBy.equals("LEADER", true) && it.status == "PENDING" }
            .map { TeamInvitation(it.requestId, it.teamId, it.participantName, it.createdAt.orRelativeTime(), it.registrationId, it.participantName) }
        val incomingLeaderRequests = leaderRequests.filter { it.initiatedBy.equals("PARTICIPANT", true) && it.status == "PENDING" }
            .map { TeamJoinRequest(it.requestId, it.teamId, it.participantName, "", it.createdAt.orRelativeTime(), it.participantId) }
        val ownPending = pendingJoinRequests.map { it.teamId }.toSet()
        val role = if (myTeam != null && members.any { it.role == TeamRole.LEADER && it.registrationId == registration?.registrationId }) TeamRole.LEADER
            else myTeam?.let { TeamRole.MEMBER }
        TeamPortalData(
            teams = teams,
            invites = incomingInvites,
            myTeam = myTeam,
            myTeamRole = role,
            members = members,
            incomingJoinRequests = incomingLeaderRequests,
            outgoingInvites = outgoingInvites,
            membershipStatus = when {
                role == TeamRole.LEADER -> TeamMembershipStatus.LEADER
                myTeam != null -> TeamMembershipStatus.MEMBER
                incomingInvites.isNotEmpty() -> TeamMembershipStatus.INVITED
                ownPending.isNotEmpty() -> TeamMembershipStatus.REQUEST_PENDING
                else -> TeamMembershipStatus.NONE
            },
            pendingTeam = ownPending.firstOrNull()?.let { id -> teams.firstOrNull { it.id == id } },
            pendingTeamIds = ownPending,
            inviteCandidates = candidates,
        )
    }

    override suspend fun requestToJoin(eventId: Int, teamId: Int): Result<Unit> = callUnit {
        api.createTeamJoinRequest(registrationId(eventId), com.example.campusconnect.feature.registrations.data.remote.TeamRequestCreateDto(teamId))
    }

    override suspend fun cancelJoinRequest(eventId: Int, teamId: Int): Result<Unit> = runCatching {
        val found = requireData(api.getMyTeamRequests(), "Could not load your requests.").orEmpty()
            .firstOrNull { it.registrationId == registrationId(eventId) && it.teamId == teamId && it.status == "PENDING" }
            ?: return@runCatching Unit
        requireData(api.cancelTeamRequest(found.requestId), "Could not cancel the request.")
    }

    override suspend fun respondToInvite(eventId: Int, inviteId: Int, accept: Boolean): Result<Unit> = callUnit {
        if (accept) api.acceptTeamRequest(inviteId) else api.rejectTeamRequest(inviteId)
    }

    override suspend fun respondToJoinRequest(eventId: Int, requestId: Int, accept: Boolean): Result<Unit> = callUnit {
        if (accept) api.acceptTeamRequest(requestId) else api.rejectTeamRequest(requestId)
    }

    override suspend fun inviteMember(eventId: Int, registrationId: Int): Result<Unit> = runCatching {
        val current = requireData(api.getParticipantRegistration(eventId), "Could not load your registration.")
            ?: error("Your registration is unavailable.")
        val teamId = current.teamId ?: error("Create a team before inviting participants.")
        requireData(api.createTeamInvitation(teamId, com.example.campusconnect.feature.registrations.data.remote.TeamInvitationCreateDto(registrationId)), "Could not send invitation.")
    }

    override suspend fun cancelInvitation(eventId: Int, invitationId: Int): Result<Unit> = callUnit {
        api.cancelTeamRequest(invitationId)
    }

    suspend fun saveAnswers(eventId: Int, type: String, answers: Map<Int, String>): Result<Unit> = runCatching {
        val id = registrationId(eventId)
        requireData(api.saveParticipantAnswers(id, com.example.campusconnect.feature.registrations.data.remote.ParticipantAnswersRequest(
            type, answers.map { com.example.campusconnect.feature.registrations.data.remote.ParticipantAnswerDto(it.key, it.value) },
        )), "Could not save answers.")
        if (type == "INDIVIDUAL") participantAnswersByEvent[eventId] = answers
    }

    fun hasCreatedTeam(eventId: Int): Boolean = teamByEvent.containsKey(eventId)

    suspend fun finalize(eventId: Int): Result<ParticipantRegistrationResponse> = runCatching {
        requireData(api.finalizeParticipantRegistration(registrationId(eventId)), "Could not finalize registration.")
            ?: error("The server returned no registration.")
    }

    suspend fun cancelRegistration(eventId: Int): Result<Unit> = runCatching {
        requireData(api.cancelParticipantRegistration(eventId), "Could not cancel registration.")
        activeRegistrationByEvent.remove(eventId)
        participantAnswersByEvent.remove(eventId)
        teamByEvent.remove(eventId)
    }

    suspend fun getOwnedDetails(eventId: Int, expectedRegistrationId: Int): Result<Pair<RegistrationResponseItem, RegistrationDetailResponse>> = runCatching {
        val registration = requireData(api.getParticipantRegistration(eventId), "Could not load your registration.")
            ?: error("No registration exists for this event.")
        if (registration.registrationId != expectedRegistrationId) error("The registration is unavailable to this participant.")
        val individualFields = requireData(api.getParticipantQuestions(eventId, true), "Could not load registration questions.").orEmpty()
        val sharedFields = requireData(api.getParticipantQuestions(eventId, false), "Could not load registration questions.").orEmpty()
        val labels = (individualFields + sharedFields).associateBy { it.id }
        val answers = registration.answers.map { answer ->
            val field = labels[answer.fieldId]
            ResponseAnswer(answer.fieldId, field?.fieldLabel ?: "Question ${answer.fieldId}", field?.fieldType, answer.answer)
        }
        val detail = RegistrationDetailResponse(
            teamAnswers = answers.filter { registration.answers.firstOrNull { saved -> saved.fieldId == it.fieldId }?.type == "TEAM" },
            members = listOf(RegistrationMemberDetail(
                userId = 0,
                individualAnswers = answers.filter { registration.answers.firstOrNull { saved -> saved.fieldId == it.fieldId }?.type != "TEAM" },
            )),
        )
        val type = if (registration.teamId == null) RegistrationResponseType.INDIVIDUAL else RegistrationResponseType.TEAM
        val status = when (registration.status?.uppercase()) {
            "CONFIRMED" -> RegistrationResponseStatus.CONFIRMED
            "PENDING" -> RegistrationResponseStatus.PENDING
            "WAITLISTED" -> RegistrationResponseStatus.WAITLISTED
            "REJECTED" -> RegistrationResponseStatus.REJECTED
            else -> RegistrationResponseStatus.UNKNOWN
        }
        RegistrationResponseItem(
            id = expectedRegistrationId, name = "Registrant", email = null, type = type,
            status = status, submittedAt = null, teamName = registration.teamName,
        ) to detail
    }

    fun registrationId(eventId: Int): Int = activeRegistrationByEvent[eventId]
        ?: error("Start or resume registration before continuing.")

    private suspend fun <T> callUnit(block: suspend () -> Response<ApiResponse<T>>): Result<Unit> = runCatching {
        requireData(block(), "The request could not be completed.")
        Unit
    }

    private fun TeamRequestDto.toInvite() = TeamInvite(
        requestId, teamName, participantName, 0, 0, createdAt.orRelativeTime(), teamId = teamId, participantId = participantId,
    )

    private fun String?.orRelativeTime(): String = runCatching {
        val instant = Instant.parse(this)
        val minutes = Duration.between(instant, Instant.now()).toMinutes().coerceAtLeast(0)
        when { minutes < 1 -> "Just now"; minutes < 60 -> "$minutes min ago"; minutes < 1440 -> "${minutes / 60} hrs ago"; else -> "${minutes / 1440} days ago" }
    }.getOrDefault("Recently")
}

private suspend fun <T> requireData(response: Response<ApiResponse<T>>, fallback: String): T? {
    val body = response.body()
    if (!response.isSuccessful || body?.success != true) {
        error(body?.message?.takeIf(String::isNotBlank) ?: "$fallback (HTTP ${response.code()}).")
    }
    return body.data
}
