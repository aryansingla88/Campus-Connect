package com.example.campusconnect.feature.map.mapengine

import androidx.compose.ui.geometry.Offset
import com.example.campusconnect.feature.map.mapengine.interaction.MapHitTester
import com.example.campusconnect.feature.map.mapengine.model.MapMarkerItem
import com.example.campusconnect.feature.map.mapengine.model.MarkerType
import com.example.campusconnect.feature.map.mapengine.viewport.MAP_IMAGE_HEIGHT
import com.example.campusconnect.feature.map.mapengine.viewport.MAP_IMAGE_WIDTH
import com.example.campusconnect.feature.map.mapengine.viewport.MapViewport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

private data class TestMarker(
    override val id: String,
    override val x: Float,
    override val y: Float,
    override val type: MarkerType = MarkerType.USER,
) : MapMarkerItem

class MapHitTesterTest {

    private val bounds = MapViewport.calculateImageBounds(1000f, 1000f)
    private val scale = 1.0f
    private val offset = Offset.Zero

    @Test
    fun testFindTappedMarker_hitsInsideRadius() {
        val marker = TestMarker(id = "m1", x = 1500f, y = 1500f)
        val markers = listOf(marker)

        // Marker screen coordinate is at center (500, 500)
        val markerScreenX = bounds.left + (marker.x / MAP_IMAGE_WIDTH) * bounds.width
        val markerScreenY = bounds.top + (marker.y / MAP_IMAGE_HEIGHT) * bounds.height

        // Tap directly on marker
        val hitResult = MapHitTester.findTappedMarker(
            tap = Offset(markerScreenX + 10f, markerScreenY + 10f),
            tappableMarkers = markers,
            bounds = bounds,
            scale = scale,
            offset = offset
        )

        assertNotNull(hitResult)
        assertEquals("m1", hitResult?.id)
    }

    @Test
    fun testFindTappedMarker_missesOutsideRadius() {
        val marker = TestMarker(id = "m1", x = 1500f, y = 1500f)
        val markers = listOf(marker)

        // Marker screen coordinate is at (500, 500)
        // Tap far away at (200, 200)
        val hitResult = MapHitTester.findTappedMarker(
            tap = Offset(200f, 200f),
            tappableMarkers = markers,
            bounds = bounds,
            scale = scale,
            offset = offset
        )

        assertNull(hitResult)
    }

    @Test
    fun testFindTappedMarker_prefersTopmostOverlappingMarker() {
        val bottom = TestMarker(id = "bottom", x = 1500f, y = 1500f)
        val top = TestMarker(id = "top", x = 1500f, y = 1500f)
        val markersInDrawOrder = listOf(bottom, top)
        val tap = Offset(
            bounds.left + (top.x / MAP_IMAGE_WIDTH) * bounds.width,
            bounds.top + (top.y / MAP_IMAGE_HEIGHT) * bounds.height
        )

        val hit = MapHitTester.findTappedMarker(
            tap = tap,
            tappableMarkers = markersInDrawOrder,
            bounds = bounds,
            scale = scale,
            offset = offset
        )

        assertEquals("top", hit?.id)
    }

    @Test
    fun testScreenToImageCoordinates_recoversImageCoords() {
        val tap = Offset(500f, 500f) // center of 1000x1000 square container

        val imageCoords = MapHitTester.screenToImageCoordinates(
            tap = tap,
            bounds = bounds,
            scale = scale,
            offset = offset
        )

        assertEquals(1500f, imageCoords.x, 1.0f)
        assertEquals(1500f, imageCoords.y, 1.0f)
    }
}
