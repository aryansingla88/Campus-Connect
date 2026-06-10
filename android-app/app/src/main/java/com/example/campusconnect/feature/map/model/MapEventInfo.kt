package com.example.campusconnect.feature.map.model

data class MapEventInfo(
    val id: String,
    val title: String,
    val hostName: String,
    val date: String,
    val time: String,
    val description: String,
    val posterResId: Int? = null
)