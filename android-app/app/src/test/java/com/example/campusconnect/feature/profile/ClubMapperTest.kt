package com.example.campusconnect.feature.profile

import com.example.campusconnect.feature.profile.data.mapper.ClubMapper
import com.example.campusconnect.feature.profile.data.remote.response.ClubDetailResponse
import com.example.campusconnect.feature.profile.data.remote.response.ClubMemberResponse
import com.example.campusconnect.feature.profile.data.remote.response.ClubMemberRequestResponse
import com.example.campusconnect.feature.profile.data.remote.response.ClubResponse
import com.example.campusconnect.feature.profile.model.ClubMemberFilter
import com.example.campusconnect.feature.profile.model.ClubMemberRole
import com.example.campusconnect.feature.profile.model.ClubLoadState
import com.example.campusconnect.feature.profile.model.ClubStatus
import com.example.campusconnect.feature.profile.model.clubLoadState
import com.example.campusconnect.feature.profile.model.filterClubMembers
import org.junit.Assert.assertEquals
import org.junit.Test

class ClubMapperTest {
    @Test
    fun mapsLeadClubListStatusToManageableState() {
        val club = ClubMapper.toClub(
            ClubResponse(
                clubId = 4,
                name = "Robotics",
                clubType = "CLUB",
                logoUrl = null,
                memberCount = 12,
                memberStatus = "LEAD"
            )
        )

        assertEquals(ClubStatus.LEAD, club.status)
    }

    @Test
    fun mapsApprovedClubListStatusAsJoined() {
        val club = ClubMapper.toClub(
            ClubResponse(4, "Robotics", "CLUB", null, 12, "APPROVED")
        )

        assertEquals(ClubStatus.JOINED, club.status)
    }

    @Test
    fun mapsClubDetailsAndAuthenticatedMembershipState() {
        val details = ClubMapper.toClubDetails(
            ClubDetailResponse(
                clubId = 7,
                name = "Robotics",
                clubType = "SOCIETY",
                description = "Build things",
                memberCount = 42,
                memberStatus = "PENDING",
                role = "COMMITTEE_HEAD",
            )
        )

        assertEquals(7, details.clubId)
        assertEquals("SOCIETY", details.clubType)
        assertEquals("Build things", details.description)
        assertEquals(42, details.memberCount)
        assertEquals(ClubStatus.PENDING, details.status)
        assertEquals(ClubMemberRole.COMMITTEE_HEAD, details.role)
    }

    @Test
    fun mapsClubDetailLeadershipFromAuthenticatedUsersClubTypeAndRole() {
        val clubSecretary = ClubMapper.toClubDetails(
            ClubDetailResponse(1, "Robotics", "CLUB", null, 4, "APPROVED", "SECRETARY")
        )
        val societyPresident = ClubMapper.toClubDetails(
            ClubDetailResponse(2, "Debate", "SOCIETY", null, 5, "APPROVED", "PRESIDENT")
        )
        val societySecretary = ClubMapper.toClubDetails(
            ClubDetailResponse(3, "Music", "SOCIETY", null, 6, "APPROVED", "SECRETARY")
        )
        val clubPresident = ClubMapper.toClubDetails(
            ClubDetailResponse(4, "Robotics", "CLUB", null, 7, "APPROVED", "PRESIDENT")
        )

        assertEquals(ClubStatus.LEAD, clubSecretary.status)
        assertEquals(ClubStatus.LEAD, societyPresident.status)
        assertEquals(ClubStatus.JOINED, societySecretary.status)
        assertEquals(ClubStatus.JOINED, clubPresident.status)

        val legacyStudentClub = ClubMapper.toClubDetails(
            ClubDetailResponse(5, "Chess", "Student Club", null, 8, "APPROVED", "SECRETARY")
        )
        assertEquals(ClubStatus.LEAD, legacyStudentClub.status)
    }

    @Test
    fun mapsPendingMembershipRequestApplicantFields() {
        val request = ClubMapper.toMembershipRequest(
            ClubMemberRequestResponse(
                clubId = 12,
                userId = 34,
                fullName = "Taylor Student",
                username = "taylor",
                avatarUrl = "/uploads/profiles/taylor.jpg",
                role = "MEMBER",
                memberStatus = "PENDING",
                requestedAt = "2026-11-20T10:15:00",
                joinedAt = null
            )
        )

        assertEquals(12, request.clubId)
        assertEquals(34, request.userId)
        assertEquals("Taylor Student", request.fullName)
        assertEquals("taylor", request.username)
        assertEquals("/uploads/profiles/taylor.jpg", request.avatarUrl)
        assertEquals(ClubMemberRole.MEMBER, request.role)
        assertEquals("2026-11-20T10:15:00", request.requestedAt)
    }

    @Test
    fun mapsRolesAndFiltersByRoleAndNameTogether() {
        val members = ClubMapper.toClubMembers(
            listOf(
                ClubMemberResponse(1, "Alex President", null, "PRESIDENT", null),
                ClubMemberResponse(2, "Alex Vice President", null, "VICE_PRESIDENT", null),
                ClubMemberResponse(3, "Casey Secretary", null, "SECRETARY", null),
                ClubMemberResponse(4, "Drew Joint", null, "JOINT_SECRETARY", null),
                ClubMemberResponse(5, "Elliot Additional", null, "ADDITIONAL_SECRETARY", null),
                ClubMemberResponse(6, "Frank Committee", null, "COMMITTEE_HEAD", null),
                ClubMemberResponse(7, "Jordan Member", null, "MEMBER", null),
            )
        )

        assertEquals(ClubMemberRole.PRESIDENT, members[0].role)
        assertEquals(ClubMemberRole.VICE_PRESIDENT, members[1].role)
        assertEquals(ClubMemberRole.SECRETARY, members[2].role)
        assertEquals(ClubMemberRole.JOINT_SECRETARY, members[3].role)
        assertEquals(ClubMemberRole.ADDITIONAL_SECRETARY, members[4].role)
        assertEquals(ClubMemberRole.COMMITTEE_HEAD, members[5].role)
        assertEquals(ClubMemberRole.MEMBER, members[6].role)
        assertEquals(listOf(1, 2), filterClubMembers(members, ClubMemberFilter.CORE, "").map { it.userId })
        assertEquals(listOf(3, 4, 5, 6), filterClubMembers(members, ClubMemberFilter.COORDINATORS, "").map { it.userId })
        assertEquals(listOf(3), filterClubMembers(members, ClubMemberFilter.COORDINATORS, "casey").map { it.userId })
        assertEquals(listOf(1), filterClubMembers(members, ClubMemberFilter.CORE, "alex president").map { it.userId })
        assertEquals(listOf(7), filterClubMembers(members, ClubMemberFilter.MEMBERS, "jordan").map { it.userId })
        assertEquals(emptyList<Int>(), filterClubMembers(members, ClubMemberFilter.MEMBERS, "missing").map { it.userId })
    }

    @Test
    fun clubLoadStateDistinguishesLoadingLoadedEmptyAndError() {
        val loading = clubLoadState(7, emptyMap<Int, List<String>>(), listOf(7), emptyMap())
        val loadedEmpty = clubLoadState(7, mapOf(7 to emptyList<String>()), emptyList(), emptyMap())
        val error = clubLoadState(7, emptyMap<Int, List<String>>(), emptyList(), mapOf(7 to "offline"))

        assertEquals(ClubLoadState.Loading, loading)
        assertEquals(ClubLoadState.Loaded(emptyList<String>()), loadedEmpty)
        assertEquals(ClubLoadState.Error("offline"), error)
    }
}
