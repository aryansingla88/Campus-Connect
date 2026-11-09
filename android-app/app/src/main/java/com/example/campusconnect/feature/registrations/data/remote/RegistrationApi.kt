package com.example.campusconnect.feature.registrations.data.remote

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.registrations.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query

interface RegistrationApi {
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

    // Backward-compatible team detail endpoints retained by the backend.
    @GET("events/teams/{teamId}")
    suspend fun getTeamSummary(@Path("teamId") teamId: Int): Response<ApiResponse<AvailableTeamDto>>

    @GET("events/teams/{teamId}/members")
    suspend fun getTeamMembers(@Path("teamId") teamId: Int): Response<ApiResponse<List<LegacyTeamMemberDto>>>

    @GET("events/teams/{teamId}/invite-candidates")
    suspend fun getTeamInviteCandidates(@Path("teamId") teamId: Int): Response<ApiResponse<List<LegacyTeamInviteCandidateDto>>>

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

}
