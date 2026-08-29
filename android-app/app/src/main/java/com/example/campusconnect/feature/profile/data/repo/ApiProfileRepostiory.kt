package com.example.campusconnect.feature.profile.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.metadata.courses.CourseRepository
import com.example.campusconnect.feature.profile.data.remote.ProfileApi
import com.example.campusconnect.feature.profile.data.mapper.toPublicUserProfile
import com.example.campusconnect.feature.profile.model.*

class ApiProfileRepository(
    private val api: ProfileApi = RetrofitClient.profileApi,
    private val courseRepository: CourseRepository
) : ProfileRepository {



    // Profile-------------------------------------------------------------

    override suspend fun getMyProfile(): Result<PublicUserProfile> {
        return try {
            val response = api.getMyProfile()

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to fetch profile")
                )
            }

            val profileResponse = response.body()?.data
                ?: return Result.failure(
                    Exception("Profile data is empty")
                )

            val profile = profileResponse.toPublicUserProfile(
                courseRepository
            )

            Result.success(profile)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //------------
    override suspend fun getProfile(
        userId: Int
    ): Result<PublicUserProfile> {

        return try {
            val response = api.getProfile(userId)

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to fetch profile")
                )
            }

            val profileResponse = response.body()?.data
                ?: return Result.failure(
                    Exception("Profile data is empty")
                )

            val profile = profileResponse.toPublicUserProfile(
                courseRepository
            )

            Result.success(profile)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //------------
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
        userId: Int
    ): Result<ProfileStats> {
        TODO()
    }


    // Connections -------------------------------------------------------------

    override suspend fun getMyConnections(): Result<List<Connection>> {
        TODO()
    }

    override suspend fun getUserConnections(
        userId: Int
    ): Result<List<Connection>> {
        TODO()
    }

    override suspend fun getConnectionRequests(): Result<List<ConnectionRequest>> {
        TODO()
    }

    override suspend fun sendConnectionRequest(
        userId: Int
    ): Result<Unit> {
        TODO()
    }

    override suspend fun acceptConnectionRequest(
        userId: Int
    ): Result<Unit> {
        TODO()
    }

    override suspend fun removeConnection(
        userId: Int
    ): Result<Unit> {
        TODO()
    }


    // Clubs -------------------------------------------------------------

    override suspend fun getMyClubs(): Result<List<Club>> {
        TODO()
    }

    override suspend fun getUserClubs(
        userId: Int
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


}