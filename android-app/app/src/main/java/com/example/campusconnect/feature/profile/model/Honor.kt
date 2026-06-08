package com.example.campusconnect.feature.profile.model

enum class HonorType {
    BADGE,
    MEDAL
}

data class ProfileHonor(
    val honorId: String,
    val type: HonorType,
    val title: String,
    val subtitle: String?,
    val iconUrl: String?,
    val priority: Int
)

data class ProfileHonors(
    val honorRank: Int,
    val badges: List<ProfileHonor>,
    val medals: List<ProfileHonor>
)