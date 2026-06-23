package com.example.campusconnect.feature.events.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.events.data.remote.EventsApi
import com.example.campusconnect.feature.events.model.Event
import com.example.campusconnect.feature.events.model.MedalAward
import com.example.campusconnect.feature.events.model.MedalType
import com.example.campusconnect.feature.events.model.ParticipantTeam
import com.example.campusconnect.feature.events.model.SoloParticipant
import com.example.campusconnect.feature.events.model.UserAccess
import com.example.campusconnect.feature.events.registrations.model.Registration

class ApiEventRepository(
    private val api: EventsApi = RetrofitClient.eventsApi
) : EventRepository {

    override suspend fun getEvents(): Result<List<Event>> = TODO()

    override suspend fun getEvent(
        eventId: Int
    ): Result<Event> = TODO()

    override suspend fun createEvent(
        event: Event
    ): Result<Event> = TODO()

    override suspend fun updateEvent(
        event: Event
    ): Result<Event> = TODO()

    override suspend fun deleteEvent(
        eventId: Int
    ): Result<Unit> = TODO()

    override suspend fun getRegistration(
        eventId: Int
    ): Result<Registration> = TODO()

    override suspend fun getTeams(
        eventId: Int
    ): Result<List<ParticipantTeam>> = TODO()

    override suspend fun getSoloParticipants(
        eventId: Int
    ): Result<List<SoloParticipant>> = TODO()

    override suspend fun getParticipantsCount(
        eventId: Int
    ): Result<Int> = TODO()

    override suspend fun getMedalsForEvent(
        eventId: Int
    ): Result<List<MedalAward>> = TODO()

    override suspend fun awardMedal(
        award: MedalAward
    ): Result<MedalAward> = TODO()

    override suspend fun removeMedal(
        eventId: Int,
        medalType: MedalType
    ): Result<Unit> = TODO()

    override suspend fun getUsersWithAccess(
        eventId: Int
    ): Result<List<UserAccess>> = TODO()

    override suspend fun searchUsers(
        eventId: Int,
        query: String
    ): Result<List<UserAccess>> = TODO()
}