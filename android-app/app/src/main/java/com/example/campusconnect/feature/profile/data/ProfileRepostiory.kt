package com.example.campusconnect.feature.profile.data

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.profile.data.remote.ProfileApi
import com.example.campusconnect.feature.profile.data.remote.request.*
import com.example.campusconnect.feature.profile.data.remote.response.*
import com.example.campusconnect.feature.profile.model.*

class ProfileRepository(
    private val api: ProfileApi = RetrofitClient.profileApi
) {

    // -------------------------------------------------------------------------
    // Profile
    // -------------------------------------------------------------------------

    suspend fun getMyProfile(): Result<PublicUserProfile> = runCatching {
        api.getMyProfile().body()?.data?.toDomain() ?: error("Empty response")
    }

    suspend fun getProfile(userId: Int): Result<PublicUserProfile> = runCatching {
        api.getProfile(userId).body()?.data?.toDomain() ?: error("Empty response")
    }

    suspend fun updateProfile(profile: PublicUserProfile): Result<PublicUserProfile> = runCatching {
        api.updateProfile(profile.toUpdateRequest()).body()?.data?.toDomain() ?: error("Empty response")
    }

    suspend fun getMyStats(): Result<ProfileStats> = runCatching {
        api.getMyStats().body()?.data?.toDomain() ?: error("Empty response")
    }

    suspend fun getUserStats(userId: Int): Result<ProfileStats> = runCatching {
        api.getUserStats(userId).body()?.data?.toDomain() ?: error("Empty response")
    }

    // -------------------------------------------------------------------------
    // Preferences
    // -------------------------------------------------------------------------

    suspend fun getPreferences(): Result<UserPreferences> = runCatching {
        api.getPreferences().body()?.data?.toDomain() ?: error("Empty response")
    }

    suspend fun updatePreferences(prefs: UserPreferences): Result<UserPreferences> = runCatching {
        api.updatePreferences(prefs.toRequest()).body()?.data?.toDomain() ?: error("Empty response")
    }

    // -------------------------------------------------------------------------
    // Connections
    // -------------------------------------------------------------------------

    suspend fun getMyConnections(): Result<List<Connection>> = runCatching {
        api.getMyConnections().body()?.data?.map { it.toDomain() } ?: emptyList()
    }

    suspend fun getUserConnections(userId: Int): Result<List<Connection>> = runCatching {
        api.getUserConnections(userId).body()?.data?.map { it.toDomain() } ?: emptyList()
    }

    suspend fun getConnectionRequests(): Result<List<ConnectionRequest>> = runCatching {
        api.getConnectionRequests().body()?.data?.map { it.toDomain() } ?: emptyList()
    }

    suspend fun sendConnectionRequest(userId: Int): Result<Unit> = runCatching {
        api.sendConnectionRequest(userId)
    }

    suspend fun acceptConnectionRequest(userId: Int): Result<Unit> = runCatching {
        api.acceptConnectionRequest(userId)
    }

    suspend fun removeConnection(userId: Int): Result<Unit> = runCatching {
        api.removeConnection(userId)
    }

    // -------------------------------------------------------------------------
    // Clubs
    // -------------------------------------------------------------------------

    suspend fun getMyClubs(): Result<List<Club>> = runCatching {
        api.getMyClubs().body()?.data?.map { it.toDomain() } ?: emptyList()
    }

    suspend fun getUserClubs(userId: Int): Result<List<Club>> = runCatching {
        api.getUserClubs(userId).body()?.data?.map { it.toDomain() } ?: emptyList()
    }

    suspend fun joinClub(clubId: Int): Result<Unit> = runCatching {
        api.joinClub(clubId)
    }

    suspend fun leaveClub(clubId: Int): Result<Unit> = runCatching {
        api.leaveClub(clubId)
    }

    // -------------------------------------------------------------------------
    // Honor
    // -------------------------------------------------------------------------

    suspend fun getMyHonor(): Result<List<Honor>> = runCatching {
        api.getMyHonor().body()?.data?.map { it.toDomain() } ?: emptyList()
    }

    suspend fun getUserHonor(userId: Int): Result<List<Honor>> = runCatching {
        api.getUserHonor(userId).body()?.data?.map { it.toDomain() } ?: emptyList()
    }

    suspend fun getLeaderboard(): Result<List<HonorEntry>> = runCatching {
        api.getLeaderboard().body()?.data?.map { it.toDomain() } ?: emptyList()
    }

    suspend fun updateHonorPriority(honorId: Int, priority: Int): Result<Unit> = runCatching {
        api.updateHonorPriority(UpdateHonorPriorityRequest(honorId, priority))
    }

    // -------------------------------------------------------------------------
    // Interests
    // -------------------------------------------------------------------------

    suspend fun getAllInterests(): Result<List<Interest>> = runCatching {
        api.getAllInterests().body()?.data?.map { it.toDomain() } ?: emptyList()
    }

    suspend fun getMyInterests(): Result<List<Interest>> = runCatching {
        api.getMyInterests().body()?.data?.map { it.toDomain() } ?: emptyList()
    }

    suspend fun getUserInterests(userId: Int): Result<List<Interest>> = runCatching {
        api.getUserInterests(userId).body()?.data?.map { it.toDomain() } ?: emptyList()
    }

    suspend fun addInterest(interestId: Int): Result<Unit> = runCatching {
        api.addInterest(interestId)
    }

    suspend fun removeInterest(interestId: Int): Result<Unit> = runCatching {
        api.removeInterest(interestId)
    }

    // -------------------------------------------------------------------------
    // Courses
    // -------------------------------------------------------------------------

    suspend fun getCourses(): Result<List<Course>> = runCatching {
        api.getCourses().body()?.data?.map { it.toDomain() } ?: emptyList()
    }

    // -------------------------------------------------------------------------
    // Mappers — response → domain
    // -------------------------------------------------------------------------

    private fun ProfileResponse.toDomain(): PublicUserProfile {
        TODO("implement when wiring to real backend")
    }

    private fun ProfileStatsResponse.toDomain(): ProfileStats {
        TODO("implement when wiring to real backend")
    }

    private fun PreferencesResponse.toDomain(): UserPreferences {
        TODO("implement when wiring to real backend")
    }

    private fun ConnectionResponse.toDomain(): Connection {
        TODO("implement when wiring to real backend")
    }

    private fun ConnectionRequestResponse.toDomain(): ConnectionRequest {
        TODO("implement when wiring to real backend")
    }

    private fun ClubResponse.toDomain(): Club {
        TODO("implement when wiring to real backend")
    }

    private fun HonorResponse.toDomain(): Honor {
        TODO("implement when wiring to real backend")
    }

    private fun HonorLeaderboardEntryResponse.toDomain(): HonorEntry {
        TODO("implement when wiring to real backend")
    }

    private fun InterestResponse.toDomain(): Interest {
        TODO("implement when wiring to real backend")
    }

    private fun CourseResponse.toDomain(): Course {
        TODO("implement when wiring to real backend")
    }

    // -------------------------------------------------------------------------
    // Mappers — domain → request
    // -------------------------------------------------------------------------

    private fun PublicUserProfile.toUpdateRequest(): UpdateProfileRequest {
        TODO("implement when wiring to real backend")
    }

    private fun UserPreferences.toRequest(): UpdatePreferencesRequest {
        TODO("implement when wiring to real backend")
    }
}