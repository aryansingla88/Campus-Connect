package com.example.campusconnect.feature.map.ui


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.map.data.repo.ApiMapRepo
import com.example.campusconnect.feature.map.data.repo.MapRepo
import com.example.campusconnect.feature.map.mapengine.MapCalibration
import com.example.campusconnect.feature.map.mapengine.model.MarkerType
import com.example.campusconnect.feature.map.model.*
import com.example.campusconnect.feature.metadata.courses.CourseRepositoryProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val courseRepository =
        CourseRepositoryProvider.getRepository(
            application.applicationContext
        )

    private val repository: MapRepo =
        ApiMapRepo(
            courseRepository = courseRepository
        )
    private val coordinateConverter = MapCalibration.converter
    private var markerLoadJob: Job? = null
    private var markerLoadRequestId = 0L
    private var detailLoadJob: Job? = null
    private val markerCache = mutableMapOf<MarkerType, List<MapMarker>>()
    private val loadedMarkerTypes = mutableSetOf<MarkerType>()

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadMarkersForMode(_uiState.value.mode)
    }

    /** All mode changes enter through this transition and clear incompatible UI state. */
    fun selectMode(mode: MapMode) {
        if (_uiState.value.mode == mode) {
            if (_uiState.value.errorMessage != null) loadMarkersForMode(mode)
            return
        }
        loadMarkersForMode(mode)
    }

    private fun loadMarkersForMode(mode: MapMode) {
        markerLoadJob?.cancel()
        val requestId = ++markerLoadRequestId
        detailLoadJob?.cancel()
        detailLoadJob = null

        val markerTypes = markerTypesForMode(mode)
        val missingTypes = markerTypes.filterNot { it in loadedMarkerTypes }
        val filterContext = filterContextForMode(mode)

        _uiState.value = _uiState.value.copy(
            mode = mode,
            markers = emptyList(),
            renderMarkers = emptyList(),
            selectedMarkerId = null,
            selectedMarker = null,
            selectedUserProfile = null,
            selectedPoiInfo = null,
            selectedEventInfo = null,
            activeFilter = null,
            filterContext = filterContext,
            isLoading = missingTypes.isNotEmpty(),
            errorMessage = null,
            isDetailLoading = false,
            detailErrorMessage = null
        )

        if (missingTypes.isEmpty()) {
            publishModeMarkers(mode, markerTypes)
            return
        }

        markerLoadJob = viewModelScope.launch {
            val results = coroutineScope {
                missingTypes.map { type ->
                    async { type to repository.getMarkers(type) }
                }.awaitAll()
                }
            if (requestId != markerLoadRequestId) return@launch

            var errorMessage: String? = null
            results.forEach { (type, result) ->
                result.onSuccess { markers ->
                    markerCache[type] = markers.map(::positionMarker)
                    loadedMarkerTypes += type
                }.onFailure { error ->
                    if (errorMessage == null) {
                        errorMessage = error.message ?: "Unable to load map markers"
                    }
                }
            }

            publishModeMarkers(mode, markerTypes, errorMessage)
        }
    }

    private fun publishModeMarkers(
        mode: MapMode,
        markerTypes: List<MarkerType>,
        errorMessage: String? = null
    ) {
        val modeMarkers = markerTypes.flatMap { markerCache[it].orEmpty() }
        updateState(
            markers = modeMarkers,
            mode = mode,
            selectedMarkerId = null,
            activeFilter = null,
            selectedMarkerOverride = null,
            selectedUserProfileOverride = null,
            selectedPoiInfoOverride = null,
            selectedEventInfoOverride = null,
            isLoading = false,
            errorMessage = errorMessage,
            isDetailLoading = false,
            detailErrorMessage = null
        )
    }

    private fun positionMarker(marker: MapMarker): MapMarker {
        val point = coordinateConverter.latLngToPoint(marker.latitude, marker.longitude)
        return marker.withMapPosition(point.x, point.y)
    }

    private fun markerTypesForMode(mode: MapMode): List<MarkerType> = when (mode) {
        MapMode.HOME -> listOf(MarkerType.USER, MarkerType.POI)
        MapMode.EVENT -> listOf(MarkerType.EVENT)
        MapMode.SHOP -> listOf(MarkerType.SHOP)
        MapMode.POSTER -> emptyList()
    }

    private fun filterContextForMode(mode: MapMode): MapFilterContext = when (mode) {
        MapMode.HOME -> MapFilterContext.HOME_MARKERS
        MapMode.EVENT -> MapFilterContext.EVENT_CATEGORIES
        MapMode.SHOP, MapMode.POSTER -> MapFilterContext.NONE
    }

    fun selectMarker(markerId: String) {
        val selectedMarker = _uiState.value.renderMarkers.firstOrNull { it.id == markerId }
            ?: run {
                return
            }

        detailLoadJob?.cancel()

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

        detailLoadJob = loadSelectedMarkerDetails(selectedMarker)
    }

    private fun loadSelectedMarkerDetails(marker: MapMarker): Job =
        viewModelScope.launch {
            when (marker) {
                is UserMarkerData -> repository.getUserProfile(marker.sourceId)
                    .onSuccess { profile ->
                        if (_uiState.value.selectedMarker?.id == marker.id) {
                            updateSelectedDetails(profile, null, null)
                        }
                    }
                    .onFailure { error ->
                        if (_uiState.value.selectedMarker?.id == marker.id) updateDetailError(error)
                    }

                is PoiMarkerData -> repository.getPoiInfo(marker.sourceId, marker.label)
                    .onSuccess { poi ->
                        if (_uiState.value.selectedMarker?.id == marker.id) {
                            updateSelectedDetails(null, poi, null)
                        }
                    }
                    .onFailure { error ->
                        if (_uiState.value.selectedMarker?.id == marker.id) updateDetailError(error)
                    }

                is EventMarkerData -> {
                    repository.getEventInfo(marker.sourceId)
                        .onSuccess { event ->
                            if (_uiState.value.selectedMarker?.id == marker.id) {
                                if (event.id == marker.sourceId) {
                                    updateSelectedDetails(null, null, event)
                                } else {
                                    updateDetailError(
                                        IllegalStateException(
                                            "Event preview ID mismatch: requested ${marker.sourceId}, received ${event.id}"
                                        )
                                    )
                                }
                            }
                        }
                        .onFailure { error ->
                            if (_uiState.value.selectedMarker?.id == marker.id) {
                                updateDetailError(error)
                            }
                        }
                }

                is ShopMarkerData -> repository.getShopInfo(marker.sourceId)
                    .onSuccess {
                        if (_uiState.value.selectedMarker?.id == marker.id) {
                            _uiState.value = _uiState.value.copy(
                                isDetailLoading = false,
                                detailErrorMessage = null,
                                selectedUserProfile = null,
                                selectedPoiInfo = null,
                                selectedEventInfo = null
                            )
                        }
                    }
                    .onFailure { error ->
                        if (_uiState.value.selectedMarker?.id == marker.id) updateDetailError(error)
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

    fun setFilter(type: MarkerType?) {
        val state = _uiState.value
        if (state.mode != MapMode.HOME) return
        if (type != null && type !in markerTypesForMode(MapMode.HOME)) return

        detailLoadJob?.cancel()
        updateState(
            markers = state.markers,
            mode = state.mode,
            selectedMarkerId = null,
            activeFilter = type,
            selectedMarkerOverride = null,
            selectedUserProfileOverride = null,
            selectedPoiInfoOverride = null,
            selectedEventInfoOverride = null,
            isDetailLoading = false,
            detailErrorMessage = null
        )
    }

    fun sendConnectionRequest(userId: Int) {
        viewModelScope.launch {
            repository.sendConnectionRequest(userId)
        }
    }

    fun enableEventReminder(eventId: Int) {
        viewModelScope.launch {
            repository.enableEventReminder(eventId)
        }
    }

    fun disableEventReminder(eventId: Int) {
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
        mode: MapMode = _uiState.value.mode,
        selectedMarkerId: String? = _uiState.value.selectedMarkerId,
        activeFilter: MarkerType? = _uiState.value.activeFilter,

        selectedMarkerOverride: MapMarker? = _uiState.value.selectedMarker,
        selectedUserProfileOverride: MapUserProfile? = _uiState.value.selectedUserProfile,
        selectedPoiInfoOverride: MapPoiInfo? = _uiState.value.selectedPoiInfo,
        selectedEventInfoOverride: MapEventInfo? = _uiState.value.selectedEventInfo,

        isLoading: Boolean = _uiState.value.isLoading,
        errorMessage: String? = _uiState.value.errorMessage,

        isDetailLoading: Boolean = _uiState.value.isDetailLoading,
        detailErrorMessage: String? = _uiState.value.detailErrorMessage
    ) {
        val modeMarkerTypes = markerTypesForMode(mode)
        val validFilter = activeFilter?.takeIf {
            mode == MapMode.HOME && it in modeMarkerTypes
        }
        val modeMarkers = markers.filter { it.type in modeMarkerTypes }
        val visibleMarkers = if (validFilter == null) modeMarkers else {
            modeMarkers.filter { it.type == validFilter }
        }

        val renderMarkers = visibleMarkers.map { marker ->
            marker.withSelection(marker.id == selectedMarkerId)
        }

        val selectedMarker = selectedMarkerOverride
            ?: renderMarkers.firstOrNull { marker ->
                marker.id == selectedMarkerId
            }

        _uiState.value = _uiState.value.copy(
            mode = mode,
            markers = markers,
            renderMarkers = renderMarkers,
            selectedMarkerId = selectedMarkerId,
            selectedMarker = selectedMarker,
            selectedUserProfile = selectedUserProfileOverride,
            selectedPoiInfo = selectedPoiInfoOverride,
            selectedEventInfo = selectedEventInfoOverride,
            activeFilter = validFilter,
            filterContext = filterContextForMode(mode),
            isLoading = isLoading,
            errorMessage = errorMessage,
            isDetailLoading = isDetailLoading,
            detailErrorMessage = detailErrorMessage
        )
    }
}
