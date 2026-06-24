package com.example.campusconnect.feature.map.model

import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerRenderData
import com.example.campusconnect.feature.map.mapengine.MarkerType

data class MapUiState(
    val markers: List<MapMarker> = emptyList(),
    val renderData: List<MarkerRenderData> = emptyList(),

    val selectedMarkerId: String? = null,
    val selectedMarker: MarkerRenderData? = null,

    val selectedUserProfile: MapUserProfile? = null,
    val selectedPoiInfo: MapPoiInfo? = null,
    val selectedEventInfo: MapEventInfo? = null,

    val activeFilter: MarkerType? = null,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val isDetailLoading: Boolean = false,
    val detailErrorMessage: String? = null
)