package com.example.campusconnect.feature.profile.data.mapper

import com.example.campusconnect.feature.profile.data.remote.response.HonorResponse
import com.example.campusconnect.feature.profile.data.remote.response.ProfileHonorsResponse
import com.example.campusconnect.feature.profile.model.HonorType
import com.example.campusconnect.feature.profile.model.ProfileHonor
import com.example.campusconnect.feature.profile.model.ProfileHonors

object HonorMapper {

    fun toProfileHonors(
        response: ProfileHonorsResponse
    ): ProfileHonors {
        return ProfileHonors(
            honorRank = response.honorRank,
            badges = response.badges.map(::toHonor),
            medals = response.medals.map(::toHonor)
        )
    }

    fun toHonor(
        response: HonorResponse
    ): ProfileHonor {
        return ProfileHonor(
            honorId = response.honorId,
            type = mapHonorType(response.type),
            title = response.title,
            subtitle = response.subtitle,
            iconUrl = response.iconUrl,
            priority = response.priority
        )
    }

    private fun mapHonorType(
        type: String
    ): HonorType {
        return when (type.uppercase()) {
            "BADGE" -> HonorType.BADGE
            "MEDAL" -> HonorType.MEDAL
            else -> error("Unknown honor type: $type")
        }
    }
}