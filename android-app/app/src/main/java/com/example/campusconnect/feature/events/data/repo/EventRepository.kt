package com.example.campusconnect.feature.events.data.repo

import com.example.campusconnect.feature.events.data.remote.request.CreateEventRequest
import com.example.campusconnect.feature.events.data.remote.request.UpdateEventRequest
import com.example.campusconnect.feature.events.data.remote.response.ParticipantsResponse
import com.example.campusconnect.feature.events.model.Event
import com.example.campusconnect.feature.events.model.MedalAward
import com.example.campusconnect.feature.events.model.MedalType
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
        eventId: Int,
        request: UpdateEventRequest,
        poster: MultipartBody.Part?
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

    suspend fun getParticipants(
        eventId: Int
    ): Result<ParticipantsResponse>


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

    suspend fun grantAccess(
        eventId: Int,
        userId: Int
    ): Result<Unit>

    suspend fun revokeAccess(
        eventId: Int,
        userId: Int
    ): Result<Unit>
}