package com.example.campusconnect.feature.events.model

data class EventHistoryItem(
    val id: Int,
    val title: String,
    val venue: String,
    val startTime: String,
    val endTime: String?,
    val posterUrl: String?
)