package com.example.campusconnect.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.map.data.repo.FakeMapRepo
import com.example.campusconnect.feature.map.data.repo.MapRepo
import com.example.campusconnect.feature.map.mapengine.*
import com.example.campusconnect.feature.map.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: MapRepo = FakeMapRepo()
) : ViewModel() {

    private val markerRenderer = MarkerRenderer()
    private val coordinateConverter = MapCalibration.converter

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadMarkers()
    }

    // Modified: Removed 'search' parameter to align with updated MapRepo
    private fun loadMarkers(
        type: MarkerType? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            repository.getMarkers(type = type)
                .onSuccess { markers ->
                    val positionedMarkers = markers.map { marker ->
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
                        markers = positionedMarkers,
                        selectedMarkerId = null,
                        activeFilter = type,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to load map markers"
                    )
                }
        }
    }

    fun selectMarker(markerId: String) {
        val selectedMarker = _uiState.value.renderData.firstOrNull { marker ->
            marker.id == markerId
        } ?: return

        updateState(
            markers = _uiState.value.markers,
            selectedMarkerId = markerId,
            activeFilter = _uiState.value.activeFilter,
            selectedMarkerOverride = selectedMarker,
            selectedUserProfileOverride = null,
            selectedPoiInfoOverride = null,
            selectedEventInfoOverride = null,
            isDetailLoading = true,
            detailErrorMessage = null
        )

        loadSelectedMarkerDetails(selectedMarker)
    }

    private fun loadSelectedMarkerDetails(
        marker: MarkerRenderData
    ) {
        viewModelScope.launch {
            when (marker.type) {

                MarkerType.USER -> {
                    repository.getUserProfile(marker.id)
                        .onSuccess { profile ->
                            updateSelectedDetails(
                                selectedUserProfile = profile,
                                selectedPoiInfo = null,
                                selectedEventInfo = null
                            )
                        }
                        .onFailure { error ->
                            updateDetailError(error)
                        }
                }

                MarkerType.POI -> {
                    repository.getPoiInfo(marker.id)
                        .onSuccess { poi ->
                            updateSelectedDetails(
                                selectedUserProfile = null,
                                selectedPoiInfo = poi,
                                selectedEventInfo = null
                            )
                        }
                        .onFailure { error ->
                            updateDetailError(error)
                        }
                }

                MarkerType.EVENT -> {
                    repository.getEventInfo(marker.id)
                        .onSuccess { event ->
                            updateSelectedDetails(
                                selectedUserProfile = null,
                                selectedPoiInfo = null,
                                selectedEventInfo = event
                            )
                        }
                        .onFailure { error ->
                            updateDetailError(error)
                        }
                }

                MarkerType.SHOP -> {
                    repository.getShopInfo(marker.id)
                        .onSuccess { shop ->
                            _uiState.value = _uiState.value.copy(
                                isDetailLoading = false,
                                detailErrorMessage = null,
                                selectedUserProfile = null,
                                selectedPoiInfo = null,
                                selectedEventInfo = null
                            )
                        }
                        .onFailure {
                            _uiState.value = _uiState.value.copy(
                                isDetailLoading = false,
                                detailErrorMessage = null,
                                selectedUserProfile = null,
                                selectedPoiInfo = null,
                                selectedEventInfo = null
                            )
                        }
                }
            }
        }
    }

    fun clearSelection() {
        updateState(
            markers = _uiState.value.markers,
            selectedMarkerId = null,
            activeFilter = _uiState.value.activeFilter,
            selectedMarkerOverride = null,
            selectedUserProfileOverride = null,
            selectedPoiInfoOverride = null,
            selectedEventInfoOverride = null,
            isDetailLoading = false,
            detailErrorMessage = null
        )
    }

    // Modified: Passing only 'type' parameter
    fun setFilter(type: MarkerType?) {
        loadMarkers(type = type)
    }

    fun sendConnectionRequest(userId: String) {
        viewModelScope.launch {
            repository.sendConnectionRequest(userId)
        }
    }

    fun registerEvent(eventId: String) {
        viewModelScope.launch {
            repository.registerEvent(eventId)
        }
    }

    fun enableEventReminder(eventId: String) {
        viewModelScope.launch {
            repository.enableEventReminder(eventId)
        }
    }

    fun disableEventReminder(eventId: String) {
        viewModelScope.launch {
            repository.disableEventReminder(eventId)
        }
    }

    private fun updateSelectedDetails(
        selectedUserProfile: MapUserProfile?,
        selectedPoiInfo: MapPoiInfo?,
        selectedEventInfo: MapEventInfo?
    ) {
        _uiState.value = _uiState.value.copy(
            selectedUserProfile = selectedUserProfile,
            selectedPoiInfo = selectedPoiInfo,
            selectedEventInfo = selectedEventInfo,
            isDetailLoading = false,
            detailErrorMessage = null
        )
    }

    private fun updateDetailError(
        error: Throwable
    ) {
        _uiState.value = _uiState.value.copy(
            isDetailLoading = false,
            detailErrorMessage = error.message ?: "Unable to load marker details"
        )
    }

    private fun updateState(
        markers: List<MapMarker> = _uiState.value.markers,
        selectedMarkerId: String? = _uiState.value.selectedMarkerId,
        activeFilter: MarkerType? = _uiState.value.activeFilter,

        selectedMarkerOverride: MarkerRenderData? = _uiState.value.selectedMarker,
        selectedUserProfileOverride: MapUserProfile? = _uiState.value.selectedUserProfile,
        selectedPoiInfoOverride: MapPoiInfo? = _uiState.value.selectedPoiInfo,
        selectedEventInfoOverride: MapEventInfo? = _uiState.value.selectedEventInfo,

        isLoading: Boolean = _uiState.value.isLoading,
        errorMessage: String? = _uiState.value.errorMessage,

        isDetailLoading: Boolean = _uiState.value.isDetailLoading,
        detailErrorMessage: String? = _uiState.value.detailErrorMessage
    ) {
        val visibleMarkers = if (activeFilter == null) {
            markers
        } else {
            markers.filter { marker ->
                marker.type == activeFilter
            }
        }

        val renderData = markerRenderer.buildMarkerRenderData(
            markers = visibleMarkers,
            selectedMarkerId = selectedMarkerId
        )

        val selectedMarker = selectedMarkerOverride
            ?: renderData.firstOrNull { marker ->
                marker.id == selectedMarkerId
            }

        _uiState.value = _uiState.value.copy(
            markers = markers,
            renderData = renderData,
            selectedMarkerId = selectedMarkerId,
            selectedMarker = selectedMarker,
            selectedUserProfile = selectedUserProfileOverride,
            selectedPoiInfo = selectedPoiInfoOverride,
            selectedEventInfo = selectedEventInfoOverride,
            activeFilter = activeFilter,
            isLoading = isLoading,
            errorMessage = errorMessage,
            isDetailLoading = isDetailLoading,
            detailErrorMessage = detailErrorMessage
        )
    }
}