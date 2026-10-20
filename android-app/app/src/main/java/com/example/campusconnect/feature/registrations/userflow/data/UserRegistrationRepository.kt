package com.example.campusconnect.feature.registrations.userflow.data

import com.example.campusconnect.feature.registrations.userflow.model.RegistrationDraft
import com.example.campusconnect.feature.registrations.userflow.model.TeamPortalData

/**
 * Participant-side registration operations. Implement against the real backend;
 * [FakeUserRegistrationRepository] stands in until the endpoints are available.
 */
interface UserRegistrationRepository {
    fun selectParticipant(userId: Int?)
    suspend fun createRegistration(eventId: Int): Result<RegistrationDraft>
    suspend fun isTeamNameAvailable(eventId: Int, name: String): Result<Boolean>
    suspend fun createTeam(eventId: Int, name: String): Result<Unit>
    suspend fun getTeams(eventId: Int): Result<TeamPortalData>
    suspend fun requestToJoin(eventId: Int, teamId: Int): Result<Unit>
    suspend fun cancelJoinRequest(eventId: Int, teamId: Int): Result<Unit>
    suspend fun respondToInvite(eventId: Int, inviteId: Int, accept: Boolean): Result<Unit>
    suspend fun respondToJoinRequest(eventId: Int, requestId: Int, accept: Boolean): Result<Unit>
    suspend fun inviteMember(eventId: Int, registrationId: Int): Result<Unit>
    suspend fun cancelInvitation(eventId: Int, invitationId: Int): Result<Unit>
}
