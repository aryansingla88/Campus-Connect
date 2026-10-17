package com.example.campusconnect.feature.map.data.remote.response

data class EventPreviewResponse(
    val id: Int,
    val title: String,
    val description: String? = null,
    val posterUrl: String? = null,
    val hostName: String? = null,
    val creatorName: String? = null,
    val clubName: String? = null,
    val registrationCount: Int? = null,
    val eventType: String? = null,
    val categories: List<EventCategoryResponse>? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val venue: String? = null,
    val registrationType: String? = null,
    val registrationLink: String? = null,
    val isJoined: Boolean? = null,
    val isReminderEnabled: Boolean? = null,
    val priority: Int? = null,
    val hosts: List<EventHostResponse>? = null
)


data class EventHostResponse(

    val userId: Int,
    val fullName: String,
    val avatarUrl: String? = null,
    val role: String? = null
)
