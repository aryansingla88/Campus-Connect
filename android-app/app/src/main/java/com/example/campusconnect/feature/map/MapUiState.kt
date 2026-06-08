package com.example.campusconnect.feature.map

import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerRenderData
import com.example.campusconnect.feature.map.mapengine.MarkerType
import com.example.campusconnect.feature.map.model.MapUserProfile

data class MapUiState(
    val markers: List<MapMarker> = emptyList(),
    val renderData: List<MarkerRenderData> = emptyList(),
    val selectedMarkerId: String? = null,
    val selectedMarker: MarkerRenderData? = null,
    val selectedUserProfile: MapUserProfile? = null,
    val activeFilter: MarkerType? = null
)