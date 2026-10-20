package com.example.campusconnect.feature.registrations.data.remote

import com.example.campusconnect.core.network.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.DELETE
import retrofit2.http.Query

/** Retrofit contract for host registration configuration, forms, and submissions. */
interface RegistrationApi {
    @GET("events/{eventId}/registration/config")
    suspend fun getRegistrationConfig(
        @Path("eventId") eventId: Int,
    ): Response<ApiResponse<RegistrationConfigResponse>>

    @POST("events/{eventId}/registration/config")
    suspend fun createRegistrationConfig(
        @Path("eventId") eventId: Int,
        @Body request: RegistrationConfigRequest,
    ): Response<ApiResponse<RegistrationConfigResponse>>

    @PUT("events/{eventId}/registration/config")
    suspend fun updateRegistrationConfig(
        @Path("eventId") eventId: Int,
        @Body request: RegistrationConfigRequest,
    ): Response<ApiResponse<RegistrationConfigResponse>>

    @PATCH("events/{eventId}/registration/config/quick-fields")
    suspend fun updateQuickFieldsConfig(
        @Path("eventId") eventId: Int,
        @Body request: QuickFieldsConfigRequest,
    ): Response<ApiResponse<RegistrationConfigResponse>>

    @GET("events/{eventId}/registration/fields")
    suspend fun getRegistrationFields(
        @Path("eventId") eventId: Int,
    ): Response<ApiResponse<List<RegistrationFieldResponse>>>

    @POST("events/{eventId}/registration/fields")
    suspend fun createRegistrationFields(
        @Path("eventId") eventId: Int,
        @Body fields: List<RegistrationFieldRequest>,
    ): Response<ApiResponse<List<RegistrationFieldResponse>>>

    @PUT("events/{eventId}/registration/fields")
    suspend fun updateRegistrationFields(
        @Path("eventId") eventId: Int,
        @Body fields: List<RegistrationFieldRequest>,
    ): Response<ApiResponse<List<RegistrationFieldResponse>>>

    @GET("events/{eventId}/registrations")
    suspend fun getEventRegistrations(
        @Path("eventId") eventId: Int,
    ): Response<ApiResponse<List<RegistrationSubmissionResponse>>>

    @GET("events/{eventId}/registrations/{registrationId}")
    suspend fun getEventRegistration(
        @Path("eventId") eventId: Int,
        @Path("registrationId") registrationId: Int,
    ): Response<ApiResponse<RegistrationSubmissionDetailResponse>>

    @PATCH("events/{eventId}/registrations/{registrationId}/approve")
    suspend fun approveRegistration(@Path("eventId") eventId: Int, @Path("registrationId") registrationId: Int): Response<ApiResponse<ParticipantRegistrationResponse>>

    @PATCH("events/{eventId}/registrations/{registrationId}/reject")
    suspend fun rejectRegistration(@Path("eventId") eventId: Int, @Path("registrationId") registrationId: Int): Response<ApiResponse<ParticipantRegistrationResponse>>

    @POST("events/{eventId}/registration")
    suspend fun initializeParticipantRegistration(@Path("eventId") eventId: Int): Response<ApiResponse<ParticipantRegistrationResponse>>

    @GET("events/{eventId}/registration")
    suspend fun getParticipantRegistration(@Path("eventId") eventId: Int): Response<ApiResponse<ParticipantRegistrationResponse>>

    @DELETE("events/{eventId}/registration")
    suspend fun cancelParticipantRegistration(@Path("eventId") eventId: Int): Response<ApiResponse<Unit>>

    @GET("events/registrations")
    suspend fun getMyRegistrations(): Response<ApiResponse<List<ParticipantRegistrationResponse>>>

    @GET("events/{eventId}/registration/questions")
    suspend fun getParticipantQuestions(@Path("eventId") eventId: Int, @Query("individual") individual: Boolean): Response<ApiResponse<List<RegistrationFieldResponse>>>

    @GET("events/{eventId}/registration/teams")
    suspend fun getAvailableTeams(@Path("eventId") eventId: Int, @Query("search") search: String? = null): Response<ApiResponse<List<AvailableTeamDto>>>

