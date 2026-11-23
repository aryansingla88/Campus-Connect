package com.example.campusconnect.feature.profile.data.remote.response

data class ClubMemberRequestResponse(
    val clubId: Int,
    val userId: Int,
    val fullName: String?,
    val username: String?,
    val avatarUrl: String?,
    val role: String?,
    val memberStatus: String?,
    val requestedAt: String?,
    val joinedAt: String?
)
