package com.example.campusconnect.feature.profile.data.remote

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.profile.data.remote.request.*
import com.example.campusconnect.feature.profile.data.remote.response.*
import retrofit2.Response
import retrofit2.http.*

interface ProfileApi {

    // -------------------------------------------------------------------------
    // Profile
    // -------------------------------------------------------------------------

    // GET own profile — uses session token to identify user
    @GET("users/me")
    suspend fun getMyProfile(): Response<ApiResponse<ProfileResponse>>

    // GET another user's profile
    @GET("users/{userId}")
    suspend fun getProfile(
        @Path("userId") userId: Int
    ): Response<ApiResponse<ProfileResponse>>

    // PATCH own profile fields (user_profile table)
    @PATCH("users/me/profile")
    suspend fun updateProfile(
        @Body body: UpdateProfileRequest
    ): Response<ApiResponse<ProfileResponse>>

    // GET own stats — connection/club/interest/honor counts
    @GET("users/me/stats")
    suspend fun getMyStats(): Response<ApiResponse<ProfileStatsResponse>>

    // GET another user's stats
    @GET("users/{userId}/stats")
    suspend fun getUserStats(
        @Path("userId") userId: Int
    ): Response<ApiResponse<ProfileStatsResponse>>

    // -------------------------------------------------------------------------
    // Preferences (user_preferences table)
    // -------------------------------------------------------------------------

    @GET("users/me/preferences")
    suspend fun getPreferences(): Response<ApiResponse<PreferencesResponse>>

    @PATCH("users/me/preferences")
    suspend fun updatePreferences(
        @Body body: UpdatePreferencesRequest
    ): Response<ApiResponse<PreferencesResponse>>

    // -------------------------------------------------------------------------
    // Connections (user_friends table)
    // -------------------------------------------------------------------------

    // GET own confirmed connections
    @GET("users/me/connections")
    suspend fun getMyConnections(): Response<ApiResponse<List<ConnectionResponse>>>

    // GET another user's confirmed connections
    @GET("users/{userId}/connections")
    suspend fun getUserConnections(
        @Path("userId") userId: Int
    ): Response<ApiResponse<List<ConnectionResponse>>>

    // GET all pending requests — both incoming and outgoing
    @GET("users/me/connections/requests")
    suspend fun getConnectionRequests(): Response<ApiResponse<List<ConnectionRequestResponse>>>

    // POST send a connection request (sender = me, receiver = userId)
    @POST("users/{userId}/connections/request")
    suspend fun sendConnectionRequest(
        @Path("userId") userId: Int
    ): Response<ApiResponse<Unit>>

    // POST accept incoming request
    @POST("users/{userId}/connections/accept")
    suspend fun acceptConnectionRequest(
        @Path("userId") userId: Int
    ): Response<ApiResponse<Unit>>

    // DELETE decline incoming request or cancel outgoing invite or remove connection
    @DELETE("users/{userId}/connections")
    suspend fun removeConnection(
        @Path("userId") userId: Int
    ): Response<ApiResponse<Unit>>

    // -------------------------------------------------------------------------
    // Clubs (clubs + club_members tables)
    // -------------------------------------------------------------------------

    // GET own clubs
    @GET("users/me/clubs")
    suspend fun getMyClubs(): Response<ApiResponse<List<ClubResponse>>>

    // GET another user's clubs
    @GET("users/{userId}/clubs")
    suspend fun getUserClubs(
        @Path("userId") userId: Int
    ): Response<ApiResponse<List<ClubResponse>>>

    // POST request to join a club
    @POST("clubs/{clubId}/join")
    suspend fun joinClub(
        @Path("clubId") clubId: Int
    ): Response<ApiResponse<Unit>>

    // DELETE leave a club
    @DELETE("clubs/{clubId}/leave")
    suspend fun leaveClub(
        @Path("clubId") clubId: Int
    ): Response<ApiResponse<Unit>>

    // -------------------------------------------------------------------------
    // Honor (user_honor + honor_items + honor_points tables)
    // -------------------------------------------------------------------------

    // GET own honor items (badges + medals)
    @GET("users/me/honor")
    suspend fun getMyHonor(): Response<ApiResponse<List<HonorResponse>>>

    // GET another user's honor items
    @GET("users/{userId}/honor")
    suspend fun getUserHonor(
        @Path("userId") userId: Int
    ): Response<ApiResponse<List<HonorResponse>>>

    // GET honor leaderboard — RANK() OVER (ORDER BY points DESC)
    @GET("honor/leaderboard")
    suspend fun getLeaderboard(): Response<ApiResponse<List<HonorLeaderboardEntryResponse>>>

    // PATCH update display priority of a single honor item
    @PATCH("users/me/honor/priority")
    suspend fun updateHonorPriority(
        @Body body: UpdateHonorPriorityRequest
    ): Response<ApiResponse<Unit>>

    // -------------------------------------------------------------------------
    // Interests (interests + user_interests tables)
    // -------------------------------------------------------------------------

    // GET full catalogue
    @GET("interests")
    suspend fun getAllInterests(): Response<ApiResponse<List<InterestResponse>>>

    // GET own interests
    @GET("users/me/interests")
    suspend fun getMyInterests(): Response<ApiResponse<List<InterestResponse>>>

    // GET another user's interests
    @GET("users/{userId}/interests")
    suspend fun getUserInterests(
        @Path("userId") userId: Int
    ): Response<ApiResponse<List<InterestResponse>>>

    // POST add interest to own profile
    @POST("users/me/interests/{interestId}")
    suspend fun addInterest(
        @Path("interestId") interestId: Int
    ): Response<ApiResponse<Unit>>

    // DELETE remove interest from own profile
    @DELETE("users/me/interests/{interestId}")
    suspend fun removeInterest(
        @Path("interestId") interestId: Int
    ): Response<ApiResponse<Unit>>

    // -------------------------------------------------------------------------
    // Courses metadata (courses table)
    // -------------------------------------------------------------------------

    // GET full course catalogue — used in profile edit dropdown
    @GET("courses")
    suspend fun getCourses(): Response<ApiResponse<List<CourseResponse>>>
}