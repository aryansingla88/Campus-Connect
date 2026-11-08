package com.example.campusconnect.feature.registrations.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.registrations.data.mapper.pendingJoinTeamIds
import com.example.campusconnect.feature.registrations.data.mapper.toMemberSummaries
import com.example.campusconnect.feature.registrations.data.mapper.toPendingInvites
import com.example.campusconnect.feature.registrations.data.mapper.toTeamInviteCandidateOrNull
import com.example.campusconnect.feature.registrations.data.mapper.toTeamJoinRequestOrNull
import com.example.campusconnect.feature.registrations.data.mapper.toTeamSummary
import com.example.campusconnect.feature.registrations.data.mapper.toTeamSummaryOrNull
import com.example.campusconnect.feature.registrations.data.remote.RegistrationApi
import com.example.campusconnect.feature.registrations.data.remote.requireRegistrationData
import com.example.campusconnect.feature.registrations.domain.repository.RegistrationRepository
import com.example.campusconnect.feature.registrations.domain.repository.TeamRepository
import com.example.campusconnect.feature.registrations.model.TeamAssignment
import com.example.campusconnect.feature.registrations.model.TeamJoinRequest
import com.example.campusconnect.feature.registrations.model.TeamPortalData
import com.example.campusconnect.feature.registrations.data.remote.dto.CreateInvitationRequest
import com.example.campusconnect.feature.registrations.data.remote.dto.CreateTeamRequest
import com.example.campusconnect.feature.registrations.data.remote.dto.TeamIdRequest

class NetworkTeamRepository(
    private val api: RegistrationApi = RetrofitClient.registrationApi,
    private val registrations: RegistrationRepository = NetworkRegistrationRepository(api),
) : TeamRepository {

    override suspend fun getTeamPortal(
        eventId: Int,
        registrationId: Int,
        search: String?,
    ): Result<TeamPortalData> = runCatching {
        val registration = registrations.getRegistration(eventId).getOrThrow()
        val requests = api.getMyTeamRequests()
            .requireRegistrationData("Could not load team requests.")
            // This endpoint returns the user's requests across all events. Its DTO has no event ID,
            // but registrationId identifies the request in this event-specific portal.
            .filter { it.registrationId == registrationId }
        val invites = requests.toPendingInvites()
        val pendingRequests = requests.pendingJoinTeamIds()
        val assignedTeamId = registration.teamId
        if (assignedTeamId != null) {
            val team = api.getMyRegistrationTeam(eventId).requireRegistrationData("Could not load your team.")
            val isLeader = team.currentUserIsLeader == true
            val candidates = if (isLeader && team.status.equals("PENDING", true)) {
                searchInviteCandidates(eventId, "").getOrThrow()
            } else emptyList()
            TeamPortalData(
                teams = emptyList(),
                invites = invites,
                currentTeam = team.toTeamSummary(assignedTeamId),
                currentUserIsLeader = isLeader,
                members = team.toMemberSummaries(),
                pendingJoinRequests = team.pendingJoinRequests.orEmpty().mapNotNull { it.toTeamJoinRequestOrNull() },
                inviteCandidates = candidates,
            )
        } else {
            val teams = api.getAvailableRegistrationTeams(eventId, search)
                .requireRegistrationData("Could not load available teams.")
                .mapNotNull { it.toTeamSummaryOrNull() }
            TeamPortalData(teams, invites, pendingTeamRequestIds = pendingRequests)
        }
    }

    override suspend fun checkTeamName(
        eventId: Int,
        registrationId: Int,
        teamName: String,
    ): Result<Boolean> = runCatching {
        api.checkTeamNameAvailability(eventId, teamName.trim())
            .requireRegistrationData("Could not check team name.")
    }

    override suspend fun createTeam(registrationId: Int, teamName: String): Result<TeamAssignment> = runCatching {
        val created = api.createParticipantTeam(registrationId, CreateTeamRequest(teamName.trim()))
            .requireRegistrationData("Could not create the team.")
        TeamAssignment(
            registrationId = created.registrationId ?: error("The server returned no registration ID."),
            teamId = created.teamId ?: error("The server returned no team ID."),
        )
    }

    override suspend fun requestToJoin(registrationId: Int, teamId: Int): Result<Unit> = runCatching {
        api.requestToJoinTeam(registrationId, TeamIdRequest(teamId))
            .requireRegistrationData("Could not send team request.")
        Unit
    }

    override suspend fun respondToInvite(requestId: Int, accept: Boolean): Result<Unit> = respond(requestId, accept)

    override suspend fun getTeamJoinRequests(teamId: Int): Result<List<TeamJoinRequest>> = runCatching {
        api.getTeamJoinRequests(teamId).requireRegistrationData("Could not load team requests.")
            .mapNotNull { it.toTeamJoinRequestOrNull() }
    }

    override suspend fun respondToJoinRequest(requestId: Int, accept: Boolean): Result<Unit> = respond(requestId, accept)

    private suspend fun respond(requestId: Int, accept: Boolean): Result<Unit> = runCatching {
        val response = if (accept) api.acceptTeamRequest(requestId) else api.rejectTeamRequest(requestId)
        response.requireRegistrationData("Could not respond to team request.")
        Unit
    }

    override suspend fun cancelTeamRequest(requestId: Int): Result<Unit> = runCatching {
        api.cancelTeamRequest(requestId).requireRegistrationData("Could not cancel team request.")
        Unit
    }

    override suspend fun inviteParticipant(teamId: Int, registrationId: Int): Result<Unit> = runCatching {
        api.inviteParticipantToTeam(teamId, CreateInvitationRequest(registrationId))
            .requireRegistrationData("Could not invite participant.")
        Unit
    }

    override suspend fun searchInviteCandidates(eventId: Int, query: String) = runCatching {
        api.getTeamCandidates(eventId, query).requireRegistrationData("Could not load invite candidates.")
            .mapNotNull { it.toTeamInviteCandidateOrNull() }
    }
}
