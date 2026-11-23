package com.example.campusconnect.feature.profile.data.repo


import android.net.Uri
import com.example.campusconnect.feature.profile.model.*

interface ProfileRepository {

//    ---- Profile
    suspend fun getMyProfile(): Result<PublicUserProfile>

    suspend fun getProfile(userId: Int): Result<PublicUserProfile>

    suspend fun updateProfile(
        profile: PublicUserProfile,
        imageUri: Uri? = null
    ): Result<PublicUserProfile>

    suspend fun getMyStats(): Result<ProfileStats>

    suspend fun getUserStats(userId: Int): Result<ProfileStats>

//    ----- Connections

    suspend fun getMyConnections(): Result<List<Connection>>

    suspend fun getUserConnections(userId: Int): Result<List<Connection>>

    suspend fun getConnectionRequests(): Result<List<ConnectionRequest>>

    suspend fun sendConnectionRequest(userId: Int): Result<Unit>

    suspend fun acceptConnectionRequest(userId: Int): Result<Unit>


    suspend fun removeConnectionRequest(userId: Int): Result<Unit>

    suspend fun removeConnection(userId: Int): Result<Unit>

    suspend fun searchUsers(query: String): Result<List<Connection>>

    suspend fun getAllClubs(): Result<List<Club>>

    suspend fun getMyClubs(): Result<List<Club>>

    suspend fun getUserClubs(userId: Int): Result<List<Club>>

    suspend fun getClubDetails(clubId: Int): Result<ClubDetails>

    suspend fun getClubMembers(clubId: Int): Result<List<ClubMember>>

    suspend fun getClubRequests(clubId: Int): Result<List<ClubMembershipRequest>>

    suspend fun approveClubRequest(
        clubId: Int,
        userId: Int,
        role: ClubMemberRole
    ): Result<ClubMembershipRequest>

    suspend fun rejectClubRequest(clubId: Int, userId: Int): Result<Unit>

    suspend fun joinClub(clubId: Int): Result<ClubStatus>

    suspend fun leaveClub(clubId: Int): Result<ClubStatus>

    suspend fun getProfileHonors(): Result<ProfileHonors>

    suspend fun getUserHonors(userId: Int): Result<ProfileHonors>

    suspend fun updateHonorPriority(
        honorId: Int,
        priority: Int
    ): Result<Unit>

    suspend fun getSelectedInterests(): Result<List<Interest>>

    suspend fun getUserInterests(userId: Int): Result<List<Interest>>

    suspend fun getAllInterests(): Result<List<Interest>>

    suspend fun addInterest(interestId: Int): Result<Unit>

    suspend fun removeInterest(interestId: Int): Result<Unit>

}
