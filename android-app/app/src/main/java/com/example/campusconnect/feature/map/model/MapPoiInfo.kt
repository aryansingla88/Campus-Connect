package com.example.campusconnect.feature.map.model

data class MapPoiInfo(
    val id: Int,                         // Strict Int ID
    val name: String,
    val category: String,
    val description: String? = null,
    val iconType: String? = null,
    val visibility: String? = null,
    val priority: Int = 0,
    val sizeString: String = "MEDIUM"
)