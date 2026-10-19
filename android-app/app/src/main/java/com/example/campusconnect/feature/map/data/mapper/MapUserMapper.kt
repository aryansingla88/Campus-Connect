package com.example.campusconnect.feature.map.data.mapper

import com.example.campusconnect.core.utils.AcademicUtils
import com.example.campusconnect.feature.map.data.remote.response.UserMapResponse
import com.example.campusconnect.feature.map.data.remote.response.UserPreviewResponse
import com.example.campusconnect.feature.map.model.MapUserProfile
import com.example.campusconnect.feature.map.model.UserMarkerData
import com.example.campusconnect.feature.profile.model.ConnectionStatus
import com.example.campusconnect.feature.metadata.courses.Course

object MapUserMapper {
    fun toMarker(response: UserMapResponse): UserMarkerData = UserMarkerData(
        sourceId = response.userId,
        latitude = response.latitude,
        longitude = response.longitude,
        username = response.username,
        insideCampus = response.insideCampus,
        userGender = response.gender?.lowercase()
    )

    fun toProfile(
        response: UserPreviewResponse,
        course: Course?,
        relationshipStatus: String? = null
    ): MapUserProfile {
        val courseName = course?.let(AcademicUtils::getCourseName) ?: "Unknown Course"
        val batch = if (course != null && response.admissionYear != null) {
            AcademicUtils.getBatch(response.admissionYear, course.durationYears)
        } else "Unknown Batch"
        return MapUserProfile(
            id = response.userId,
            fullName = response.fullName,
            course = courseName,
            batch = batch,
            description = response.bio.orEmpty(),
            badges = emptyList(),
            medals = emptyList(),
            mutualFriendsCount = response.mutualConnectionsCount ?: 0,
            avatarUrl = response.avatarUrl,
            connectionStatus = when (relationshipStatus) {
                "PENDING_SENT", "PENDING_RECEIVED" -> ConnectionStatus.PENDING
                "CONNECTED" -> ConnectionStatus.CONNECTED
                else -> ConnectionStatus.NOT_CONNECTED
            }
        )
    }
}
