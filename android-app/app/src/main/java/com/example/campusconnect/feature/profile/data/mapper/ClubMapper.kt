package com.example.campusconnect.feature.profile.data.mapper

import com.example.campusconnect.feature.profile.data.remote.response.ClubResponse
import com.example.campusconnect.feature.profile.data.remote.response.ClubDetailResponse
import com.example.campusconnect.feature.profile.data.remote.response.ClubMemberResponse
import com.example.campusconnect.feature.profile.model.Club
import com.example.campusconnect.feature.profile.model.ClubDetails
import com.example.campusconnect.feature.profile.model.ClubMember
import com.example.campusconnect.feature.profile.model.ClubMemberRole
import com.example.campusconnect.feature.profile.model.ClubStatus

object ClubMapper {

    fun toClub(response: ClubResponse): Club {
        return Club(
            clubId = response.clubId,
            name = response.name,
            clubType = response.clubType,
            logoUrl = response.logoUrl,
            memberCount = response.memberCount,
            status = toStatus(response.memberStatus)
        )
    }

    fun toClubs(responses: List<ClubResponse>): List<Club> {
        return responses.map(::toClub)
    }

    fun toClubDetails(response: ClubDetailResponse) = ClubDetails(
        clubId = response.clubId,
        name = response.name,
        clubType = response.clubType,
        description = response.description,
        memberCount = response.memberCount,
        status = toStatus(response.memberStatus),
        role = response.role?.let(::toRole)
    )

    fun toClubMembers(responses: List<ClubMemberResponse>): List<ClubMember> =
        responses.map { response ->
            ClubMember(
                userId = response.userId,
                fullName = response.fullName.orEmpty().ifBlank { "Campus member" },
                avatarUrl = response.avatarUrl,
                role = toRole(response.role),
                joinedAt = response.joinedAt
            )
        }

    fun toRole(role: String): ClubMemberRole = when (role.trim().uppercase()) {
        "PRESIDENT" -> ClubMemberRole.PRESIDENT
        "VICE_PRESIDENT" -> ClubMemberRole.VICE_PRESIDENT
        "SECRETARY" -> ClubMemberRole.SECRETARY
        "JOINT_SECRETARY" -> ClubMemberRole.JOINT_SECRETARY
        "ADDITIONAL_SECRETARY" -> ClubMemberRole.ADDITIONAL_SECRETARY
        "COMMITTEE_HEAD" -> ClubMemberRole.COMMITTEE_HEAD
        else -> ClubMemberRole.MEMBER
    }

    fun toStatus(status: String?): ClubStatus {
        return when (status) {
            "PENDING" -> ClubStatus.PENDING
            "APPROVED", "LEAD" -> ClubStatus.JOINED
            else -> ClubStatus.NOT_JOINED
        }
    }
}
