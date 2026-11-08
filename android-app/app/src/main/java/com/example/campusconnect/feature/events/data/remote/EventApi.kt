package com.example.campusconnect.feature.events.data.remote

import android.util.Log
import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.events.data.remote.request.AwardMedalRequest
import com.example.campusconnect.feature.events.data.remote.request.CreateRegistrationRequest
import com.example.campusconnect.feature.events.data.remote.request.GrantAccessRequest
import com.example.campusconnect.feature.events.data.remote.response.EventHistoryResponse
import com.example.campusconnect.feature.events.data.remote.response.EventResponse
import com.example.campusconnect.feature.events.data.remote.response.MedalCandidateResponse
import com.example.campusconnect.feature.events.data.remote.response.MedalsResponse
import com.example.campusconnect.feature.events.data.remote.response.ParticipantsResponse
import com.example.campusconnect.feature.events.data.remote.response.RegistrationResponse
import com.example.campusconnect.feature.events.data.remote.response.UserAccessResponse
import com.example.campusconnect.feature.registrations.data.remote.RegistrationConfigRequest
import com.example.campusconnect.feature.registrations.data.remote.RegistrationConfigResponse
import com.example.campusconnect.feature.registrations.data.remote.QuickFieldsConfigRequest
import com.example.campusconnect.feature.registrations.data.remote.RegistrationFieldRequest
import com.example.campusconnect.feature.registrations.data.remote.RegistrationFieldResponse
import com.example.campusconnect.feature.registrations.data.remote.RegistrationSubmissionResponse
import com.example.campusconnect.feature.registrations.data.remote.RegistrationSubmissionDetailResponse
import com.example.campusconnect.feature.registrations.data.remote.AvailableTeamDto
import com.example.campusconnect.feature.registrations.data.remote.CreateInvitationRequest
import com.example.campusconnect.feature.registrations.data.remote.CreateTeamRequest
import com.example.campusconnect.feature.registrations.data.remote.ParticipantQuestionDto
import com.example.campusconnect.feature.registrations.data.remote.ParticipantRegistrationConfigDto
import com.example.campusconnect.feature.registrations.data.remote.ParticipantRegistrationDto
import com.example.campusconnect.feature.registrations.data.remote.SaveAnswersRequest
import com.example.campusconnect.feature.registrations.data.remote.TeamCandidateDto
import com.example.campusconnect.feature.registrations.data.remote.TeamIdRequest
import com.example.campusconnect.feature.registrations.data.remote.TeamPortalResponseDto
import com.example.campusconnect.feature.registrations.data.remote.TeamRequestDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query

interface EventsApi {

    // Participant registration --------------------------------------------

    @POST("events/{eventId}/registration")
    suspend fun initializeParticipantRegistration(
        @Path("eventId") eventId: Int,
    ): Response<ApiResponse<ParticipantRegistrationDto>>

    @GET("events/{eventId}/registration")
    suspend fun getParticipantRegistration(
        @Path("eventId") eventId: Int,
    ): Response<ApiResponse<ParticipantRegistrationDto>>

    @DELETE("events/{eventId}/registration")
    suspend fun cancelParticipantRegistration(
        @Path("eventId") eventId: Int,
    ): Response<ApiResponse<Unit>>

    @GET("events/{eventId}/registration/participant-config")
    suspend fun getParticipantRegistrationConfig(
        @Path("eventId") eventId: Int,
    ): Response<ApiResponse<ParticipantRegistrationConfigDto>>

    @GET("events/{eventId}/registration/questions")
    suspend fun getParticipantRegistrationQuestions(
        @Path("eventId") eventId: Int,
        @Query("individual") individual: Boolean,
    ): Response<ApiResponse<List<ParticipantQuestionDto>>>

    @GET("events/{eventId}/registration/teams")
    suspend fun getAvailableRegistrationTeams(
        @Path("eventId") eventId: Int,
        @Query("search") search: String? = null,
    ): Response<ApiResponse<List<AvailableTeamDto>>>

    @GET("events/{eventId}/registration/team-name-availability")
    suspend fun checkTeamNameAvailability(
        @Path("eventId") eventId: Int,
        @Query("teamName") teamName: String,
    ): Response<ApiResponse<Boolean>>

