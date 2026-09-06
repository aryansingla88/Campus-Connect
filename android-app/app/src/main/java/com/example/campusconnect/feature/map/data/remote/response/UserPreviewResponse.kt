package com.example.campusconnect.feature.map.data.remote.response

import com.example.campusconnect.core.utils.AcademicUtils
import com.example.campusconnect.feature.map.model.MapUserProfile
import com.google.gson.annotations.SerializedName

data class UserPreviewResponse(

    @SerializedName("userId")
    val userId: Int,

    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("courseId")
    val courseId: Int?,

    @SerializedName("admissionYear")
    val admissionYear: Int?,

    @SerializedName("avatarUrl")
    val avatarUrl: String?,

    @SerializedName("bio")
    val bio: String?,

    @SerializedName("mutualConnectionsCount")
    val mutualConnectionsCount: Int?
)

// UI Model (MapUserProfile) mein map karne ke liye extension function
fun UserPreviewResponse.toMapUserProfile(
    courseData: Course?
): MapUserProfile {

    val courseName = courseData?.let {
        AcademicUtils.getCourseName(it)
    } ?: "Unknown Course"

    val batch = courseData?.let {
        AcademicUtils.getBatch(
            admissionYear = admissionYear,
            durationYears = it.durationYears
        )
    } ?: "Unknown Batch"

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