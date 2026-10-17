package com.example.campusconnect.feature.map.data.local

import com.example.campusconnect.feature.map.data.remote.response.PoiResponse

fun PoiResponse.toEntity() = PoiEntity(
    id = id,
    name = name,
    category = category,
    description = description,
    latitude = latitude,
    longitude = longitude,
    iconType = iconType,
    visibility = visibility,
    priority = priority
)

fun PoiEntity.toResponse() = PoiResponse(
    id = id,
    name = name,
    category = category,
    description = description,
    latitude = latitude,
    longitude = longitude,
    iconType = iconType,
    visibility = visibility,
    priority = priority
)
