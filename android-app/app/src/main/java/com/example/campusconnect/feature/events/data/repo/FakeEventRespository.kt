package com.example.campusconnect.feature.events.data.repo

import com.example.campusconnect.feature.events.data.remote.request.AwardMedalRequest
import com.example.campusconnect.feature.events.data.remote.request.CreateEventRequest
import com.example.campusconnect.feature.events.data.remote.request.CreateRegistrationRequest
import com.example.campusconnect.feature.events.data.remote.request.GrantAccessRequest
import com.example.campusconnect.feature.events.data.remote.request.RemoveMedalRequest
import com.example.campusconnect.feature.events.data.remote.request.RevokeAccessRequest
import com.example.campusconnect.feature.events.data.remote.request.UpdateEventRequest

class FakeEventRepository : EventRepository {

    override suspend fun getEvents() = TODO()

    override suspend fun getEvent(
        eventId: Int
    ) = TODO()

    override suspend fun createEvent(
        request: CreateEventRequest
    ) = TODO()

    override suspend fun updateEvent(
        eventId: Int,
        request: UpdateEventRequest
    ) = TODO()

    override suspend fun deleteEvent(
        eventId: Int
    ) = Result.success(Unit)

    override suspend fun getRegistration(
        eventId: Int
    ) = TODO()

    override suspend fun createRegistration(
        eventId: Int,
        request: CreateRegistrationRequest
    ) = TODO()

    override suspend fun getTeams(
        eventId: Int
    ) = TODO()

    override suspend fun getSoloParticipants(
        eventId: Int
    ) = TODO()

    override suspend fun getParticipantsCount(
        eventId: Int
    ) = TODO()

    override suspend fun getMedalsForEvent(
        eventId: Int
    ) = TODO()

    override suspend fun awardMedal(
        eventId: Int,
        request: AwardMedalRequest
    ) = TODO()

    override suspend fun removeMedal(
        eventId: Int,
        request: RemoveMedalRequest
    ) = Result.success(Unit)

    override suspend fun getUsersWithAccess(
        eventId: Int
    ) = TODO()

    override suspend fun searchUsers(
        eventId: Int,
        query: String
    ) = TODO()

    override suspend fun grantAccess(
        eventId: Int,
        request: GrantAccessRequest
    ) = Result.success(Unit)

    override suspend fun revokeAccess(
        eventId: Int,
        request: RevokeAccessRequest
    ) = Result.success(Unit)
}