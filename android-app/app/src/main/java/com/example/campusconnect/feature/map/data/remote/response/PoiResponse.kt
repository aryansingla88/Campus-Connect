package com.example.campusconnect.feature.map.data.remote.response

import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerSize
import com.example.campusconnect.feature.map.mapengine.MarkerType
import com.example.campusconnect.feature.map.model.MapPoiInfo
import com.google.gson.annotations.SerializedName

data class PoiRes(
    @SerializedName("id")
    val id: Int, // Strict Int ID

    @SerializedName("name")
    val name: String,

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("latitude")
    val latitude: Double? = null,

    @SerializedName("longitude")
    val longitude: Double? = null,

    @SerializedName("iconType")
    val iconType: String? = null,

    @SerializedName("visibility")
    val visibility: String? = null,

    @SerializedName("priority")
    val priority: Int? = 0
)

// Map engine pin object conversion
fun PoiRes.toMarker(): MapMarker {
    val poiPriority = priority ?: 0
    val markerSize = if (poiPriority > 5) MarkerSize.LARGE else MarkerSize.MEDIUM

    return MapMarker(
        id = id,
        sourceId = id,
        type = MarkerType.POI,
        latitude = latitude ?: 0.0,
        longitude = longitude ?: 0.0,
        label = name,
        priority = poiPriority,
        size = markerSize
    )
}

// Bottom sheet detail object conversion
fun PoiRes.toPoiInfo(): MapPoiInfo {
    val poiPriority = priority ?: 0
    val derivedSizeString = if (poiPriority > 5) "LARGE" else "MEDIUM"

    return MapPoiInfo(
        id = id,
        name = name,
        category = category ?: "GENERAL",
        description = description,
        iconType = iconType,
        visibility = visibility,
        priority = poiPriority,
        sizeString = derivedSizeString
    )
}