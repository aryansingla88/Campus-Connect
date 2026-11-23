package com.example.campusconnect.feature.profile.data.mapper

import com.example.campusconnect.feature.profile.data.remote.response.ClubResponse
import com.example.campusconnect.feature.profile.data.remote.response.ClubDetailResponse
import com.example.campusconnect.feature.profile.data.remote.response.ClubMemberResponse
import com.example.campusconnect.feature.profile.data.remote.response.ClubMemberRequestResponse
import com.example.campusconnect.feature.profile.model.Club
import com.example.campusconnect.feature.profile.model.ClubDetails
import com.example.campusconnect.feature.profile.model.ClubMember
import com.example.campusconnect.feature.profile.model.ClubMembershipRequest
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

    fun toClubDetails(response: ClubDetailResponse): ClubDetails {
        val role = response.role?.let(::toRole)
        val type = response.clubType.trim().uppercase().replace("_", " ")
        val isLeader = response.memberStatus == "APPROVED" && when (type) {
            "CLUB", "STUDENT CLUB" -> role == ClubMemberRole.SECRETARY
            "SOCIETY" -> role == ClubMemberRole.PRESIDENT
            else -> false
        }

        return ClubDetails(
            clubId = response.clubId,
            name = response.name,
            clubType = response.clubType,
            description = response.description,
            memberCount = response.memberCount,
            status = if (isLeader) ClubStatus.LEAD else toStatus(response.memberStatus),
            role = role
        )
    }

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

    fun toMembershipRequest(response: ClubMemberRequestResponse): ClubMembershipRequest =
        ClubMembershipRequest(
            clubId = response.clubId,
            userId = response.userId,
            fullName = response.fullName.orEmpty().ifBlank { "Campus member" },
            username = response.username?.takeIf(String::isNotBlank),
            avatarUrl = response.avatarUrl,
            role = toRole(response.role.orEmpty()),
            requestedAt = response.requestedAt
        )

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
            "APPROVED" -> ClubStatus.JOINED
            "LEAD" -> ClubStatus.LEAD
            else -> ClubStatus.NOT_JOINED
        }
    }
}
