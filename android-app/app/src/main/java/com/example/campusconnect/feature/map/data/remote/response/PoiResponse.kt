package com.example.campusconnect.feature.map.data.remote.response

import com.example.campusconnect.feature.map.mapengine.model.MapMarker
import com.example.campusconnect.feature.map.mapengine.model.MarkerSize
import com.example.campusconnect.feature.map.mapengine.model.MarkerType
import com.example.campusconnect.feature.map.model.MapPoiInfo

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

// Map engine pin object conversion
fun PoiResponse.toMarker(): MapMarker {
    val poiPriority = priority ?: 1

    val markerSize = when (poiPriority) {
        1 -> MarkerSize.SMALL
        2 -> MarkerSize.MEDIUM
        else -> MarkerSize.LARGE
    }

    val highlightCheck = poiPriority == 4

    return MapMarker(
        id = "POI_$id",
        sourceId = id,
        type = MarkerType.POI,
        latitude = latitude ?: 0.0,
        longitude = longitude ?: 0.0,
        label = name,
        priority = poiPriority,
        size = markerSize,
        isHighlighted = highlightCheck
    )
}

// Bottom sheet detail object conversion
fun PoiResponse.toPoiInfo(): MapPoiInfo {
    val poiPriority = priority ?: 1

    return MapPoiInfo(
        id = id,
        name = name,
        category = category ?: "GENERAL",
        description = description,
        iconType = iconType,
        visibility = visibility,
        priority = poiPriority,
        sizeString = "MEDIUM"
    )
}