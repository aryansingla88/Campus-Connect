package com.example.campusconnect.feature.map.mapengine

import androidx.compose.ui.geometry.Offset
import com.example.campusconnect.feature.map.mapengine.viewport.MAP_IMAGE_HEIGHT
import com.example.campusconnect.feature.map.mapengine.viewport.MAP_IMAGE_WIDTH
import com.example.campusconnect.feature.map.mapengine.viewport.MapViewport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapViewportTest {

    @Test
    fun testCalculateImageBounds_portraitContainer() {
        val containerWidth = 1080f
        val containerHeight = 2400f

        val bounds = MapViewport.calculateImageBounds(containerWidth, containerHeight)

        // For portrait (canvasAspect < 1), image should be fit horizontally
        assertEquals(0f, bounds.left, 0.01f)
        assertEquals(containerWidth, bounds.width, 0.01f)
        assertEquals(containerWidth, bounds.height, 0.01f) // 1:1 aspect ratio
        assertEquals((containerHeight - containerWidth) / 2f, bounds.top, 0.01f)
    }

    @Test
    fun testCalculateImageBounds_landscapeContainer() {
        val containerWidth = 2400f
        val containerHeight = 1080f

        val bounds = MapViewport.calculateImageBounds(containerWidth, containerHeight)

        // For landscape (canvasAspect > 1), image should be fit vertically
        assertEquals(0f, bounds.top, 0.01f)
        assertEquals(containerHeight, bounds.height, 0.01f)
        assertEquals(containerHeight, bounds.width, 0.01f)
        assertEquals((containerWidth - containerHeight) / 2f, bounds.left, 0.01f)
    }

    @Test
    fun testCalculateCenterOffset() {
        val containerWidth = 1000f
        val containerHeight = 1000f
        val bounds = MapViewport.calculateImageBounds(containerWidth, containerHeight)
        val scale = 2.5f

        val centerOffset = MapViewport.calculateCenterOffset(scale, containerWidth, containerHeight, bounds)

        // Center offset should be symmetric in a square container
        assertEquals(centerOffset.x, centerOffset.y, 0.1f)
    }

    @Test
    fun testCalculateFocusOffset_focusesOnMarker() {
        val containerWidth = 1000f
        val containerHeight = 1000f
        val bounds = MapViewport.calculateImageBounds(containerWidth, containerHeight)
        val targetScale = 3f

        val markerX = 1500f // exactly center of MAP_IMAGE_WIDTH
        val markerY = 1500f // exactly center of MAP_IMAGE_HEIGHT

        val offset = MapViewport.calculateFocusOffset(
            markerX = markerX,
            markerY = markerY,
            targetScale = targetScale,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            bounds = bounds
        )

        val mapScreenX = (bounds.left + (markerX / MAP_IMAGE_WIDTH) * bounds.width) * targetScale + offset.x
        val mapScreenY = (bounds.top + (markerY / MAP_IMAGE_HEIGHT) * bounds.height) * targetScale + offset.y

        // The marker should be centered on the screen (500, 500)
        assertEquals(500f, mapScreenX, 1.0f)
        assertEquals(500f, mapScreenY, 1.0f)
    }
}
