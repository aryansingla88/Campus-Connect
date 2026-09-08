package com.example.campusconnect.feature.events.data.repo

import com.example.campusconnect.feature.events.data.remote.request.CreateEventRequest
import com.example.campusconnect.feature.events.model.Event
import com.example.campusconnect.feature.events.model.MedalAward
import com.example.campusconnect.feature.events.model.MedalType
import com.example.campusconnect.feature.events.model.ParticipantTeam
import com.example.campusconnect.feature.events.model.SoloParticipant
import com.example.campusconnect.feature.events.model.UserAccess
import com.example.campusconnect.feature.events.registrations.model.Registration
import okhttp3.MultipartBody

interface EventRepository {

    // Events -------------------------------------------------------------

    suspend fun getEvents(): Result<List<Event>>

    suspend fun getEvent(
        eventId: Int
    ): Result<Event>

    suspend fun createEvent(
        request: CreateEventRequest,
        poster: MultipartBody.Part?
    ): Result<Event>

    suspend fun updateEvent(
        event: Event
    ): Result<Event>

    suspend fun deleteEvent(
        eventId: Int
    ): Result<Unit>

    suspend fun getEventFeed(): Result<List<Event>>

    suspend fun getMyEvents(): Result<List<Event>>

    suspend fun getSharedEvents(): Result<List<Event>>

    suspend fun getManagedEvents(): Result<List<Event>>


    // Registration -------------------------------------------------------

    suspend fun getRegistration(
        eventId: Int
    ): Result<Registration>


    // Participants -------------------------------------------------------

    suspend fun getTeams(
        eventId: Int
    ): Result<List<ParticipantTeam>>

    suspend fun getSoloParticipants(
        eventId: Int
    ): Result<List<SoloParticipant>>

    suspend fun getParticipantsCount(
        eventId: Int
    ): Result<Int>


    // Medals -------------------------------------------------------------

    suspend fun getMedalsForEvent(
        eventId: Int
    ): Result<List<MedalAward>>

    suspend fun awardMedal(
        award: MedalAward
    ): Result<MedalAward>

    suspend fun removeMedal(
        eventId: Int,
        medalType: MedalType
    ): Result<Unit>


    // Access -------------------------------------------------------------

    suspend fun getUsersWithAccess(
        eventId: Int
    ): Result<List<UserAccess>>

    suspend fun searchUsers(
        eventId: Int,
        query: String
    ): Result<List<UserAccess>>
}