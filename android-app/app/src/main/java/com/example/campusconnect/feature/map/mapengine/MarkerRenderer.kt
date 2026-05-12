package com.example.campusconnect.feature.map.mapengine

class MarkerRenderer {

    fun buildMarkerRenderData(
        markers: List<MapMarker>,
        selectedMarkerId: String? = null
    ): List<MarkerRenderData> {

        return markers.map { marker ->

            val isSelected = marker.id == selectedMarkerId

            MarkerRenderData(
                x = marker.x,
                y = marker.y,
                radius = getRadius(marker, isSelected),
                color = getColor(marker),
                label = marker.label,
                isSelected = isSelected
            )
        }
    }

    private fun getRadius(
        marker: MapMarker,
        isSelected: Boolean
    ): Float {

        if (isSelected) {
            return 18f
        }

        return when (marker.type) {

            MarkerType.USER -> 12f

            MarkerType.EVENT -> 14f

            MarkerType.POI -> 10f

            MarkerType.SHOP -> 11f
        }
    }

    private fun getColor(marker: MapMarker): Long {

        return when (marker.type) {

            MarkerType.USER -> 0xFF42A5F5

            MarkerType.EVENT -> 0xFFEF5350

            MarkerType.POI -> 0xFF66BB6A

            MarkerType.SHOP -> 0xFFFFCA28
        }
    }
}