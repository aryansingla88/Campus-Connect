package com.example.campusconnect.feature.map

import androidx.lifecycle.ViewModel
import com.example.campusconnect.feature.map.mapengine.CoordinateConverter
import com.example.campusconnect.feature.map.mapengine.MarkerRenderData
import com.example.campusconnect.feature.map.mapengine.MarkerRenderer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapViewModel : ViewModel() {

    private val fakeMapService = FakeMapService()

    private val markerRenderer = MarkerRenderer()

    private val coordinateConverter = CoordinateConverter(
        mapWidth = 3000f,
        mapHeight = 3000f,
        minLat = 29.944000,
        maxLat = 29.948500,
        minLng = 76.814000,
        maxLng = 76.820000
    )

    private val _uiState = MutableStateFlow(MapUiState())

    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadMarkers()
    }

    private fun loadMarkers() {

        val markers = fakeMapService.getMarkers().map { marker ->

            val point = coordinateConverter.latLngToPoint(
                marker.latitude,
                marker.longitude
            )

            marker.copy(
                x = point.first,
                y = point.second
            )
        }

        val renderData = markerRenderer.buildMarkerRenderData(markers)

        _uiState.value = MapUiState(
            markers = markers,
            renderData = renderData
        )
    }
}