    @GET("events/{eventId}/registration/team-name-availability")
    suspend fun isTeamNameAvailable(@Path("eventId") eventId: Int, @Query("teamName") teamName: String): Response<ApiResponse<Boolean>>

    @GET("events/teams/{teamId}")
    suspend fun getTeamSummary(@Path("teamId") teamId: Int): Response<ApiResponse<AvailableTeamDto>>

    @PUT("events/registrations/{registrationId}/answers")
    suspend fun saveParticipantAnswers(@Path("registrationId") registrationId: Int, @Body request: ParticipantAnswersRequest): Response<ApiResponse<ParticipantRegistrationResponse>>

    @POST("events/registrations/{registrationId}/team")
    suspend fun createParticipantTeam(@Path("registrationId") registrationId: Int, @Body request: CreateParticipantTeamRequest): Response<ApiResponse<ParticipantRegistrationResponse>>

    @POST("events/registrations/{registrationId}/finalize")
    suspend fun finalizeParticipantRegistration(@Path("registrationId") registrationId: Int): Response<ApiResponse<ParticipantRegistrationResponse>>

    @POST("events/registrations/{registrationId}/team-requests")
    suspend fun createTeamJoinRequest(@Path("registrationId") registrationId: Int, @Body request: TeamRequestCreateDto): Response<ApiResponse<TeamRequestDto>>

    @POST("events/teams/{teamId}/invitations")
    suspend fun createTeamInvitation(@Path("teamId") teamId: Int, @Body request: TeamInvitationCreateDto): Response<ApiResponse<TeamRequestDto>>

    @GET("events/registrations/team-requests")
    suspend fun getMyTeamRequests(): Response<ApiResponse<List<TeamRequestDto>>>

    @GET("events/teams/{teamId}/requests")
    suspend fun getTeamRequests(@Path("teamId") teamId: Int): Response<ApiResponse<List<TeamRequestDto>>>

    @POST("events/team-requests/{requestId}/accept")
    suspend fun acceptTeamRequest(@Path("requestId") requestId: Int): Response<ApiResponse<TeamRequestDto>>

    @POST("events/team-requests/{requestId}/reject")
    suspend fun rejectTeamRequest(@Path("requestId") requestId: Int): Response<ApiResponse<TeamRequestDto>>

    @POST("events/team-requests/{requestId}/cancel")
    suspend fun cancelTeamRequest(@Path("requestId") requestId: Int): Response<ApiResponse<TeamRequestDto>>

    @GET("events/teams/{teamId}/members")
    suspend fun getTeamMembers(@Path("teamId") teamId: Int): Response<ApiResponse<List<TeamMemberDto>>>

    @GET("events/teams/{teamId}/invite-candidates")
    suspend fun getTeamInviteCandidates(@Path("teamId") teamId: Int): Response<ApiResponse<List<TeamInviteCandidateDto>>>
}

data class ParticipantRegistrationResponse(
    val registrationId: Int? = null, val status: String? = null, val eventId: Int? = null,
    val teamId: Int? = null, val teamName: String? = null, val canResume: Boolean? = null, val teamLeader: Boolean? = null,
    val answers: List<SavedParticipantAnswerDto> = emptyList(),
)
data class SavedParticipantAnswerDto(val fieldId: Int, val answer: String, val type: String? = null)
data class ParticipantAnswersRequest(val type: String, val answers: List<ParticipantAnswerDto>)
data class ParticipantAnswerDto(val fieldId: Int, val answer: String)
data class CreateParticipantTeamRequest(val teamName: String)
data class AvailableTeamDto(val teamId: Int, val teamName: String, val memberCount: Int, val capacity: Int, val leaderName: String)
data class TeamRequestCreateDto(val teamId: Int)
data class TeamInvitationCreateDto(val registrationId: Int)
data class TeamRequestDto(
    val requestId: Int, val teamId: Int, val teamName: String, val registrationId: Int,
    val participantId: Int, val participantName: String, val initiatedBy: String,
    val status: String, val createdAt: String? = null,
)
data class TeamMemberDto(val registrationId: Int, val userId: Int, val name: String, val email: String? = null, val role: String? = null, val program: String? = null)
data class TeamInviteCandidateDto(val registrationId: Int, val participantId: Int, val name: String, val email: String? = null, val program: String? = null)
