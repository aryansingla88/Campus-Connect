package com.example.campusconnect.feature.map.data.mapper

import com.example.campusconnect.feature.map.data.remote.response.PoiResponse
import com.example.campusconnect.feature.map.model.MapPoiInfo
import com.example.campusconnect.feature.map.model.PoiMarkerData

object MapPoiMapper {
    /** Returns null when a POI has no usable geographic location. */
    fun toMarker(response: PoiResponse): PoiMarkerData? {
        val latitude = response.latitude ?: return null
        val longitude = response.longitude ?: return null
        if (!latitude.isFinite() || latitude !in -90.0..90.0) return null
        if (!longitude.isFinite() || longitude !in -180.0..180.0) return null

        return PoiMarkerData(
            sourceId = response.id,
            latitude = latitude,
            longitude = longitude,
            name = response.name,
            category = response.category,
            iconType = response.iconType,
            visibility = response.visibility,
            priority = response.priority ?: 1
        )
    }

    fun toInfo(response: PoiResponse): MapPoiInfo = MapPoiInfo(
        id = response.id,
        name = response.name,
        category = response.category ?: "GENERAL",
        description = response.description,
        iconType = response.iconType,
        visibility = response.visibility,
        priority = response.priority ?: 1,
        sizeString = "MEDIUM"
    )
}
