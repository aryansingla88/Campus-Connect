package com.example.campusconnect.feature.profile.data.mapper

import com.example.campusconnect.feature.profile.data.remote.response.InterestResponse
import com.example.campusconnect.feature.profile.model.Interest

object InterestMapper {

    fun toInterest(response: InterestResponse): Interest {
        return Interest(
            interestId = response.interestId,
            label = response.label,
            category = response.category
        )
    }

    fun toInterests(
        responses: List<InterestResponse>
    ): List<Interest> {
        return responses.map(::toInterest)
    }
}