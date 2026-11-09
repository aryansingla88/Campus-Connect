package com.example.campusconnect.feature.registrations.domain.repository

import com.example.campusconnect.feature.registrations.model.TeamAssignment
import com.example.campusconnect.feature.registrations.model.TeamInviteCandidate
import com.example.campusconnect.feature.registrations.model.TeamJoinRequest
import com.example.campusconnect.feature.registrations.model.TeamPortalData
import com.example.campusconnect.feature.registrations.model.TeamSummary

/** Team discovery, invitations, membership and leader-side actions. */
interface TeamRepository {
    suspend fun getTeamPortal(eventId: Int, registrationId: Int, search: String? = null): Result<TeamPortalData>
    suspend fun checkTeamName(eventId: Int, registrationId: Int, teamName: String): Result<Boolean>
    suspend fun createTeam(registrationId: Int, teamName: String): Result<TeamAssignment>
    suspend fun requestToJoin(registrationId: Int, teamId: Int): Result<Unit>
    suspend fun respondToInvite(requestId: Int, accept: Boolean): Result<Unit>
    suspend fun getTeamJoinRequests(teamId: Int): Result<List<TeamJoinRequest>>
    suspend fun respondToJoinRequest(requestId: Int, accept: Boolean): Result<Unit>
    suspend fun cancelTeamRequest(requestId: Int): Result<Unit>
    suspend fun inviteParticipant(teamId: Int, registrationId: Int): Result<Unit>
    suspend fun searchInviteCandidates(eventId: Int, query: String): Result<List<TeamInviteCandidate>>
}
