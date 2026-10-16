package com.example.campusconnect.feature.map.model

import com.example.campusconnect.feature.map.mapengine.model.MarkerType

data class MapUiState(
    val mode: MapMode = MapMode.HOME,
    val markers: List<MapMarker> = emptyList(),
    val renderMarkers: List<MapMarker> = emptyList(),

    val selectedMarkerId: String? = null,
    val selectedMarker: MapMarker? = null,

    val selectedUserProfile: MapUserProfile? = null,
    val selectedPoiInfo: MapPoiInfo? = null,
    val selectedEventInfo: MapEventInfo? = null,

    val activeFilter: MarkerType? = null,
    val filterContext: MapFilterContext = MapFilterContext.HOME_MARKERS,
    val eventCategories: List<EventFilter> = emptyList(),

    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val isDetailLoading: Boolean = false,
    val detailErrorMessage: String? = null
)

/** Context supplied to the shared filter UI; category data is intentionally empty for now. */
enum class MapFilterContext {
    HOME_MARKERS,
    EVENT_CATEGORIES,
    NONE
}

/** The selected map mode is state owned by MapViewModel, not by the composable. */
enum class MapMode {
    HOME,
    EVENT,
    SHOP,
    POSTER
}
