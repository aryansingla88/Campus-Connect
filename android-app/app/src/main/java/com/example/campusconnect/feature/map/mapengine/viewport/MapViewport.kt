package com.example.campusconnect.feature.map.mapengine.viewport

import androidx.compose.ui.geometry.Offset

const val MAP_IMAGE_WIDTH = 3000f
const val MAP_IMAGE_HEIGHT = 3000f
const val MIN_ZOOM = 2.1f
const val MAX_ZOOM = 5f
const val DEFAULT_ZOOM = 2.2f

data class ImageBounds(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
)

object MapViewport {

    fun calculateImageBounds(
        width: Float,
        height: Float
    ): ImageBounds {
        val imageAspect = MAP_IMAGE_WIDTH / MAP_IMAGE_HEIGHT
        val canvasAspect = width / height

        return if (canvasAspect > imageAspect) {
            val imageHeight = height
            val imageWidth = imageHeight * imageAspect
            ImageBounds(
                left = (width - imageWidth) / 2f,
                top = 0f,
                width = imageWidth,
                height = imageHeight
            )
        } else {
            val imageWidth = width
            val imageHeight = imageWidth / imageAspect
            ImageBounds(
                left = 0f,
                top = (height - imageHeight) / 2f,
                width = imageWidth,
                height = imageHeight
            )
        }
    }

    fun clampOffsetSmooth(
        offset: Offset,
        scale: Float,
        containerWidth: Float,
        containerHeight: Float,
        bounds: ImageBounds
    ): Offset {
        val mapWidth = bounds.width * scale
        val mapHeight = bounds.height * scale

        val mapLeft = bounds.left * scale
        val mapTop = bounds.top * scale

        val minX = containerWidth - mapLeft - mapWidth
        val maxX = -mapLeft

        val minY = containerHeight - mapTop - mapHeight
        val maxY = -mapTop

        val finalX = if (mapWidth <= containerWidth) {
            (containerWidth - mapWidth) / 2f - mapLeft
        } else {
            offset.x.coerceIn(minX, maxX)
        }

        val finalY = if (mapHeight <= containerHeight) {
            (containerHeight - mapHeight) / 2f - mapTop
        } else {
            offset.y.coerceIn(minY, maxY)
        }

        return Offset(finalX, finalY)
    }

    fun calculateFocusOffset(
        markerX: Float,
        markerY: Float,
        targetScale: Float,
        containerWidth: Float,
        containerHeight: Float,
        bounds: ImageBounds
    ): Offset {
        val mapX = bounds.left + (markerX / MAP_IMAGE_WIDTH) * bounds.width
        val mapY = bounds.top + (markerY / MAP_IMAGE_HEIGHT) * bounds.height

        val rawOffset = Offset(
            x = containerWidth / 2f - mapX * targetScale,
            y = containerHeight / 2f - mapY * targetScale
        )

        return clampOffsetSmooth(
            offset = rawOffset,
            scale = targetScale,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            bounds = bounds
        )
    }

    fun calculateCenterOffset(
        scale: Float,
        containerWidth: Float,
        containerHeight: Float,
        bounds: ImageBounds
    ): Offset {
        return calculateFocusOffset(
            markerX = MAP_IMAGE_WIDTH / 2f,
            markerY = MAP_IMAGE_HEIGHT / 2f,
            targetScale = scale,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            bounds = bounds
        )
    }
}