    @PUT("events/registrations/{registrationId}/answers")
    suspend fun saveParticipantAnswers(
        @Path("registrationId") registrationId: Int,
        @Body request: SaveAnswersRequest,
    ): Response<ApiResponse<ParticipantRegistrationDto>>

    @POST("events/registrations/{registrationId}/team")
    suspend fun createParticipantTeam(
        @Path("registrationId") registrationId: Int,
        @Body request: CreateTeamRequest,
    ): Response<ApiResponse<ParticipantRegistrationDto>>

    @POST("events/registrations/{registrationId}/finalize")
    suspend fun finalizeParticipantRegistration(
        @Path("registrationId") registrationId: Int,
    ): Response<ApiResponse<ParticipantRegistrationDto>>

    @POST("events/registrations/{registrationId}/team-requests")
    suspend fun requestToJoinTeam(
        @Path("registrationId") registrationId: Int,
        @Body request: TeamIdRequest,
    ): Response<ApiResponse<TeamRequestDto>>

    @POST("events/teams/{teamId}/invitations")
    suspend fun inviteParticipantToTeam(
        @Path("teamId") teamId: Int,
        @Body request: CreateInvitationRequest,
    ): Response<ApiResponse<TeamRequestDto>>

    @GET("events/registrations/team-requests")
    suspend fun getMyTeamRequests(): Response<ApiResponse<List<TeamRequestDto>>>

    @GET("events/teams/{teamId}/requests")
    suspend fun getTeamJoinRequests(
        @Path("teamId") teamId: Int,
    ): Response<ApiResponse<List<TeamRequestDto>>>

    @POST("events/team-requests/{requestId}/accept")
    suspend fun acceptTeamRequest(@Path("requestId") requestId: Int): Response<ApiResponse<TeamRequestDto>>

    @POST("events/team-requests/{requestId}/reject")
    suspend fun rejectTeamRequest(@Path("requestId") requestId: Int): Response<ApiResponse<TeamRequestDto>>

    @POST("events/team-requests/{requestId}/cancel")
    suspend fun cancelTeamRequest(@Path("requestId") requestId: Int): Response<ApiResponse<TeamRequestDto>>

    @GET("events/{eventId}/registration/team-candidates")
    suspend fun getTeamCandidates(
        @Path("eventId") eventId: Int,
        @Query("query") query: String,
    ): Response<ApiResponse<List<TeamCandidateDto>>>

    @GET("events/{eventId}/registration/my-team")
    suspend fun getMyRegistrationTeam(
        @Path("eventId") eventId: Int,
    ): Response<ApiResponse<TeamPortalResponseDto>>

    @PATCH("events/{eventId}/registrations/{registrationId}/approve")
    suspend fun approveRegistration(
        @Path("eventId") eventId: Int,
        @Path("registrationId") registrationId: Int,
    ): Response<ApiResponse<ParticipantRegistrationDto>>

    @PATCH("events/{eventId}/registrations/{registrationId}/reject")
    suspend fun rejectRegistration(
        @Path("eventId") eventId: Int,
        @Path("registrationId") registrationId: Int,
    ): Response<ApiResponse<ParticipantRegistrationDto>>

    // Host registration configuration ------------------------------------

    @GET("events/{eventId}/registration/config")
    suspend fun getRegistrationConfig(
        @Path("eventId") eventId: Int
    ): Response<ApiResponse<RegistrationConfigResponse>>

    @POST("events/{eventId}/registration/config")
    suspend fun createRegistrationConfig(
        @Path("eventId") eventId: Int,
        @Body request: RegistrationConfigRequest
    ): Response<ApiResponse<RegistrationConfigResponse>>

    @PUT("events/{eventId}/registration/config")
    suspend fun updateRegistrationConfig(
        @Path("eventId") eventId: Int,
        @Body request: RegistrationConfigRequest
    ): Response<ApiResponse<RegistrationConfigResponse>>

