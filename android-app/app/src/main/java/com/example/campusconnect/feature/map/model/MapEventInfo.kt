package com.example.campusconnect.feature.map.model

data class HostInfo(
    val id: Int,
    val name: String,
    val avatarUrl: String? = null,
    val role: String? = null
)

data class MapEventInfo(
    val id: Int,
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
    val endTime: String? = null,
    val creatorName: String? = null,
    val clubName: String? = null,
    val registrationCount: Int? = null,
    val eventType: String? = null,
    val categories: List<EventFilter> = emptyList(),
    val registrationType: String? = null,
    val registrationLink: String? = null,
    val isReminderEnabled: Boolean? = null,
    val priority: Int? = null
)
