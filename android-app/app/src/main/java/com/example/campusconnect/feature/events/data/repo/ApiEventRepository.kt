package com.example.campusconnect.feature.events.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.events.data.remote.EventsApi
import com.example.campusconnect.feature.events.data.remote.request.CreateEventRequest
import com.example.campusconnect.feature.events.mapper.toEvent
import com.example.campusconnect.feature.events.model.Event
import com.example.campusconnect.feature.events.model.MedalAward
import com.example.campusconnect.feature.events.model.MedalType
import com.example.campusconnect.feature.events.model.ParticipantTeam
import com.example.campusconnect.feature.events.model.SoloParticipant
import com.example.campusconnect.feature.events.model.UserAccess
import com.example.campusconnect.feature.events.registrations.model.Registration
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ApiEventRepository(
    private val api: EventsApi = RetrofitClient.eventsApi

) : EventRepository {

    override suspend fun getEvents(): Result<List<Event>> {
        return try {
            val response = api.getEvents()

            if (response.isSuccessful) {
                Result.success(
                    response.body()?.data?.map { it.toEvent() } ?: emptyList()
                )
            } else {
                Result.failure(
                    Exception("Failed to get events: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEvent(
        eventId: Int
    ): Result<Event> {
        return try {
            val response = api.getEvent(eventId)

            if (response.isSuccessful) {
                val event = response.body()?.data

                if (event != null) {
                    Result.success(event.toEvent())
                } else {
                    Result.failure(Exception("Event not found"))
                }
            } else {
                Result.failure(
                    Exception("Failed to get event: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getMyEvents(): Result<List<Event>> {
        return try {
            val response = api.getMyEvents()

            if (response.isSuccessful) {
                Result.success(
                    response.body()?.data?.map { it.toEvent() } ?: emptyList()
                )
            } else {
                Result.failure(
                    Exception("Failed to get my events: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSharedEvents(): Result<List<Event>> {
        return try {
            val response = api.getSharedEvents()

            if (response.isSuccessful) {
                Result.success(
                    response.body()?.data?.map { it.toEvent() } ?: emptyList()
                )
            } else {
                Result.failure(
                    Exception("Failed to get shared events: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getManagedEvents(): Result<List<Event>> {
        return try {
            val response = api.getManagedEvents()

            if (response.isSuccessful) {
                Result.success(
                    response.body()?.data?.map { it.toEvent() } ?: emptyList()
                )
            } else {
                Result.failure(
                    Exception("Failed to get managed events: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getEventFeed(): Result<List<Event>> = TODO()

    override suspend fun createEvent(
        request: CreateEventRequest,
        poster: MultipartBody.Part?
    ): Result<Event> {
        return try {
            val json = Gson().toJson(request)

            val eventPart = MultipartBody.Part.createFormData(
                "event",
                "event.json",
                json.toRequestBody("application/json".toMediaType())
            )

            val response = api.createEvent(
                event = eventPart,
                poster = poster
            )

            if (response.isSuccessful) {
                val eventResponse = response.body()?.data

                if (eventResponse != null) {
                    Result.success(eventResponse.toEvent())
                } else {
                    Result.failure(
                        Exception("Failed to create event: empty response")
                    )
                }
            } else {
                Result.failure(
                    Exception("Failed to create event: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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