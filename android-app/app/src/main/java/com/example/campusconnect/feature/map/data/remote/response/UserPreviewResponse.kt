package com.example.campusconnect.feature.map.data.remote.response

import com.example.campusconnect.core.utils.AcademicUtils
import com.example.campusconnect.feature.map.model.MapUserProfile
import com.example.campusconnect.feature.metadata.courses.Course
import com.google.gson.annotations.SerializedName

data class UserPreviewResponse(

    val userId: Int,

    val fullName: String,

    val courseId: Int?,

    val admissionYear: Int?,

    val avatarUrl: String?,

    val bio: String?,

    val mutualConnectionsCount: Int?
)

// UI Model (MapUserProfile) mein map karne ke liye extension function
fun UserPreviewResponse.toMapUserProfile(
    courseData: Course?
): MapUserProfile {

    val courseName = courseData?.let {
        AcademicUtils.getCourseName(it)
    } ?: "Unknown Course"

    val batch = if (
        courseData != null &&
        admissionYear != null
    ) {
        AcademicUtils.getBatch(
            admissionYear = admissionYear,
            durationYears = courseData.durationYears
        )
    } else {
        "Unknown Batch"
    }

    return MapUserProfile(
        id = userId,
        fullName = fullName,
        course = courseName,
        batch = batch,
        description = bio.orEmpty(),
        badges = emptyList(),
        medals = emptyList(),
        mutualFriendsCount = mutualConnectionsCount ?: 0
    )
}