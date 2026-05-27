package com.example.campusconnect.feature.map

import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerRenderData
import com.example.campusconnect.feature.map.mapengine.MarkerType

data class MapUiState(
    val markers: List<MapMarker> = emptyList(),
    val renderData: List<MarkerRenderData> = emptyList(),
    val selectedMarkerId: String? = null,
    val selectedMarker: MapMarker? = null,
    val activeFilter: MarkerType? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)