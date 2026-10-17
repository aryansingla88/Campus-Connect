package com.example.campusconnect.feature.map.mapengine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.example.campusconnect.feature.map.mapengine.viewport.DEFAULT_ZOOM
import com.example.campusconnect.feature.map.mapengine.viewport.MAX_ZOOM
import com.example.campusconnect.feature.map.mapengine.viewport.MIN_ZOOM
import com.example.campusconnect.feature.map.mapengine.viewport.MapViewportState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapViewportStateTest {

    @Test
    fun testInitialState() {
        val state = MapViewportState(initialZoom = 3.0f)
        assertEquals(3.0f, state.scale, 0.001f)
        assertEquals(Offset.Zero, state.offset)
    }

    @Test
    fun testFocusOnCommand() {
        val state = MapViewportState()
        state.containerSize = IntSize(1000, 1000)

        val targetPosition = Offset(1500f, 1500f)
        val targetZoom = 4.0f

        state.focusOn(targetPosition, targetZoom)

        assertEquals(targetZoom, state.scale, 0.001f)
        // With marker at center (1500, 1500) and square container, offset should center it
        assertTrue("Offset should be computed and non-zero", state.offset != Offset.Zero)
    }

    @Test
    fun testResetViewportCommand() {
        val state = MapViewportState(initialZoom = 4.5f)
        state.containerSize = IntSize(1000, 1000)

        // Reset viewport
        state.resetViewport()

        assertEquals(DEFAULT_ZOOM, state.scale, 0.001f)
    }

    @Test
    fun testOnTransformClampsZoom() {
        val state = MapViewportState(initialZoom = 2.5f)
        state.containerSize = IntSize(1000, 1000)

        // Try zooming beyond MAX_ZOOM
        state.onTransform(
            centroid = Offset(500f, 500f),
            pan = Offset.Zero,
            zoom = 10f
        )
        assertTrue(state.scale <= MAX_ZOOM)

        // Try zooming below MIN_ZOOM
        state.onTransform(
            centroid = Offset(500f, 500f),
            pan = Offset.Zero,
            zoom = 0.01f
        )
        assertTrue(state.scale >= MIN_ZOOM)
    }
}
