package com.example.campusconnect.feature.profile

import com.example.campusconnect.feature.profile.data.mapper.ClubMapper
import com.example.campusconnect.feature.profile.data.remote.response.ClubDetailResponse
import com.example.campusconnect.feature.profile.data.remote.response.ClubMemberResponse
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
