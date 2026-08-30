package com.example.campusconnect.feature.map.data.remote.response

import com.example.campusconnect.feature.map.model.MapUserProfile
import com.google.gson.annotations.SerializedName

data class UserPreviewRes(
    @SerializedName("userId")
    val userId: Int,

    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("courseName")
    val courseName: String,

    @SerializedName("courseCode")
    val courseCode: String,

    @SerializedName("courseYear")
    val courseYear: Int,

    @SerializedName("admissionYear")
    val admissionYear: Int,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,

    @SerializedName("bio")
    val bio: String? = null,

    @SerializedName("mutualConnectionsCount")
    val mutualConnectionsCount: Int? = 0
)

fun UserPreviewRes.toMapUserProfile(): MapUserProfile {
    val endCalculatedYear = admissionYear + 4
    val formattedCourse = if (courseCode.isNotBlank()) "$courseName ($courseCode)" else courseName

    return MapUserProfile(
        id = userId, // Directly pass Int (removed .toString())
        fullName = fullName,
        course = formattedCourse,
        startYear = admissionYear,
        endYear = endCalculatedYear,
        description = bio ?: "",
        badges = emptyList(),
        medals = emptyList(),
        mutualFriendsCount = mutualConnectionsCount ?: 0
    )
}