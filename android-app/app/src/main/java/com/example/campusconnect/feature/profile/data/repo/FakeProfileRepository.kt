package com.example.campusconnect.feature.profile.data.repo

import com.example.campusconnect.feature.profile.data.fake.*
import com.example.campusconnect.feature.profile.model.*

class FakeProfileRepository : ProfileRepository {

    // -------------------------------------------------------------------------
    // Profile
    // -------------------------------------------------------------------------

    override suspend fun getMyProfile() =
        Result.success(FakeProfileService.getMyProfile())

    override suspend fun getProfile(userId: String) =
        Result.success(FakeProfileService.getProfile(userId))

    private val honors = FakeHonorService.getProfileHonors()
    override suspend fun getMyStats() =
        Result.success(
            ProfileStats(
                connectionCount = FakeConnectionsService.getConnections().size,
                honorCount = honors.badges.size + honors.medals.size,
                clubCount = FakeClubsService.getClubs().size,
                interestCount = FakeInterestsService.getSelectedInterests().size
            )
        )

    override suspend fun getUserStats(userId: String) =
        getMyStats()

    // -------------------------------------------------------------------------
    // Connections
    // -------------------------------------------------------------------------

    override suspend fun getMyConnections() =
        Result.success(FakeConnectionsService.getConnections())

    override suspend fun getUserConnections(userId: String) =
        Result.success(FakeConnectionsService.getConnections())

    override suspend fun getConnectionRequests() =
        Result.success(
            FakeRequestsService.getIncomingRequests() +
                    FakeRequestsService.getSentInvites()
        )

    override suspend fun sendConnectionRequest(userId: String) =
        Result.success(Unit)

    override suspend fun acceptConnectionRequest(userId: String) =
        Result.success(Unit)

    override suspend fun removeConnection(userId: String) =
        Result.success(Unit)

    // -------------------------------------------------------------------------
    // Clubs
    // -------------------------------------------------------------------------

    override suspend fun getMyClubs() =
        Result.success(FakeClubsService.getClubs())

    override suspend fun getUserClubs(userId: String) =
        Result.success(FakeClubsService.getClubs())

    override suspend fun joinClub(clubId: String) =
        Result.success(Unit)

    override suspend fun leaveClub(clubId: String) =
        Result.success(Unit)

    // -------------------------------------------------------------------------
    // Honors
    // -------------------------------------------------------------------------

    override suspend fun getProfileHonors() =
        Result.success(FakeHonorService.getProfileHonors())

    // -------------------------------------------------------------------------
    // Interests
    // -------------------------------------------------------------------------

    override suspend fun getSelectedInterests() =
        Result.success(FakeInterestsService.getSelectedInterests())

    override suspend fun getAllInterests() =
        Result.success(FakeInterestsService.getAllInterests())

    override suspend fun addInterest(interestId: String) =
        Result.success(Unit)

    override suspend fun removeInterest(interestId: String) =
        Result.success(Unit)

    // -------------------------------------------------------------------------
    // Courses
    // -------------------------------------------------------------------------

    override suspend fun getCourses() =
        Result.success(emptyList<Course>())
}