package com.example.campusconnect.feature.profile.data.remote

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.profile.data.remote.request.*
import com.example.campusconnect.feature.profile.data.remote.response.*
import retrofit2.Response
import retrofit2.http.*

interface ProfileApi {



    // Profile----------------------------------------------------------------

    // GET own profile — uses session token to identify user
    @GET("users/me")
    suspend fun getMyProfile(): Response<ApiResponse<ProfileResponse>>

    // GET another user's profile
    @GET("users/{userId}")
    suspend fun getProfile(
        @Path("userId") userId: Int
    ): Response<ApiResponse<ProfileResponse>>

    // PATCH own profile fields (user_profile table)
    @PATCH("users/me")
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




    // Connections (user_friends table)----------------------------------------------------------------

    @GET("users/me/connections")
    suspend fun getMyConnections(): Response<ApiResponse<List<ConnectionResponse>>>

    @GET("users/{userId}/connections")
    suspend fun getUserConnections(
        @Path("userId") userId: Int
    ): Response<ApiResponse<List<ConnectionResponse>>>

    @GET("users/me/connections/requests")
    suspend fun getConnectionRequests():
            Response<ApiResponse<List<ConnectionRequestResponse>>>

    @POST("users/{userId}/connections/request")
    suspend fun sendConnectionRequest(
        @Path("userId") userId: Int
    ): Response<ApiResponse<Unit>>

    @POST("users/{userId}/connections/accept")
    suspend fun acceptConnectionRequest(
        @Path("userId") userId: Int
    ): Response<ApiResponse<Unit>>

    @DELETE("users/{userId}/connections")
    suspend fun removeConnection(
        @Path("userId") userId: Int
    ): Response<ApiResponse<Unit>>

    @GET("users/search")
    suspend fun searchUsers(
        @Query("q") query: String
    ): Response<ApiResponse<List<ConnectionResponse>>>



    // Clubs (clubs + club_members tables)----------------------------------------------------------------

    @GET("users/me/clubs")
    suspend fun getMyClubs(): Response<ApiResponse<List<ClubResponse>>>

    @GET("users/{userId}/clubs")
    suspend fun getUserClubs(
        @Path("userId") userId: Int
    ): Response<ApiResponse<List<ClubResponse>>>

    @POST("clubs/{clubId}/join")
    suspend fun joinClub(
        @Path("clubId") clubId: String
    ): Response<ApiResponse<Unit>>

    @DELETE("clubs/{clubId}/leave")
    suspend fun leaveClub(
        @Path("clubId") clubId: String
    ): Response<ApiResponse<Unit>>

    // Honor (user_honor + honor_items + honor_points tables)----------------------------------------------------------------

    @GET("users/me/honors")
    suspend fun getMyHonors():
            Response<ApiResponse<ProfileHonorsResponse>>

    @GET("users/{userId}/honors")
    suspend fun getUserHonors(
        @Path("userId") userId: Int
    ): Response<ApiResponse<ProfileHonorsResponse>>

    @PATCH("users/me/honors/priority")
    suspend fun updateHonorPriority(
        @Body body: UpdateHonorPriorityRequest
    ): Response<ApiResponse<Unit>>



    // Interests (interests + user_interests tables)----------------------------------------------------------------

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
        @Path("interestId") interestId: String
    ): Response<ApiResponse<Unit>>

    // DELETE remove interest from own profile
    @DELETE("users/me/interests/{interestId}")
    suspend fun removeInterest(
        @Path("interestId") interestId: String
    ): Response<ApiResponse<Unit>>


}