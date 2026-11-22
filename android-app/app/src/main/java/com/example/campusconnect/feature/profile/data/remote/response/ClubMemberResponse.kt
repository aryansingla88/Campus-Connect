package com.example.campusconnect.feature.profile.data.remote.response

data class ClubMemberResponse(
    val userId: Int,
    val fullName: String?,
    val avatarUrl: String?,
    val role: String,
    val joinedAt: String?
)
