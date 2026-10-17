package com.example.campusconnect.feature.map.styling

import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import com.example.campusconnect.feature.map.mapengine.model.MarkerType
import com.example.campusconnect.feature.map.mapengine.rendering.LabelCollisionResolver
import com.example.campusconnect.feature.map.mapengine.rendering.LabelRect
import com.example.campusconnect.feature.map.mapengine.styling.MarkerStyleConfig
import com.example.campusconnect.feature.map.mapengine.styling.MarkerStyleResolver
import com.example.campusconnect.feature.map.mapengine.viewport.ImageBounds
import com.example.campusconnect.feature.map.model.PoiMarkerData

/**
 * Resolves POI label visibility and avoids overlapping labels.
 */
object PoiLabelResolver {

    fun isPoiVisibleAtZoom(priority: Int, zoom: Float): Boolean {
        val minZoom = MarkerStyleConfig.getMinZoomForMarker(
            MarkerType.POI,
            priority
        )
        return zoom >= minZoom
    }

    fun resolvePoiLabelVisibility(
        poiMarkers: List<PoiMarkerData>,
        bounds: ImageBounds,
        scale: Float,
        offset: Offset
    ): Map<String, Boolean> {
        val measurePaint = Paint()

        return LabelCollisionResolver.resolveLabelVisibility(
            markers = poiMarkers,
            bounds = bounds,
            scale = scale,
            offset = offset,
            measureLabel = { marker, screenPos ->
                val style = MarkerStyleResolver.resolve(
                    marker, scale, priority = marker.priority, isHighlighted = marker.isHighlighted
                )
                val radius = style.radius
                val labelTextSize = style.labelTextSize

                measurePaint.textSize = labelTextSize
                measurePaint.isFakeBoldText = marker.isHighlighted

                val labelWidth = measurePaint.measureText(marker.label) + 22f
                val labelHeight = if (marker.isHighlighted) 30f else 26f

                val labelLeft = screenPos.x + radius + 8f
                val labelTop = screenPos.y - labelHeight / 2f

                LabelRect(
                    left = labelLeft,
                    top = labelTop,
                    right = labelLeft + labelWidth,
                    bottom = labelTop + labelHeight
                )
            },
            isForcedVisible = { it.isSelected },
            prioritySelector = { it.priority }
        )
    }
}
