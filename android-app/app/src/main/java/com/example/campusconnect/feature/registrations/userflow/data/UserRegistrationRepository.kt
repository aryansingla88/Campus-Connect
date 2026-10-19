package com.example.campusconnect.feature.registrations.userflow.data

import com.example.campusconnect.feature.registrations.userflow.model.RegistrationDraft
import com.example.campusconnect.feature.registrations.userflow.model.TeamPortalData

/**
 * Participant-side registration operations. Implement against the real backend;
 * [FakeUserRegistrationRepository] stands in until the endpoints are available.
 */
interface UserRegistrationRepository {
    suspend fun createRegistration(eventId: Int): Result<RegistrationDraft>
    suspend fun getTeams(eventId: Int): Result<TeamPortalData>
    suspend fun requestToJoin(eventId: Int, teamId: Int): Result<Unit>
    suspend fun respondToInvite(eventId: Int, inviteId: Int, accept: Boolean): Result<Unit>
}
