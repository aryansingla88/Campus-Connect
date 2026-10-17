package com.example.campusconnect.feature.map.model

import com.example.campusconnect.feature.map.mapengine.model.MarkerType
import org.junit.Assert.assertEquals
import org.junit.Test

class MapFilterSelectionTest {
    @Test
    fun activeHomeFilterProducesHighlightedDrawerKey() {
        assertEquals(
            "Users:users",
            selectedMapFilterKey(MapUiState(activeFilter = MarkerType.USER))
        )
        assertEquals(
            "Places:LIBRARY",
            selectedMapFilterKey(MapUiState(activeFilter = MarkerType.POI, activePoiCategory = "LIBRARY"))
        )
        assertEquals(
            "Places:$ALL_POIS_FILTER_ID",
            selectedMapFilterKey(MapUiState(activeFilter = MarkerType.POI))
        )
    }

    @Test
    fun tappingSelectedHomeFilterClearsIt() {
        val users = MapUiState(activeFilter = MarkerType.USER)
        val poi = MapUiState(activeFilter = MarkerType.POI, activePoiCategory = "LIBRARY")

        assertEquals(MapFilterAction.ClearHomeFilters, resolveMapFilterTap(users, "Users", "users"))
        assertEquals(MapFilterAction.ClearHomeFilters, resolveMapFilterTap(poi, "Places", "LIBRARY"))
    }

    @Test
    fun tappingSelectedEventCategoryResetsToAllEvents() {
        val state = MapUiState(
            mode = MapMode.EVENT,
            filterContext = MapFilterContext.EVENT_CATEGORIES,
            activeEventCategoryId = 12
        )

        assertEquals(MapFilterAction.ShowAllEvents, resolveMapFilterTap(state, "Events", "12"))
        assertEquals(
            MapFilterAction.SelectEventCategory(4),
            resolveMapFilterTap(state, "Events", "4")
        )
    }

    @Test
    fun malformedEventCategoryIsIgnored() {
        val state = MapUiState(filterContext = MapFilterContext.EVENT_CATEGORIES)
        assertEquals(MapFilterAction.Ignore, resolveMapFilterTap(state, "Events", "not-an-id"))
    }
}
