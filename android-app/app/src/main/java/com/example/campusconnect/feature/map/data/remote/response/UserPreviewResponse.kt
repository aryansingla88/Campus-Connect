package com.example.campusconnect.feature.map.data.remote.response

data class UserPreviewResponse(
    val userId: Int,
    val fullName: String,
    val courseId: Int?,
    val admissionYear: Int?,
    val avatarUrl: String?,
    val bio: String?,
    val mutualConnectionsCount: Int?
)
