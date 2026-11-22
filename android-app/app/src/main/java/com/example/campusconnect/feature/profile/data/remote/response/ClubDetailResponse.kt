package com.example.campusconnect.feature.profile.data.remote.response

data class ClubDetailResponse(
    val clubId: Int,
    val name: String,
    val clubType: String = "CLUB",
    val description: String?,
    val memberCount: Int,
    val memberStatus: String?,
    val role: String?
)
