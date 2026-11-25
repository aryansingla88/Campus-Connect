package com.example.campusconnect.feature.map.model

import androidx.compose.ui.geometry.Offset
import com.example.campusconnect.feature.map.mapengine.coordinates.MapCoordinateTransform
import com.example.campusconnect.feature.map.mapengine.model.MapMarkerItem
import com.example.campusconnect.feature.map.mapengine.model.MarkerType
import com.example.campusconnect.feature.map.mapengine.styling.MarkerStyleConfig
import com.example.campusconnect.feature.map.mapengine.viewport.ImageBounds
import kotlin.math.floor

data class MarkerClusterData(
    override val id: String,
    override val x: Float,
    override val y: Float,
    val markers: List<MapMarker>
) : MapMarkerItem {
    val representative: MapMarker = markers.first()
    val priority: Int = markers.maxOf { marker ->
        when (marker) {
            is PoiMarkerData -> marker.priority
            is EventMarkerData -> marker.priority
            else -> 0
        }
    }
    val isHighlighted: Boolean = markers.any { it is PoiMarkerData && it.isHighlighted }
    val countLabel: String = "${markers.size.coerceAtMost(5)}+"

    override val type: MarkerType = representative.type
    override val label: String = if (type == MarkerType.EVENT) "" else representative.label
    override val subLabel: String = ""
}

/** Clusters only same-type POI or event markers that nearly overlap on screen. */
fun clusterMapMarkers(
    markers: List<MapMarker>,
    bounds: ImageBounds,
    scale: Float,
    overlapRadiusPx: Float
): List<MapMarkerItem> {
    val eligible = markers.filter { marker ->
        (marker.type == MarkerType.POI || marker.type == MarkerType.EVENT) &&
                scale >= MarkerStyleConfig.getMinZoomForMarker(marker.type, markerPriority(marker))
    }
    if (eligible.size < 2) return markers

    val screenPositions = eligible.map { marker ->
        MapCoordinateTransform.imageToScreen(
            x = marker.x,
            y = marker.y,
            bounds = bounds,
            scale = scale,
            offset = Offset.Zero
        )
    }
    val radiusSquared = overlapRadiusPx * overlapRadiusPx
    val cells = mutableMapOf<Pair<Int, Int>, MutableList<Int>>()
    eligible.indices.forEach { index ->
        val position = screenPositions[index]
        val cell = floor(position.x / overlapRadiusPx).toInt() to
                floor(position.y / overlapRadiusPx).toInt()
        cells.getOrPut(cell) { mutableListOf() }.add(index)
    }
    val consumed = BooleanArray(eligible.size)
    val clusters = mutableListOf<MarkerClusterData>()

    eligible.indices.forEach { index ->
        if (consumed[index]) return@forEach

        val position = screenPositions[index]
        val cellX = floor(position.x / overlapRadiusPx).toInt()
        val cellY = floor(position.y / overlapRadiusPx).toInt()
        val nearby = mutableListOf(index)

        for (dx in -1..1) for (dy in -1..1) {
            cells[cellX + dx to cellY + dy].orEmpty().forEach candidateLoop@{ candidate ->
                if (candidate == index || consumed[candidate] || eligible[candidate].type != eligible[index].type) {
                    return@candidateLoop
                }
                val other = screenPositions[candidate]
                val deltaX = position.x - other.x
                val deltaY = position.y - other.y
                if (deltaX * deltaX + deltaY * deltaY <= radiusSquared) nearby.add(candidate)
            }
        }

        if (nearby.size > 1) {
            nearby.forEach { consumed[it] = true }
            val members = nearby.map(eligible::get)
            val clusterKey = members.map(MapMarker::id).sorted().joinToString("|")
            clusters.add(
                MarkerClusterData(
                    id = "MARKER_CLUSTER_${clusterKey.hashCode()}",
                    x = members.map(MapMarker::x).average().toFloat(),
                    y = members.map(MapMarker::y).average().toFloat(),
                    markers = members
                )
            )
        }
    }

    if (clusters.isEmpty()) return markers
    val clusteredIds = clusters.flatMapTo(mutableSetOf()) { cluster -> cluster.markers.map(MapMarker::id) }
    return markers
        .filterNot { it.id in clusteredIds }
        .map { it as MapMarkerItem } + clusters
}

private fun markerPriority(marker: MapMarker): Int = when (marker) {
    is PoiMarkerData -> marker.priority
    is EventMarkerData -> marker.priority
    else -> 0
}
