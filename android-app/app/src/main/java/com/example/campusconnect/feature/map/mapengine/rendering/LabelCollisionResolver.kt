package com.example.campusconnect.feature.map.mapengine.rendering

import androidx.compose.ui.geometry.Offset
import com.example.campusconnect.feature.map.mapengine.model.MapMarkerItem
import com.example.campusconnect.feature.map.mapengine.viewport.ImageBounds
import com.example.campusconnect.feature.map.mapengine.viewport.MAP_IMAGE_HEIGHT
import com.example.campusconnect.feature.map.mapengine.viewport.MAP_IMAGE_WIDTH

/**
 * Axis-aligned bounding box for label collision testing.
 */
data class LabelRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    fun intersects(other: LabelRect): Boolean =
        left < other.right && right > other.left &&
                top < other.bottom && bottom > other.top
}

/**
 * Reusable engine mechanics for resolving label collisions between markers
 * based on priority, selection, and bounding box overlap.
 */
object LabelCollisionResolver {

    fun <T : MapMarkerItem> resolveLabelVisibility(
        markers: List<T>,
        bounds: ImageBounds,
        scale: Float,
        offset: Offset,
        measureLabel: (marker: T, screenPos: Offset) -> LabelRect?,
        isForcedVisible: (marker: T) -> Boolean = { false },
        prioritySelector: (marker: T) -> Int = { 0 }
    ): Map<String, Boolean> {
        if (markers.isEmpty()) return emptyMap()

        val ordered = markers.sortedWith(
            compareByDescending<T> { prioritySelector(it) }
                .thenByDescending { isForcedVisible(it) }
        )

        val placedBoxes = mutableListOf<LabelRect>()
        val visibility = mutableMapOf<String, Boolean>()

        for (marker in ordered) {
            val mapX = bounds.left + (marker.x / MAP_IMAGE_WIDTH) * bounds.width
            val mapY = bounds.top + (marker.y / MAP_IMAGE_HEIGHT) * bounds.height
            val screenPos = Offset(mapX * scale + offset.x, mapY * scale + offset.y)

            val box = measureLabel(marker, screenPos)
            if (box == null) {
                visibility[marker.id] = false
                continue
            }

            val collides = placedBoxes.any { it.intersects(box) }
            if (!collides) {
                placedBoxes.add(box)
                visibility[marker.id] = true
            } else {
                visibility[marker.id] = isForcedVisible(marker)
            }
        }

        return visibility
    }
}
