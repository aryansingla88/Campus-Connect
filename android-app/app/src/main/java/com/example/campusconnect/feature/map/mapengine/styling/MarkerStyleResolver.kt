package com.example.campusconnect.feature.map.mapengine.styling

import com.example.campusconnect.feature.map.mapengine.model.MapMarkerItem
import com.example.campusconnect.feature.map.mapengine.model.MarkerType

/** Centralizes marker appearance decisions; renderers only draw the resolved style. */
object MarkerStyleResolver {
    fun resolve(
        marker: MapMarkerItem,
        zoom: Float,
        priority: Int = 0,
        gender: String? = null,
        isHighlighted: Boolean = false
    ): ResolvedMarkerStyle {
        val zoomBoost = (zoom - 1f).coerceIn(0f, 2f) * 2.2f
        val baseRadius = MarkerStyleConfig.calculateRadius(
            type = marker.type,
            priority = priority,
            isHighlighted = isHighlighted,
            isSelected = marker.isSelected
        )
        val radius = (baseRadius + zoomBoost).coerceIn(8f, 30f)
        val selectionScale = when {
            !marker.isSelected -> 1f
            marker.type == MarkerType.USER -> 1.18f
            marker.type == MarkerType.EVENT -> 1.08f
            else -> 1f
        }
        return ResolvedMarkerStyle(
            color = MarkerStyleConfig.getMarkerColor(
                type = marker.type,
                gender = gender,
                isHighlighted = isHighlighted,
                priority = priority
            ),
            radius = radius,
            labelTextSize = if (isHighlighted) 27f else 23f,
            selectionScale = selectionScale
        )
    }
}
