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
    val date: String,
    val time: String,
    val description: String,
    val venue: String? = null,
    val posterUrl: String? = null,
    val posterResId: Int? = null,
    val hosts: List<HostInfo> = emptyList(),
    val endTime: String? = null,
    val subtitle: String? = null,
    val registrationCount: Int? = null,
    val registrationStatus: String? = null,
    val categories: List<EventFilter> = emptyList(),
    val registrationType: String? = null,
    val registrationLink: String? = null,
    val isReminderEnabled: Boolean? = null,
    val priority: Int? = null
)
