package com.example.campusconnect.feature.map

import androidx.lifecycle.ViewModel
import com.example.campusconnect.feature.map.mapengine.CoordinateConverter
import com.example.campusconnect.feature.map.mapengine.MarkerRenderer
import com.example.campusconnect.feature.map.mapengine.MarkerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.campusconnect.feature.map.mapengine.MapCalibration
class MapViewModel : ViewModel() {

    private val fakeMapService = FakeMapService()
    private val markerRenderer = MarkerRenderer()

    private val coordinateConverter = MapCalibration.converter

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadMarkers()
    }

    private fun loadMarkers() {
        val markers = fakeMapService.getMarkers().map { marker ->
            val point = coordinateConverter.latLngToPoint(
                lat = marker.latitude,
                lng = marker.longitude
            )

            marker.copy(
                x = point.x,
                y = point.y
            )
        }

        updateState(markers = markers)
    }

    fun selectMarker(markerId: String) {
        val selectedMarker = _uiState.value.markers.firstOrNull {
            it.id == markerId
        }

        updateState(
            selectedMarkerId = markerId,
            selectedMarker = selectedMarker
        )
    }

    fun clearSelection() {
        updateState(
            selectedMarkerId = null,
            selectedMarker = null
        )
    }

    fun setFilter(type: MarkerType?) {
        updateState(
            activeFilter = type,
            selectedMarkerId = null,
            selectedMarker = null
        )
    }

    private fun updateState(
        markers: List<com.example.campusconnect.feature.map.mapengine.MapMarker> = _uiState.value.markers,
        selectedMarkerId: String? = _uiState.value.selectedMarkerId,
        selectedMarker: com.example.campusconnect.feature.map.mapengine.MapMarker? = _uiState.value.selectedMarker,
        activeFilter: MarkerType? = _uiState.value.activeFilter
    ) {
        val visibleMarkers = if (activeFilter == null) {
            markers
        } else {
            markers.filter { it.type == activeFilter }
        }

        _uiState.value = _uiState.value.copy(
            markers = markers,
            selectedMarkerId = selectedMarkerId,
            selectedMarker = selectedMarker,
            activeFilter = activeFilter,
            renderData = markerRenderer.buildMarkerRenderData(
                markers = visibleMarkers,
                selectedMarkerId = selectedMarkerId
            )
        )
    }
}