package com.example.campusconnect.feature.events.data.repo

import com.example.campusconnect.feature.events.data.remote.request.AwardMedalRequest
import com.example.campusconnect.feature.events.data.remote.request.CreateEventRequest
import com.example.campusconnect.feature.events.data.remote.request.CreateRegistrationRequest
import com.example.campusconnect.feature.events.data.remote.request.GrantAccessRequest
import com.example.campusconnect.feature.events.data.remote.request.RemoveMedalRequest
import com.example.campusconnect.feature.events.data.remote.request.RevokeAccessRequest
import com.example.campusconnect.feature.events.data.remote.request.UpdateEventRequest
import com.example.campusconnect.feature.events.data.remote.response.EventResponse
import com.example.campusconnect.feature.events.data.remote.response.MedalAwardResponse
import com.example.campusconnect.feature.events.data.remote.response.ParticipantTeamResponse
import com.example.campusconnect.feature.events.data.remote.response.RegistrationResponse
import com.example.campusconnect.feature.events.data.remote.response.SoloParticipantResponse
import com.example.campusconnect.feature.events.data.remote.response.UserAccessResponse

interface EventRepository {

    // Events -------------------------------------------------------------

    suspend fun getEvents(): Result<List<EventResponse>>

    suspend fun getEvent(eventId: Int): Result<EventResponse>

    suspend fun createEvent(
        request: CreateEventRequest
    ): Result<EventResponse>

    suspend fun updateEvent(
        eventId: Int,
        request: UpdateEventRequest
    ): Result<EventResponse>

    suspend fun deleteEvent(eventId: Int): Result<Unit>


    // Registration -------------------------------------------------------

    suspend fun getRegistration(
        eventId: Int
    ): Result<RegistrationResponse>

    suspend fun createRegistration(
        eventId: Int,
        request: CreateRegistrationRequest
    ): Result<RegistrationResponse>


    // Participants -------------------------------------------------------

    suspend fun getTeams(
        eventId: Int
    ): Result<List<ParticipantTeamResponse>>

    suspend fun getSoloParticipants(
        eventId: Int
    ): Result<List<SoloParticipantResponse>>

    suspend fun getParticipantsCount(
        eventId: Int
    ): Result<Int>


    // Medals -------------------------------------------------------------

    suspend fun getMedalsForEvent(
        eventId: Int
    ): Result<List<MedalAwardResponse>>

    suspend fun awardMedal(
        eventId: Int,
        request: AwardMedalRequest
    ): Result<MedalAwardResponse>

    suspend fun removeMedal(
        eventId: Int,
        request: RemoveMedalRequest
    ): Result<Unit>


    // Access -------------------------------------------------------------

    suspend fun getUsersWithAccess(
        eventId: Int
    ): Result<List<UserAccessResponse>>

    suspend fun searchUsers(
        eventId: Int,
        query: String
    ): Result<List<UserAccessResponse>>

    suspend fun grantAccess(
        eventId: Int,
        request: GrantAccessRequest
    ): Result<Unit>

    suspend fun revokeAccess(
        eventId: Int,
        request: RevokeAccessRequest
    ): Result<Unit>
}