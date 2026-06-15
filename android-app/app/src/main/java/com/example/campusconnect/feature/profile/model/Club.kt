package com.example.campusconnect.feature.profile.model

enum class ClubStatus {  NOT_JOINED, PENDING, JOINED }

data class Club(
    val clubId: String,
    val name: String,
    val logoUrl: String?,
    val memberCount: Int,
    val status: ClubStatus
)