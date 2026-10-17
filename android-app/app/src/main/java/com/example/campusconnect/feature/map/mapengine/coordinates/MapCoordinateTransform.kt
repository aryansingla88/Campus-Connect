package com.example.campusconnect.feature.map.mapengine.coordinates

import androidx.compose.ui.geometry.Offset
import com.example.campusconnect.feature.map.mapengine.viewport.ImageBounds
import com.example.campusconnect.feature.map.mapengine.viewport.MAP_IMAGE_HEIGHT
import com.example.campusconnect.feature.map.mapengine.viewport.MAP_IMAGE_WIDTH

/** Shared conversion between map-image coordinates and the visible screen. */
object MapCoordinateTransform {
    fun imageToScreen(
        x: Float,
        y: Float,
        bounds: ImageBounds,
        scale: Float,
        offset: Offset
    ): Offset {
        val imageX = bounds.left + (x / MAP_IMAGE_WIDTH) * bounds.width
        val imageY = bounds.top + (y / MAP_IMAGE_HEIGHT) * bounds.height
        return Offset(imageX * scale + offset.x, imageY * scale + offset.y)
    }

    fun screenToImage(
        point: Offset,
        bounds: ImageBounds,
        scale: Float,
        offset: Offset
    ): Offset {
        require(scale != 0f) { "Map scale must not be zero." }
        require(bounds.width != 0f && bounds.height != 0f) { "Map bounds must have non-zero dimensions." }
        val mapX = (point.x - offset.x) / scale
        val mapY = (point.y - offset.y) / scale
        return Offset(
            ((mapX - bounds.left) / bounds.width) * MAP_IMAGE_WIDTH,
            ((mapY - bounds.top) / bounds.height) * MAP_IMAGE_HEIGHT
        )
    }
}
