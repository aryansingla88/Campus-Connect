package com.example.campusconnect.feature.profile.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.profile.data.remote.ProfileApi
import com.example.campusconnect.feature.profile.model.*

class ApiProfileRepository(
    private val api: ProfileApi = RetrofitClient.profileApi
) : ProfileRepository {



    // Profile-------------------------------------------------------------

    override suspend fun getMyProfile(): Result<PublicUserProfile> {
        TODO()
    }

    override suspend fun getProfile(
        userId: String
    ): Result<PublicUserProfile> {
        TODO()
    }

    override suspend fun updateProfile(
        profile: PublicUserProfile
    ): Result<PublicUserProfile> {
        TODO()
    }


    // Stats -------------------------------------------------------------

    override suspend fun getMyStats(): Result<ProfileStats> {
        TODO()
    }

    override suspend fun getUserStats(
        userId: String
    ): Result<ProfileStats> {
        TODO()
    }


    // Connections -------------------------------------------------------------

    override suspend fun getMyConnections(): Result<List<Connection>> {
        TODO()
    }

    override suspend fun getUserConnections(
        userId: String
    ): Result<List<Connection>> {
        TODO()
    }

    override suspend fun getConnectionRequests(): Result<List<ConnectionRequest>> {
        TODO()
    }

    override suspend fun sendConnectionRequest(
        userId: String
    ): Result<Unit> {
        TODO()
    }

    override suspend fun acceptConnectionRequest(
        userId: String
    ): Result<Unit> {
        TODO()
    }

    override suspend fun removeConnection(
        userId: String
    ): Result<Unit> {
        TODO()
    }


    // Clubs -------------------------------------------------------------

    override suspend fun getMyClubs(): Result<List<Club>> {
        TODO()
    }

    override suspend fun getUserClubs(
        userId: String
    ): Result<List<Club>> {
        TODO()
    }

    override suspend fun joinClub(
        clubId: String
    ): Result<Unit> {
        TODO()
    }

    override suspend fun leaveClub(
        clubId: String
    ): Result<Unit> {
        TODO()
    }


    // Honors-------------------------------------------------------------

    override suspend fun getProfileHonors(): Result<ProfileHonors> {
        TODO()
    }


    // Interests-------------------------------------------------------------
    override suspend fun getSelectedInterests(): Result<List<Interest>> {
        TODO()
    }

    override suspend fun getAllInterests(): Result<List<Interest>> {
        TODO()
    }

    override suspend fun addInterest(
        interestId: String
    ): Result<Unit> {
        TODO()
    }

    override suspend fun removeInterest(
        interestId: String
    ): Result<Unit> {
        TODO()
    }

    // Courses-------------------------------------------------------------

    override suspend fun getCourses(): Result<List<Course>> {
        TODO()
    }
}