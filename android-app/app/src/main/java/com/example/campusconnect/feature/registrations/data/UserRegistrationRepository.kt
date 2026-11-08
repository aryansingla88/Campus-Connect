package com.example.campusconnect.feature.registrations.data

import com.example.campusconnect.feature.registrations.model.RegistrationDraft
import com.example.campusconnect.feature.registrations.model.TeamPortalData
import com.example.campusconnect.feature.registrations.model.TeamAssignment

/**
 * Participant-side registration operations backed by participant registration endpoints.
 */
interface UserRegistrationRepository {
    suspend fun createRegistration(eventId: Int): Result<RegistrationDraft>
    suspend fun saveIndividualAnswers(registrationId: Int, answers: Map<Int, String>): Result<Unit>
    suspend fun checkTeamName(eventId: Int, registrationId: Int, teamName: String): Result<Boolean>
    suspend fun createTeam(registrationId: Int, teamName: String): Result<TeamAssignment>
    suspend fun getTeams(eventId: Int, registrationId: Int): Result<TeamPortalData>
    suspend fun requestToJoin(registrationId: Int, teamId: Int): Result<Unit>
    suspend fun respondToInvite(requestId: Int, accept: Boolean): Result<Unit>
    suspend fun respondToJoinRequest(requestId: Int, accept: Boolean): Result<Unit>
    suspend fun inviteParticipant(teamId: Int, registrationId: Int): Result<Unit>
}
