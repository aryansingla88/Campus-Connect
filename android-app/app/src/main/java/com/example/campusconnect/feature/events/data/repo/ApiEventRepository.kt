package com.example.campusconnect.feature.events.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.events.data.remote.EventsApi
import com.example.campusconnect.feature.events.data.remote.request.AwardMedalRequest
import com.example.campusconnect.feature.events.data.remote.request.CreateEventRequest
import com.example.campusconnect.feature.events.data.remote.request.GrantAccessRequest
import com.example.campusconnect.feature.events.data.remote.request.RemoveMedalRequest
import com.example.campusconnect.feature.events.data.remote.request.UpdateEventRequest
import com.example.campusconnect.feature.events.data.remote.response.EventHistoryResponse
import com.example.campusconnect.feature.events.data.remote.response.ParticipantsResponse
import com.example.campusconnect.feature.events.mapper.toEvent
import com.example.campusconnect.feature.events.model.Event
import com.example.campusconnect.feature.events.model.MedalAward
import com.example.campusconnect.feature.events.model.MedalType
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
        eventId: Int,
        request: UpdateEventRequest,
        poster: MultipartBody.Part?
    ): Result<Event> {
        return try {
            val json = Gson().toJson(request)

            val eventPart = MultipartBody.Part.createFormData(
                "event",
                "event.json",
                json.toRequestBody("application/json".toMediaType())
            )

            val response = api.updateEvent(
                eventId = eventId,
                event = eventPart,
                poster = poster
            )

            if (response.isSuccessful) {
                val eventResponse = response.body()?.data

                if (eventResponse != null) {
                    Result.success(eventResponse.toEvent())
                } else {
                    Result.failure(
                        Exception("Failed to update event: empty response")
                    )
                }
            } else {
                Result.failure(
                    Exception("Failed to update event: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteEvent(
        eventId: Int
    ): Result<Unit> {
        return try {
            val response = api.deleteEvent(eventId)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        "Failed to delete event: ${response.code()}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEventHistory(): Result<EventHistoryResponse> {
        return try {
            val response = api.getEventHistory()

            if (response.isSuccessful) {
                val data = response.body()?.data

                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(
                        Exception("Event history response is empty")
                    )
                }
            } else {
                Result.failure(
                    Exception(
                        "Failed to get event history: ${response.code()}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getRegistration(
        eventId: Int
    ): Result<Registration> = TODO()

    override suspend fun getParticipants(
        eventId: Int
    ): Result<ParticipantsResponse> {
        return try {
            val response = api.getParticipants(eventId)

            if (response.isSuccessful) {
                val data = response.body()?.data

                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(
                        Exception("Participants response is empty")
                    )
                }
            } else {
                Result.failure(
                    Exception(
                        "Failed to get participants: ${response.code()}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMedalsForEvent(
        eventId: Int
    ): Result<List<MedalAward>> {
        return try {
            val response = api.getMedalsForEvent(eventId)

            if (response.isSuccessful) {
                Result.success(
                    response.body()?.data?.map {
                        MedalAward(
                            eventId = it.eventId,
                            medalType = MedalType.valueOf(it.medalType),
                            recipientId = it.recipientId,
                            recipientName = it.recipientName,
                            recipientSubtitle = it.recipientSubtitle,
                            isTeam = it.isTeam
                        )
                    } ?: emptyList()
                )
            } else {
                Result.failure(
                    Exception(
                        "Failed to get medals: ${response.code()}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun awardMedal(
        award: MedalAward
    ): Result<MedalAward> {
        return try {
            val response = api.awardMedal(
                eventId = award.eventId,
                body = AwardMedalRequest(
                    medalType = award.medalType.name,
                    recipientId = award.recipientId,
                    recipientName = award.recipientName,
                    recipientSubtitle = award.recipientSubtitle,
                    isTeam = award.isTeam
                )
            )

            if (response.isSuccessful) {
                val data = response.body()?.data

                if (data != null) {
                    Result.success(
                        MedalAward(
                            eventId = data.eventId,
                            medalType = MedalType.valueOf(data.medalType),
                            recipientId = data.recipientId,
                            recipientName = data.recipientName,
                            recipientSubtitle = data.recipientSubtitle,
                            isTeam = data.isTeam
                        )
                    )
                } else {
                    Result.failure(
                        Exception("Award medal response is empty")
                    )
                }
            } else {
                Result.failure(
                    Exception(
                        "Failed to award medal: ${response.code()}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeMedal(
        eventId: Int,
        medalType: MedalType
    ): Result<Unit> {
        return try {
            val response = api.removeMedal(
                eventId = eventId,
                body = RemoveMedalRequest(
                    medalType = medalType.name
                )
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        "Failed to remove medal: ${response.code()}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUsersWithAccess(
        eventId: Int
    ): Result<List<UserAccess>> {
        return try {
            val response = api.getUsersWithAccess(eventId)

            if (response.isSuccessful) {
                Result.success(
                    response.body()?.data?.map {
                        UserAccess(
                            id = it.id,
                            name = it.name,
                            courseId = it.courseId,
                            admissionYear = it.admissionYear
                        )
                    } ?: emptyList()
                )
            } else {
                Result.failure(
                    Exception(
                        "Failed to get users with access: ${response.code()}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(
        eventId: Int,
        query: String
    ): Result<List<UserAccess>> {
        return try {
            val response = api.searchUsers(
                eventId = eventId,
                query = query
            )

            if (response.isSuccessful) {
                Result.success(
                    response.body()?.data?.map {
                        UserAccess(
                            id = it.id,
                            name = it.name,
                            courseId = it.courseId,
                            admissionYear = it.admissionYear
                        )
                    } ?: emptyList()
                )
            } else {
                Result.failure(
                    Exception(
                        "Failed to search users: ${response.code()}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun grantAccess(
        eventId: Int,
        userId: Int
    ): Result<Unit> {
        return try {
            val response = api.grantAccess(
                eventId = eventId,
                body = GrantAccessRequest(
                    userId = userId
                )
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        "Failed to grant access: ${response.code()}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun revokeAccess(
        eventId: Int,
        userId: Int
    ): Result<Unit> {
        return try {
            val response = api.revokeAccess(
                eventId = eventId,
                userId = userId
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        "Failed to revoke access: ${response.code()}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}