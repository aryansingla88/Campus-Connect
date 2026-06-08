package com.example.campusconnect.feature.profile.data.repo


import com.example.campusconnect.feature.profile.model.*

interface ProfileRepository {

    suspend fun getMyProfile(): Result<PublicUserProfile>

    suspend fun getProfile(userId: String): Result<PublicUserProfile>

    suspend fun getMyStats(): Result<ProfileStats>

    suspend fun getUserStats(userId: String): Result<ProfileStats>

    suspend fun getMyConnections(): Result<List<Connection>>

    suspend fun getUserConnections(userId: String): Result<List<Connection>>

    suspend fun getConnectionRequests(): Result<List<ConnectionRequest>>

    suspend fun sendConnectionRequest(userId: String): Result<Unit>

    suspend fun acceptConnectionRequest(userId: String): Result<Unit>

    suspend fun removeConnection(userId: String): Result<Unit>

    suspend fun getMyClubs(): Result<List<Club>>

    suspend fun getUserClubs(userId: String): Result<List<Club>>

    suspend fun joinClub(clubId: String): Result<Unit>

    suspend fun leaveClub(clubId: String): Result<Unit>

    suspend fun getProfileHonors(): Result<ProfileHonors>

    suspend fun getSelectedInterests(): Result<List<Interest>>

    suspend fun getAllInterests(): Result<List<Interest>>

    suspend fun addInterest(interestId: String): Result<Unit>

    suspend fun removeInterest(interestId: String): Result<Unit>

    suspend fun getCourses(): Result<List<Course>>
}