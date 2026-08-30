package com.example.campusconnect.feature.map.model

data class MapUserProfile(
    val id: Int,
    val fullName: String,
    val course: String,
    val startYear: Int,
    val endYear: Int,
    val description: String,
    val badges: List<String>,
    val medals: List<Int>,
    val mutualFriendsCount: Int
)

data class MapBadge(
    val id: String,
    val label: String,
    val emoji: String
)

data class MapMedal(
    val id: String,
    val label: String,
    val rank: Int
)