package com.example.campusconnect.feature.map.model

import com.example.campusconnect.feature.map.mapengine.model.MarkerType

data class MapFilterOption(
    val id: String,
    val name: String
)

internal const val ALL_POIS_FILTER_ID = "__all_pois__"

internal sealed interface MapFilterAction {
    data object ClearHomeFilters : MapFilterAction
    data object SelectUsers : MapFilterAction
    data object SelectAllPois : MapFilterAction
    data class SelectPoiCategory(val category: String) : MapFilterAction
    data object ShowAllEvents : MapFilterAction
    data class SelectEventCategory(val categoryId: Int) : MapFilterAction
    data object Ignore : MapFilterAction
}

internal fun selectedMapFilterKey(state: MapUiState): String? = when (state.filterContext) {
    MapFilterContext.HOME_MARKERS -> when {
        state.activeFilter == MarkerType.USER -> "Users:users"
        state.activeFilter == MarkerType.POI && state.activePoiCategory == null ->
            "Places:$ALL_POIS_FILTER_ID"
        state.activePoiCategory != null -> "Places:${state.activePoiCategory}"
        else -> null
    }
    MapFilterContext.EVENT_CATEGORIES -> state.activeEventCategoryId?.let { "Events:$it" }
    MapFilterContext.NONE -> null
}

internal fun resolveMapFilterTap(
    state: MapUiState,
    section: String,
    optionId: String
): MapFilterAction {
    val selected = selectedMapFilterKey(state) == "$section:$optionId"
    return when (section) {
        "Users" -> if (selected) MapFilterAction.ClearHomeFilters else MapFilterAction.SelectUsers
        "Places" -> when {
            optionId == ALL_POIS_FILTER_ID && selected -> MapFilterAction.ClearHomeFilters
            optionId == ALL_POIS_FILTER_ID -> MapFilterAction.SelectAllPois
            selected -> MapFilterAction.ClearHomeFilters
            else -> MapFilterAction.SelectPoiCategory(optionId)
        }
        "Events" -> if (selected) {
            MapFilterAction.ShowAllEvents
        } else {
            optionId.toIntOrNull()?.let(MapFilterAction::SelectEventCategory)
                ?: MapFilterAction.Ignore
        }
        else -> MapFilterAction.Ignore
    }
}
