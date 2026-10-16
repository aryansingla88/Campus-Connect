package com.example.campusconnect.feature.map.data.remote.response

data class PoiResponse(
    val id: Int,
    val name: String,
    val category: String? = null,
    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val iconType: String? = null,
    val visibility: String? = null,
    val priority: Int? = 1
)
