package com.example.campusconnect.feature.map.model

data class MapUserProfile(
    val userId: String,
    val fullName: String,
    val username: String,
    val course: String,
    val batch: String,
    val description: String,
    val badges: List<MapBadge> = emptyList(),
    val medals: List<MapMedal> = emptyList(),
    val isFriend: Boolean = false
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