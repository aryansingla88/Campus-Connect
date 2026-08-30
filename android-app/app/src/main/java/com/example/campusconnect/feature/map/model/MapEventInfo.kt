package com.example.campusconnect.feature.map.model

data class HostInfo(
    val id: Int,
    val name: String,
    val avatarUrl: String? = null
)

data class MapEventInfo(
    val id: Int,                         // Strict Int ID
    val title: String,
    val hostName: String,
    val date: String,
    val time: String,
    val description: String,
    val venue: String? = null,
    val posterUrl: String? = null,
    val posterResId: Int? = null,
    val hosts: List<HostInfo> = emptyList(),
    val isJoined: Boolean = false,
)