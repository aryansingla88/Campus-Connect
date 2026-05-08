package com.example.campusconnect.feature.map

import com.example.campusconnect.feature.map.mapengine.MapMarker

data class MapUiState(
    val markers: List<MapMarker> = emptyList(),
    val selectedMarker: MapMarker? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)