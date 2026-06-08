package com.example.campusconnect.feature.map

import androidx.lifecycle.ViewModel
import com.example.campusconnect.feature.map.data.fake.FakeMapUserProfileService
import com.example.campusconnect.feature.map.mapengine.MapCalibration
import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerRenderData
import com.example.campusconnect.feature.map.mapengine.MarkerRenderer
import com.example.campusconnect.feature.map.mapengine.MarkerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapViewModel : ViewModel() {

    private val fakeUserProfileService = FakeMapUserProfileService()
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

        updateState(
            markers = markers,
            selectedMarkerId = null,
            activeFilter = null
        )
    }

    fun selectMarker(markerId: String) {
        val selectedMarker = _uiState.value.renderData.firstOrNull { it.id == markerId }
            ?: return

        val selectedProfile = if (selectedMarker.type == MarkerType.USER) {
            fakeUserProfileService.getProfileByMarkerId(markerId)
        } else {
            null
        }

        updateState(
            markers = _uiState.value.markers,
            selectedMarkerId = markerId,
            activeFilter = _uiState.value.activeFilter,
            selectedMarkerOverride = selectedMarker,
            selectedUserProfileOverride = selectedProfile
        )
    }

    fun clearSelection() {
        updateState(
            markers = _uiState.value.markers,
            selectedMarkerId = null,
            activeFilter = _uiState.value.activeFilter,
            selectedMarkerOverride = null,
            selectedUserProfileOverride = null
        )
    }

    fun setFilter(type: MarkerType?) {
        updateState(
            markers = _uiState.value.markers,
            selectedMarkerId = null,
            activeFilter = type,
            selectedMarkerOverride = null,
            selectedUserProfileOverride = null
        )
    }

    private fun updateState(
        markers: List<MapMarker> = _uiState.value.markers,
        selectedMarkerId: String? = _uiState.value.selectedMarkerId,
        activeFilter: MarkerType? = _uiState.value.activeFilter,
        selectedMarkerOverride: MarkerRenderData? = _uiState.value.selectedMarker,
        selectedUserProfileOverride: com.example.campusconnect.feature.map.model.MapUserProfile? =
            _uiState.value.selectedUserProfile
    ) {
        val visibleMarkers = if (activeFilter == null) {
            markers
        } else {
            markers.filter { it.type == activeFilter }
        }

        val renderData = markerRenderer.buildMarkerRenderData(
            markers = visibleMarkers,
            selectedMarkerId = selectedMarkerId
        )

        val selectedMarker = selectedMarkerOverride
            ?: renderData.firstOrNull { it.id == selectedMarkerId }

        _uiState.value = _uiState.value.copy(
            markers = markers,
            renderData = renderData,
            selectedMarkerId = selectedMarkerId,
            selectedMarker = selectedMarker,
            selectedUserProfile = selectedUserProfileOverride,
            activeFilter = activeFilter
        )
    }
}