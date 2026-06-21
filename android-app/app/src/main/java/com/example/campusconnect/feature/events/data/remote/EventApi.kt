package com.example.campusconnect.feature.events.data.remote

import com.example.campusconnect.core.network.ApiResponse
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
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EventsApi {

    // Events ----------------------------------------------------------------

    @GET("events")
    suspend fun getEvents():
            Response<ApiResponse<List<EventResponse>>>

    @GET("events/{eventId}")
    suspend fun getEvent(
        @Path("eventId") eventId: Int
    ): Response<ApiResponse<EventResponse>>

    @POST("events")
    suspend fun createEvent(
        @Body body: CreateEventRequest
    ): Response<ApiResponse<EventResponse>>

    @PATCH("events/{eventId}")
    suspend fun updateEvent(
        @Path("eventId") eventId: Int,
        @Body body: UpdateEventRequest
    ): Response<ApiResponse<EventResponse>>

    @DELETE("events/{eventId}")
    suspend fun deleteEvent(
        @Path("eventId") eventId: Int
    ): Response<ApiResponse<Unit>>


    // Registration ----------------------------------------------------------

    @GET("events/{eventId}/registration")
    suspend fun getRegistration(
        @Path("eventId") eventId: Int
    ): Response<ApiResponse<RegistrationResponse>>

    @POST("events/{eventId}/registration")
    suspend fun createRegistration(
        @Path("eventId") eventId: Int,
        @Body body: CreateRegistrationRequest
    ): Response<ApiResponse<RegistrationResponse>>


    // Participants ----------------------------------------------------------

    @GET("events/{eventId}/teams")
    suspend fun getTeams(
        @Path("eventId") eventId: Int
    ): Response<ApiResponse<List<ParticipantTeamResponse>>>

    @GET("events/{eventId}/solo-participants")
    suspend fun getSoloParticipants(
        @Path("eventId") eventId: Int
    ): Response<ApiResponse<List<SoloParticipantResponse>>>

    @GET("events/{eventId}/participants-count")
    suspend fun getParticipantsCount(
        @Path("eventId") eventId: Int
    ): Response<ApiResponse<Int>>


    // Medals ----------------------------------------------------------------

    @GET("events/{eventId}/medals")
    suspend fun getMedalsForEvent(
        @Path("eventId") eventId: Int
    ): Response<ApiResponse<List<MedalAwardResponse>>>

    @POST("events/{eventId}/medals")
    suspend fun awardMedal(
        @Path("eventId") eventId: Int,
        @Body body: AwardMedalRequest
    ): Response<ApiResponse<MedalAwardResponse>>

    @HTTP(
        method = "DELETE",
        path = "events/{eventId}/medals",
        hasBody = true
    )
    suspend fun removeMedal(
        @Path("eventId") eventId: Int,
        @Body body: RemoveMedalRequest
    ): Response<ApiResponse<Unit>>


    // Access ----------------------------------------------------------------

    @GET("events/{eventId}/access")
    suspend fun getUsersWithAccess(
        @Path("eventId") eventId: Int
    ): Response<ApiResponse<List<UserAccessResponse>>>

    @GET("events/{eventId}/access/search")
    suspend fun searchUsers(
        @Path("eventId") eventId: Int,
        @Query("query") query: String
    ): Response<ApiResponse<List<UserAccessResponse>>>

    @POST("events/{eventId}/access")
    suspend fun grantAccess(
        @Path("eventId") eventId: Int,
        @Body body: GrantAccessRequest
    ): Response<ApiResponse<Unit>>

    @HTTP(
        method = "DELETE",
        path = "events/{eventId}/access",
        hasBody = true
    )
    suspend fun revokeAccess(
        @Path("eventId") eventId: Int,
        @Body body: RevokeAccessRequest
    ): Response<ApiResponse<Unit>>
}