package com.example.campusconnect.feature.profile.data.mapper

import com.example.campusconnect.feature.profile.data.remote.response.ClubResponse
import com.example.campusconnect.feature.profile.model.Club
import com.example.campusconnect.feature.profile.model.ClubStatus

object ClubMapper {

    fun toClub(response: ClubResponse): Club {
        return Club(
            clubId = response.clubId,
            name = response.name,
            logoUrl = response.logoUrl,
            memberCount = response.memberCount,
            status = mapStatus(response.memberStatus)
        )
    }

    fun toClubs(responses: List<ClubResponse>): List<Club> {
        return responses.map(::toClub)
    }

    private fun mapStatus(status: String?): ClubStatus {
        return when (status) {
            "PENDING" -> ClubStatus.PENDING
            "APPROVED" -> ClubStatus.JOINED
            else -> ClubStatus.NOT_JOINED
        }
    }
}