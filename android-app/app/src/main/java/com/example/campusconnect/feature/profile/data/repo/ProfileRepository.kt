package com.example.campusconnect.feature.profile.data.repo


import com.example.campusconnect.feature.profile.model.*

interface ProfileRepository {

    suspend fun getMyProfile(): Result<PublicUserProfile>

    suspend fun getProfile(userId: Int): Result<PublicUserProfile>

    suspend fun updateProfile(profile: PublicUserProfile): Result<PublicUserProfile>

    suspend fun getMyStats(): Result<ProfileStats>

    suspend fun getUserStats(userId: Int): Result<ProfileStats>

    suspend fun getMyConnections(): Result<List<Connection>>

    suspend fun getUserConnections(userId: Int): Result<List<Connection>>

    suspend fun getConnectionRequests(): Result<List<ConnectionRequest>>

    suspend fun sendConnectionRequest(userId: Int): Result<Unit>

    suspend fun acceptConnectionRequest(userId: Int): Result<Unit>

    suspend fun removeConnection(userId: Int): Result<Unit>

    suspend fun getMyClubs(): Result<List<Club>>

    suspend fun getUserClubs(userId: Int): Result<List<Club>>

    suspend fun joinClub(clubId: String): Result<Unit>

    suspend fun leaveClub(clubId: String): Result<Unit>

    suspend fun getProfileHonors(): Result<ProfileHonors>

    suspend fun getSelectedInterests(): Result<List<Interest>>

    suspend fun getAllInterests(): Result<List<Interest>>

    suspend fun addInterest(interestId: String): Result<Unit>

    suspend fun removeInterest(interestId: String): Result<Unit>

}