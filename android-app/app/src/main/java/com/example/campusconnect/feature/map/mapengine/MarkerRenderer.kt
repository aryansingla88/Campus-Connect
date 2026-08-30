package com.example.campusconnect.feature.map.mapengine

class MarkerRenderer {

    fun buildMarkerRenderData(
        markers: List<MapMarker>,
        selectedMarkerId: Int? = null
    ): List<MarkerRenderData> {

        return markers.map { marker ->

            val isSelected = marker.id == selectedMarkerId

            MarkerRenderData(
                id = marker.id,
                x = marker.x,
                y = marker.y,
                radius = getRadius(marker, isSelected),
                color = getColor(marker),
                label = marker.label,
                type = marker.type,
                gender = marker.gender,
                priority = marker.priority,
                size = marker.size,
                isHighlighted = marker.isHighlighted,
                isSelected = isSelected
            )
        }
    }

    private fun getRadius(
        marker: MapMarker,
        isSelected: Boolean
    ): Float {

        val baseRadius = when (marker.size) {
            MarkerSize.SMALL -> 9f
            MarkerSize.MEDIUM -> 13f
            MarkerSize.LARGE -> 15f
        }

        val typeRadius = when (marker.type) {
            MarkerType.POI -> {
                if (marker.isHighlighted) baseRadius + 4f else baseRadius - 3f
            }

            MarkerType.USER -> baseRadius + 2f

            MarkerType.SHOP -> baseRadius + 1f

            MarkerType.EVENT -> baseRadius + marker.priority.coerceIn(0, 3)
        }

        return if (isSelected) typeRadius + 4f else typeRadius
    }

    private fun getColor(marker: MapMarker): Long {

        return when (marker.type) {

            MarkerType.POI -> {
                if (marker.isHighlighted) 0xFF00C853 else 0xFF66BB6A
            }

            MarkerType.USER -> {
                if (marker.gender == "female") 0xFFE91E63 else 0xFF2196F3
            }

            MarkerType.EVENT -> 0xFFFF6F00

            MarkerType.SHOP -> 0xFFD84315
        }
    }
}