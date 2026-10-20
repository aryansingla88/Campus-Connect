package com.example.campusconnect.feature.registrations.data.mapper

import com.example.campusconnect.feature.events.data.remote.response.EventResponse
import com.example.campusconnect.feature.registrations.model.RegistrationEventHeader

object RegistrationEventMapper {
    fun toHeader(response: EventResponse): RegistrationEventHeader {
        val approvalStatus = response.approvalStatus.trim().uppercase()
        return RegistrationEventHeader(
            title = response.title,
            isPublished = approvalStatus == "VERIFIED" || approvalStatus == "APPROVED",
        )
    }
}
