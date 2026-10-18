package com.example.campusconnect.feature.map.model

import com.example.campusconnect.feature.profile.model.ConnectionStatus

data class MapUserProfile(
    val id: Int,
    val fullName: String,
    val course: String,
    val batch: String,
    val description: String,
    val badges: List<String>,
    val medals: List<Int>,
    val mutualFriendsCount: Int,
    val avatarUrl: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.NOT_CONNECTED
)

data class MapBadge(
    val id: Int,
    val label: String,
    val emoji: String
)

data class MapMedal(
    val id: Int,
    val label: String,
    val rank: Int
)
