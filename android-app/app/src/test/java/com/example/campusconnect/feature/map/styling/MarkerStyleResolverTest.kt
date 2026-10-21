package com.example.campusconnect.feature.map.styling

import androidx.compose.ui.graphics.colorspace.ColorSpaces
import com.example.campusconnect.feature.map.mapengine.model.MarkerType
import com.example.campusconnect.feature.map.mapengine.styling.MarkerStyleConfig
import com.example.campusconnect.feature.map.mapengine.styling.MarkerStyleResolver
import com.example.campusconnect.feature.map.model.EventMarkerData
import com.example.campusconnect.feature.map.model.LocalUserMarkerData
import com.example.campusconnect.feature.map.model.PoiMarkerData
import com.example.campusconnect.feature.map.model.ShopMarkerData
import com.example.campusconnect.feature.map.model.UserMarkerData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkerStyleResolverTest {

    private val resolver = MarkerStyleResolver

    @Test
    fun userStyleUsesGenderAndUserRadius() {
        val female = UserMarkerData(
            sourceId = 1, latitude = 29.94, longitude = 76.81,
            username = "Alice", userGender = "female"
        )

        val style = resolver.resolve(female, zoom = 1f, gender = female.userGender)

        assertEquals(MarkerStyleConfig.USER_FEMALE, style.color)
        assertEquals(15f, style.radius, 0.01f)
    }

    @Test
    fun eventStyleUsesPriorityColorAndRadius() {
        val highPriority = EventMarkerData(
            sourceId = 10, latitude = 29.94, longitude = 76.81,
            title = "Hackathon", priority = 3
        )
        val lowPriority = EventMarkerData(
            sourceId = 11, latitude = 29.94, longitude = 76.81,
            title = "Club Meet", priority = 1
        )

        val highStyle = resolver.resolve(highPriority, zoom = 1f, priority = highPriority.priority)
        val lowStyle = resolver.resolve(lowPriority, zoom = 1f, priority = lowPriority.priority)

        assertEquals(MarkerStyleConfig.EVENT_HIGH_PRIORITY, highStyle.color)
        assertEquals(MarkerStyleConfig.EVENT_LOW_PRIORITY, lowStyle.color)
        assertEquals(16f, highStyle.radius, 0.01f)
        assertEquals(14f, lowStyle.radius, 0.01f)
    }

    @Test
    fun poiStylePreservesHighlightAndZoomRules() {
        val highlighted = PoiMarkerData(
            sourceId = 20, latitude = 29.94, longitude = 76.81,
            name = "Library", category = "ACADEMIC", iconType = null,
            visibility = null, priority = 4
        )
        val lowPriority = PoiMarkerData(
            sourceId = 21, latitude = 29.94, longitude = 76.81,
            name = "Bench", category = "OUTDOOR", iconType = null,
            visibility = null, priority = 2
        )

        assertEquals(MarkerStyleConfig.POI_DOT_HIGHLIGHTED, resolver.resolve(highlighted, 1f, priority = highlighted.priority, isHighlighted = highlighted.isHighlighted).color)
        assertEquals(MarkerStyleConfig.POI_DOT_NORMAL, resolver.resolve(lowPriority, 1f, priority = lowPriority.priority, isHighlighted = lowPriority.isHighlighted).color)
        assertEquals(0f, MarkerStyleConfig.getMinZoomForMarker(MarkerType.POI, highlighted.priority), 0.01f)
        assertEquals(MarkerStyleConfig.POI_ZOOM_HIGH_THRESHOLD, MarkerStyleConfig.getMinZoomForMarker(MarkerType.POI, lowPriority.priority), 0.01f)
    }

    @Test
    fun selectionPreservesTypeAndRaisesEventStyle() {
        val event = EventMarkerData(
            sourceId = 30, latitude = 29.94, longitude = 76.81,
            title = "Event", priority = 1
        )

        val selected = event.withSelection(true)

        assertTrue(selected.isSelected)
        assertTrue(
            MarkerStyleConfig.getDrawOrder(selected.type, event.priority, selected.isSelected) >
                MarkerStyleConfig.getDrawOrder(event.type, event.priority, event.isSelected)
        )
        assertEquals(1.08f, resolver.resolve(selected, 1f).selectionScale, 0.001f)

        val shop = ShopMarkerData(
            sourceId = 31, latitude = 29.94, longitude = 76.81, name = "Canteen"
        )
        assertFalse(shop.isSelected)
        assertTrue(shop.withSelection(true).isSelected)
    }

    @Test
    fun markerColorsUseValidSrgbValues() {
        val markers = listOf(
            UserMarkerData(sourceId = 1, latitude = 0.0, longitude = 0.0, username = "M", userGender = "male"),
            UserMarkerData(sourceId = 2, latitude = 0.0, longitude = 0.0, username = "F", userGender = "female"),
            PoiMarkerData(sourceId = 3, latitude = 0.0, longitude = 0.0, name = "P", category = null, iconType = null, visibility = null, priority = 4),
            EventMarkerData(sourceId = 4, latitude = 0.0, longitude = 0.0, title = "E", priority = 3),
            ShopMarkerData(sourceId = 5, latitude = 0.0, longitude = 0.0, name = "S"),
            LocalUserMarkerData(latitude = 0.0, longitude = 0.0, isStale = false, mapX = 10f, mapY = 10f)
        )

        for (marker in markers) {
            val color = when (marker) {
                is UserMarkerData -> resolver.resolve(marker, 1f, gender = marker.userGender).color
                is PoiMarkerData -> resolver.resolve(marker, 1f, priority = marker.priority, isHighlighted = marker.isHighlighted).color
                is EventMarkerData -> resolver.resolve(marker, 1f, priority = marker.priority).color
                is ShopMarkerData -> resolver.resolve(marker, 1f).color
                is LocalUserMarkerData -> resolver.resolve(marker, 1f).color
            }
            assertNotNull(color.colorSpace)
            assertEquals(ColorSpaces.Srgb, color.colorSpace)
        }
    }
}
