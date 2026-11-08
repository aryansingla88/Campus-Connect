package com.example.campusconnect.feature.registrations.data

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.events.data.remote.EventsApi
import com.example.campusconnect.feature.registrations.ui.screens.FieldType
import com.example.campusconnect.feature.registrations.ui.screens.FormField
import com.example.campusconnect.feature.registrations.data.remote.CreateTeamRequest
import com.example.campusconnect.feature.registrations.data.remote.CreateInvitationRequest
import com.example.campusconnect.feature.registrations.data.remote.SaveAnswerDto
import com.example.campusconnect.feature.registrations.data.remote.SaveAnswersRequest
import com.example.campusconnect.feature.registrations.data.remote.TeamIdRequest
import com.example.campusconnect.feature.registrations.model.*
import retrofit2.Response

class NetworkUserRegistrationRepository(
    private val api: EventsApi = RetrofitClient.eventsApi,
) : UserRegistrationRepository {

    override suspend fun createRegistration(eventId: Int): Result<RegistrationDraft> = runCatching {
        val initialized = api.initializeParticipantRegistration(eventId).requireData("Could not start registration.")
        val registrationId = initialized.registrationId ?: error("The server returned no registration ID.")
        val config = api.getParticipantRegistrationConfig(eventId).requireData("Could not load event registration rules.")
        val questions = api.getParticipantRegistrationQuestions(eventId, true)
            .requireData("Could not load individual registration questions.")
            .mapNotNull { it.toQuestion() }
        val saved = api.getParticipantRegistration(eventId).requireData("Could not restore registration draft.")
        RegistrationDraft(
            registrationId = registrationId,
            questions = questions,
            initialAnswers = saved.answers.orEmpty()
                .filter { it.type.equals("INDIVIDUAL", true) }
                .mapNotNull { answer -> answer.fieldId?.let { id -> id to answer.answer.orEmpty() } }
                .toMap(),
            allowSolo = (config.minTeamMembers ?: 1) <= 1,
            allowTeam = (config.maxTeamMembers ?: 1) > 1,
        )
    }

    override suspend fun saveIndividualAnswers(registrationId: Int, answers: Map<Int, String>): Result<Unit> = runCatching {
        api.saveParticipantAnswers(registrationId, SaveAnswersRequest(
            type = "INDIVIDUAL",
            answers = answers.map { (id, value) -> SaveAnswerDto(id, value) },
        )).requireData("Could not save individual answers.")
        Unit
    }

    override suspend fun checkTeamName(eventId: Int, registrationId: Int, teamName: String): Result<Boolean> = runCatching {
        api.checkTeamNameAvailability(eventId, teamName.trim()).requireData("Could not check team name.")
    }

    override suspend fun createTeam(registrationId: Int, teamName: String): Result<TeamAssignment> = runCatching {
        val result = api.createParticipantTeam(registrationId, CreateTeamRequest(teamName.trim()))
            .requireData("Could not create the team.")
        TeamAssignment(
            registrationId = result.registrationId ?: error("The server returned no registration ID."),
            teamId = result.teamId ?: error("The server returned no team ID."),
        )
    }

    override suspend fun getTeams(eventId: Int, registrationId: Int): Result<TeamPortalData> = runCatching {
        val registration = api.getParticipantRegistration(eventId).requireData("Could not load registration.")
        val requests = api.getMyTeamRequests().requireData("Could not load team requests.")
        val invites = requests.filter {
            it.initiatedBy.equals("LEADER", true) && it.status.equals("PENDING", true)
        }.mapNotNull { request ->
            val id = request.requestId ?: return@mapNotNull null
            TeamInvite(
                id = id,
                teamName = request.teamName.orEmpty(),
                invitedBy = "Team leader",
                memberCount = 0,
                maxMembers = 0,
                sentAgo = request.createdAt.orEmpty(),
                teamId = request.teamId ?: return@mapNotNull null,
            )
        }
        val pendingRequests = requests.filter {
            it.initiatedBy.equals("PARTICIPANT", true) && it.status.equals("PENDING", true)
        }.mapNotNull { it.teamId }
        val assignedTeamId = registration.teamId
        if (assignedTeamId != null) {
            val team = api.getMyRegistrationTeam(eventId).requireData("Could not load your team.")
            val isLeader = team.currentUserIsLeader == true
            val candidates = if (isLeader && team.status.equals("PENDING", true)) {
                api.getTeamCandidates(eventId, "").requireData("Could not load invite candidates.")
                    .mapNotNull { c ->
                        val id = c.registrationId ?: return@mapNotNull null
                        TeamInviteCandidate(id, c.userId ?: return@mapNotNull null, c.displayName.orEmpty())
                    }
            } else emptyList()
            TeamPortalData(
                teams = emptyList(), invites = invites,
                currentTeam = TeamSummary(team.teamId ?: assignedTeamId, team.teamName.orEmpty(),
                    team.leaderName.orEmpty(), team.memberCount ?: 0, team.maxMembers ?: 0,
                    leaderUserId = team.leaderUserId, minMembers = team.minMembers ?: 1,
                    registrationStatus = team.status.orEmpty()),
                currentUserIsLeader = isLeader,
                members = team.members.orEmpty().mapNotNull { member ->
                    val regId = member.registrationId ?: return@mapNotNull null
                    TeamMemberSummary(regId, member.userId ?: return@mapNotNull null,
                        member.displayName.orEmpty(), member.status.orEmpty())
                },
                pendingJoinRequests = team.pendingJoinRequests.orEmpty().mapNotNull { req ->
                    val reqId = req.requestId ?: return@mapNotNull null
                    TeamJoinRequest(reqId, req.registrationId ?: return@mapNotNull null,
                        req.participantName.orEmpty())
                },
                inviteCandidates = candidates,
            )
        } else {
            val teams = api.getAvailableRegistrationTeams(eventId).requireData("Could not load available teams.")
                .mapNotNull { team ->
                    val id = team.teamId ?: return@mapNotNull null
                    TeamSummary(id, team.teamName.orEmpty(), team.leaderName.orEmpty(),
                        team.memberCount ?: 0, team.capacity ?: 0)
                }
            TeamPortalData(teams, invites, pendingTeamRequestIds = pendingRequests.toSet())
        }
    }

    override suspend fun requestToJoin(registrationId: Int, teamId: Int): Result<Unit> = runCatching {
        api.requestToJoinTeam(registrationId, TeamIdRequest(teamId)).requireData("Could not send team request.")
        Unit
    }

    override suspend fun respondToInvite(requestId: Int, accept: Boolean): Result<Unit> = runCatching {
        val response = if (accept) api.acceptTeamRequest(requestId) else api.rejectTeamRequest(requestId)
        response.requireData("Could not respond to invitation.")
        Unit
    }

    override suspend fun respondToJoinRequest(requestId: Int, accept: Boolean): Result<Unit> = runCatching {
        if (accept) api.acceptTeamRequest(requestId).requireData("Could not accept join request.")
        else api.rejectTeamRequest(requestId).requireData("Could not reject join request.")
        Unit
    }

    override suspend fun inviteParticipant(teamId: Int, registrationId: Int): Result<Unit> = runCatching {
        api.inviteParticipantToTeam(teamId, CreateInvitationRequest(registrationId))
            .requireData("Could not invite participant.")
        Unit
    }

    private fun com.example.campusconnect.feature.registrations.data.remote.ParticipantQuestionDto.toQuestion(): UserQuestion? {
        val id = id ?: return null
        val type = fieldType.orEmpty()
        val known = FieldType.entries.firstOrNull { it.name.equals(type, ignoreCase = true) }
        val field = FormField(
            id = id,
            backendId = id,
            label = fieldLabel.orEmpty(),
            fieldType = known ?: FieldType.TEXT,
            rawFieldType = if (known == null) type else null,
            placeholder = placeholder.orEmpty(),
            isRequired = required == true,
            options = options.orEmpty().sortedBy { it.optionOrder ?: 0 }.mapNotNull { it.optionValue },
            individual = individual ?: true,
            fieldOrder = fieldOrder ?: 0,
        )
        return field.toUserQuestion()
    }

    private fun <T : Any> Response<com.example.campusconnect.core.network.ApiResponse<T>>.requireData(
        fallback: String,
    ): T {
        val envelope = body()
        if (!isSuccessful || envelope?.success != true) {
            error(envelope?.message?.takeIf(String::isNotBlank) ?: "$fallback (HTTP ${this.code()}).")
        }
        return envelope?.data ?: error(envelope?.message?.takeIf(String::isNotBlank) ?: fallback)
    }
}
