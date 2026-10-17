package com.example.campusconnect.feature.map.model

import com.example.campusconnect.feature.map.mapengine.model.MarkerType

data class MapFilterOption(
    val id: String,
    val name: String
)

internal const val ALL_POIS_FILTER_ID = "__all_pois__"

internal sealed interface MapFilterAction {
    /** Compatibility for callers using the former shared Home-filter model. */
    data object ClearHomeFilters : MapFilterAction
    data object ClearUserFilter : MapFilterAction
    data class SelectUserFilter(val filterId: String) : MapFilterAction
    data object ClearPoiFilter : MapFilterAction
    data object SelectAllPois : MapFilterAction
    data class SelectPoiCategory(val category: String) : MapFilterAction
    data object ShowAllEvents : MapFilterAction
    data class SelectEventCategory(val categoryId: Int) : MapFilterAction
    data object Ignore : MapFilterAction
}

@Suppress("DEPRECATION")
internal fun selectedMapFilterKeys(state: MapUiState): Set<String> = when (state.filterContext) {
    MapFilterContext.HOME_MARKERS -> buildSet {
        (state.activeUserFilterId
            ?: state.activeFilter?.takeIf { it == MarkerType.USER }?.let { "users" })
            ?.let { add("Users:$it") }
        when {
            (state.isPoiFilterActive || state.activeFilter == MarkerType.POI) &&
                    state.activePoiCategory == null ->
                add("Places:$ALL_POIS_FILTER_ID")
            state.activePoiCategory != null -> add("Places:${state.activePoiCategory}")
        }
    }
    MapFilterContext.EVENT_CATEGORIES -> state.activeEventCategoryId
        ?.let { setOf("Events:$it") }
        .orEmpty()
    MapFilterContext.NONE -> emptySet()
}

@Suppress("DEPRECATION")
internal fun selectedMapFilterKey(state: MapUiState): String? =
    selectedMapFilterKeys(state).firstOrNull()

@Suppress("DEPRECATION")
internal fun resolveMapFilterTap(
    state: MapUiState,
    section: String,
    optionId: String
): MapFilterAction {
    val selected = "$section:$optionId" in selectedMapFilterKeys(state)
    return when (section) {
        "Users" -> if (selected) {
            if (state.activeFilter == MarkerType.USER) {
                MapFilterAction.ClearHomeFilters
            } else MapFilterAction.ClearUserFilter
        } else {
            MapFilterAction.SelectUserFilter(optionId)
        }
        "Places" -> when {
            optionId == ALL_POIS_FILTER_ID && selected -> if (state.activeFilter == MarkerType.POI) {
                MapFilterAction.ClearHomeFilters
            } else MapFilterAction.ClearPoiFilter
            optionId == ALL_POIS_FILTER_ID -> MapFilterAction.SelectAllPois
            selected -> if (state.activeFilter == MarkerType.POI) {
                MapFilterAction.ClearHomeFilters
            } else MapFilterAction.ClearPoiFilter
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