    @PATCH("events/{eventId}/registration/config/quick-fields")
    suspend fun updateQuickFieldsConfig(
        @Path("eventId") eventId: Int,
        @Body request: QuickFieldsConfigRequest
    ): Response<ApiResponse<RegistrationConfigResponse>>

    @GET("events/{eventId}/registration/fields")
    suspend fun getRegistrationFields(
        @Path("eventId") eventId: Int
    ): Response<ApiResponse<List<RegistrationFieldResponse>>>

    @GET("events/{eventId}/registrations")
    suspend fun getEventRegistrations(
        @Path("eventId") eventId: Int
    ): Response<ApiResponse<List<RegistrationSubmissionResponse>>>

    @GET("events/{eventId}/registrations/{registrationId}")
    suspend fun getEventRegistration(
        @Path("eventId") eventId: Int,
        @Path("registrationId") registrationId: Int
    ): Response<ApiResponse<RegistrationSubmissionDetailResponse>>

    @POST("events/{eventId}/registration/fields")
    suspend fun createRegistrationFields(
        @Path("eventId") eventId: Int,
        @Body fields: List<RegistrationFieldRequest>
    ): Response<ApiResponse<List<RegistrationFieldResponse>>>

    @PUT("events/{eventId}/registration/fields")
    suspend fun updateRegistrationFields(
        @Path("eventId") eventId: Int,
        @Body fields: List<RegistrationFieldRequest>
    ): Response<ApiResponse<List<RegistrationFieldResponse>>>

    // Events ----------------------------------------------------------------

    @GET("events")
    suspend fun getEvents():
            Response<ApiResponse<List<EventResponse>>>

    @GET("events/mine")
    suspend fun getMyEvents():
            Response<ApiResponse<List<EventResponse>>>

    @GET("events/shared")
    suspend fun getSharedEvents():
            Response<ApiResponse<List<EventResponse>>>

    @GET("events/managed")
    suspend fun getManagedEvents():
            Response<ApiResponse<List<EventResponse>>>

    @GET("events/{eventId}")
    suspend fun getEvent(
        @Path("eventId") eventId: Int
    ): Response<ApiResponse<EventResponse>>

    @Multipart
    @POST("events")
    suspend fun createEvent(
        @Part event: MultipartBody.Part,
        @Part poster: MultipartBody.Part?
    ): Response<ApiResponse<EventResponse>>

    @Multipart
    @PATCH("events/{eventId}")
    suspend fun updateEvent(
        @Path("eventId") eventId: Int,
        @Part event: MultipartBody.Part,
        @Part poster: MultipartBody.Part?
    ): Response<ApiResponse<EventResponse>>

    @DELETE("events/{eventId}")
    suspend fun deleteEvent(
        @Path("eventId") eventId: Int
    ): Response<ApiResponse<Unit>>

    @GET("events/history")
    suspend fun getEventHistory(): Response<ApiResponse<EventHistoryResponse>>


    // Participants ----------------------------------------------------------

    @GET("events/{eventId}/participants")
    suspend fun getParticipants(
        @Path("eventId") eventId: Int
    ): Response<ApiResponse<ParticipantsResponse>>


    // Medals ----------------------------------------------------------------

    @GET("events/{eventId}/medals")
    suspend fun getMedalsForEvent(
        @Path("eventId") eventId: Int
    ): Response<ApiResponse<MedalsResponse>>

    @GET("events/{eventId}/participants/eligible-for-medal")
    suspend fun getEligibleParticipantsForMedal(
        @Path("eventId") eventId: Int
    ): Response<ApiResponse<List<MedalCandidateResponse>>>

    @POST("events/{eventId}/medals")
    suspend fun awardMedal(
        @Path("eventId") eventId: Int,
        @Body body: AwardMedalRequest
    ): Response<ApiResponse<Unit>>

    @DELETE("events/{eventId}/medals/{honorId}")
    suspend fun removeMedal(
        @Path("eventId") eventId: Int,
        @Path("honorId") honorId: Int
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

    @DELETE("events/{eventId}/access/{userId}")
    suspend fun revokeAccess(
        @Path("eventId") eventId: Int,
        @Path("userId") userId: Int
    ): Response<ApiResponse<Unit>>
}
