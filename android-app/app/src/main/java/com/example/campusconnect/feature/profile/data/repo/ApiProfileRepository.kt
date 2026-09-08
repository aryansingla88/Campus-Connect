package com.example.campusconnect.feature.profile.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.metadata.courses.CourseRepository
import com.example.campusconnect.feature.profile.data.mapper.*
import com.example.campusconnect.feature.profile.data.remote.ProfileApi
import com.example.campusconnect.feature.profile.data.remote.request.UpdateHonorPriorityRequest
import com.example.campusconnect.feature.profile.data.remote.request.UpdateProfileRequest
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

            val profile = ProfileMapper.toPublicUserProfile(
                response = profileResponse,
                courseRepository = courseRepository
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

            val profile = ProfileMapper.toPublicUserProfile(
                response = profileResponse,
                courseRepository = courseRepository
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

        val request = UpdateProfileRequest(
            bio = profile.bio,
            avatarUrl = profile.avatarUrl,
            hostel = profile.hostel,
            hometown = profile.hometown,
            phone = profile.phone,
            github = profile.github,
            linkedin = profile.linkedin,
            instagram = profile.instagram
        )

        return try {
            val response = api.updateProfile(request)

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception(
                        "Failed to update profile: ${response.code()}"
                    )
                )
            }

            val body = response.body()
                ?: return Result.failure(
                    Exception("Empty response from server")
                )

            val updatedProfile = body.data
                ?: return Result.failure(
                    Exception("Updated profile data is empty")
                )

            Result.success(
                ProfileMapper.toPublicUserProfile(
                    response = updatedProfile,
                    courseRepository = courseRepository
                )
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Stats -------------------------------------------------------------

    override suspend fun getMyStats(): Result<ProfileStats> {
        return try {
            val response = api.getMyStats()

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to load profile stats: ${response.code()}")
                )
            }

            val data = response.body()?.data
                ?: return Result.failure(
                    Exception("Profile stats data is empty")
                )

            Result.success(
                ProfileStats(
                    connectionCount = data.connectionCount,
                    honorCount = data.honorCount,
                    clubCount = data.clubCount,
                    interestCount = data.interestCount
                )
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserStats(
        userId: Int
    ): Result<ProfileStats> {
        return try {
            val response = api.getUserStats(userId)

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to load user stats: ${response.code()}")
                )
            }

            val data = response.body()?.data
                ?: return Result.failure(
                    Exception("User stats data is empty")
                )

            Result.success(
                ProfileStats(
                    connectionCount = data.connectionCount,
                    honorCount = data.honorCount,
                    clubCount = data.clubCount,
                    interestCount = data.interestCount
                )
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Connections -------------------------------------------------------------

    override suspend fun getMyConnections(): Result<List<Connection>> {
        return try {
            val response = api.getMyConnections()

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to fetch connections")
                )
            }

            val connectionResponses = response.body()?.data
                ?: return Result.failure(
                    Exception("Connection data is empty")
                )

            val connections = ConnectionMapper.toConnections(
                responses = connectionResponses,
                courseRepository = courseRepository
            )

            Result.success(connections)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserConnections(
        userId: Int
    ): Result<List<Connection>> {
        return try {
            val response = api.getUserConnections(userId)

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to fetch user connections")
                )
            }

            val connectionResponses = response.body()?.data
                ?: return Result.failure(
                    Exception("Connection data is empty")
                )

            val connections = ConnectionMapper.toConnections(
                responses = connectionResponses,
                courseRepository = courseRepository
            )

            Result.success(connections)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getConnectionRequests(): Result<List<ConnectionRequest>> {
        return try {
            val response = api.getConnectionRequests()

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to fetch connection requests")
                )
            }

            val requestResponses = response.body()?.data
                ?: return Result.failure(
                    Exception("Connection request data is empty")
                )

            val requests = ConnectionMapper.toConnectionRequests(
                responses = requestResponses,
                courseRepository = courseRepository
            )

            Result.success(requests)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendConnectionRequest(
        userId: Int
    ): Result<Unit> {
        return try {
            val response = api.sendConnectionRequest(userId)

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to send connection request")
                )
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptConnectionRequest(
        userId: Int
    ): Result<Unit> {
        return try {
            val response = api.acceptConnectionRequest(userId)

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to accept connection request")
                )
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeConnectionRequest(
        userId: Int
    ): Result<Unit> {
        return try {
            val response = api.removeConnectionRequest(userId)

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to remove connection request")
                )
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeConnection(
        userId: Int
    ): Result<Unit> {
        return try {
            val response = api.removeConnection(userId)

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to remove connection")
                )
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(
        query: String
    ): Result<List<Connection>> {
        return try {
            val response = api.searchUsers(query)

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to search users")
                )
            }

            val connectionResponses = response.body()?.data
                ?: return Result.failure(
                    Exception("Search result data is empty")
                )

            val connections = ConnectionMapper.toConnections(
                responses = connectionResponses,
                courseRepository = courseRepository
            )

            Result.success(connections)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

// Clubs -------------------------------------------------------------

    override suspend fun getAllClubs(): Result<List<Club>> {
        return try {
            val response = api.getAllClubs()

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to fetch all clubs")
                )
            }

            val clubResponses = response.body()?.data
                ?: return Result.failure(
                    Exception("Club data is empty")
                )

            Result.success(
                ClubMapper.toClubs(clubResponses)
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyClubs(): Result<List<Club>> {
        return try {
            val response = api.getMyClubs()

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to fetch my clubs")
                )
            }

            val clubResponses = response.body()?.data
                ?: return Result.failure(
                    Exception("Club data is empty")
                )

            Result.success(
                ClubMapper.toClubs(clubResponses)
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserClubs(
        userId: Int
    ): Result<List<Club>> {
        return try {
            val response = api.getUserClubs(userId)

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to fetch user clubs")
                )
            }

            val clubResponses = response.body()?.data
                ?: return Result.failure(
                    Exception("Club data is empty")
                )

            Result.success(
                ClubMapper.toClubs(clubResponses)
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinClub(
        clubId: Int
    ): Result<Unit> {
        return try {
            val response = api.joinClub(clubId)

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to join club")
                )
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveClub(
        clubId: Int
    ): Result<Unit> {
        return try {
            val response = api.leaveClub(clubId)

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to leave club")
                )
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Honors-------------------------------------------------------------

    override suspend fun getProfileHonors(): Result<ProfileHonors> {
        return try {
            val response = api.getMyHonors()

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception(
                        "Failed to fetch profile honors: ${response.code()}"
                    )
                )
            }

            val honorsResponse = response.body()?.data
                ?: return Result.failure(
                    Exception("Profile honors data is empty")
                )

            Result.success(
                HonorMapper.toProfileHonors(honorsResponse)
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserHonors(
        userId: Int
    ): Result<ProfileHonors> {
        return try {
            val response = api.getUserHonors(userId)

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception(
                        "Failed to fetch user honors: ${response.code()}"
                    )
                )
            }

            val honorsResponse = response.body()?.data
                ?: return Result.failure(
                    Exception("User honors data is empty")
                )

            Result.success(
                HonorMapper.toProfileHonors(honorsResponse)
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateHonorPriority(
        honorId: Int,
        priority: Int
    ): Result<Unit> {
        return try {
            val response = api.updateHonorPriority(
                UpdateHonorPriorityRequest(
                    honorId = honorId,
                    priority = priority
                )
            )

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception(
                        "Failed to update honor priority: ${response.code()}"
                    )
                )
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Interests-------------------------------------------------------------

    override suspend fun getSelectedInterests(): Result<List<Interest>> {
        return try {
            val response = api.getMyInterests()

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to fetch selected interests")
                )
            }

            val interestResponses = response.body()?.data
                ?: return Result.failure(
                    Exception("Selected interests data is empty")
                )

            Result.success(
                interestResponses.map { interestResponse ->
                    Interest(
                        interestId = interestResponse.interestId,
                        label = interestResponse.label,
                        category = interestResponse.category
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserInterests(
        userId: Int
    ): Result<List<Interest>> {
        return try {
            val response = api.getUserInterests(userId)

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to fetch user interests")
                )
            }

            val interestResponses = response.body()?.data
                ?: return Result.failure(
                    Exception("User interests data is empty")
                )

            Result.success(
                interestResponses.map { interestResponse ->
                    Interest(
                        interestId = interestResponse.interestId,
                        label = interestResponse.label,
                        category = interestResponse.category
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllInterests(): Result<List<Interest>> {
        return try {
            val response = api.getAllInterests()

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to fetch all interests")
                )
            }

            val interestResponses = response.body()?.data
                ?: return Result.failure(
                    Exception("All interests data is empty")
                )

            Result.success(
                interestResponses.map { interestResponse ->
                    Interest(
                        interestId = interestResponse.interestId,
                        label = interestResponse.label,
                        category = interestResponse.category
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addInterest(
        interestId: Int
    ): Result<Unit> {
        return try {
            val response = api.addInterest(interestId)

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to add interest")
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeInterest(
        interestId: Int
    ): Result<Unit> {
        return try {
            val response = api.removeInterest(interestId)

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to remove interest")
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}