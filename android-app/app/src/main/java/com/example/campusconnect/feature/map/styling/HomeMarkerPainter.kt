package com.example.campusconnect.feature.map.styling

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.campusconnect.feature.map.mapengine.rendering.MarkerCanvasPainter
import com.example.campusconnect.feature.map.mapengine.rendering.StandardMarkerPainter
import com.example.campusconnect.feature.map.model.*
import com.example.campusconnect.feature.map.mapengine.model.MapMarkerItem
import com.example.campusconnect.feature.map.mapengine.model.MarkerType

/** Dispatches Home map markers and compact clusters to their canvas visuals. */
object HomeMarkerPainter : MarkerCanvasPainter<MapMarkerItem> {
    override fun DrawScope.drawMarker(
        marker: MapMarkerItem,
        screenX: Float,
        screenY: Float,
        zoom: Float,
        showLabel: Boolean
    ) {
        with(StandardMarkerPainter) {
            when (marker) {
                is MarkerClusterData -> drawMarkerCluster(marker, screenX, screenY, zoom)
                is UserMarkerData -> drawUserVisual(marker, screenX, screenY, zoom, gender = marker.userGender)
                is LocalUserMarkerData -> drawLocalUserMarker(marker, screenX, screenY)
                is PoiMarkerData -> drawPoiVisual(marker, screenX, screenY, zoom, showLabel, marker.priority, marker.isHighlighted)
                is EventMarkerData -> drawEventVisual(marker, screenX, screenY, zoom, marker.priority)
                is ShopMarkerData -> drawShopVisual(marker, screenX, screenY, zoom)
                else -> Unit
            }
        }
    }

    private fun DrawScope.drawMarkerCluster(
        marker: MarkerClusterData,
        x: Float,
        y: Float,
        zoom: Float
    ) {
        with(StandardMarkerPainter) {
            when (marker.type) {
                MarkerType.POI -> {
                    val radius = 10.dp.toPx()
                    val fill = if (marker.isHighlighted) {
                        Color(0xFF00C853)
                    } else {
                        Color(0xFF66BB6A)
                    }
                    drawCircle(Color.White, radius = radius + 2.dp.toPx(), center = Offset(x, y))
                    drawCircle(fill, radius = radius, center = Offset(x, y))
                }
                MarkerType.EVENT -> scale(
                    scaleX = 1.25f,
                    scaleY = 1.25f,
                    pivot = Offset(x, y)
                ) {
                    drawEventVisual(marker, x, y, zoom, marker.priority, showLabel = false)
                }
                else -> Unit
            }
        }

        val badgeRadius = 8.dp.toPx()
        val badgeCenter = Offset(x + 6.dp.toPx(), y - 6.dp.toPx())
        val badgeColor = if (marker.type == MarkerType.POI) {
            Color(0xFF2E7D32)
        } else {
            Color(0xFFB3261E)
        }
        drawCircle(Color.White, radius = badgeRadius + 1.5.dp.toPx(), center = badgeCenter)
        drawCircle(badgeColor, radius = badgeRadius, center = badgeCenter)

        val countTextSize = 6.dp.toPx()
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = countTextSize
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            )
        }
        drawContext.canvas.nativeCanvas.drawText(
            marker.countLabel,
            badgeCenter.x,
            badgeCenter.y - (paint.ascent() + paint.descent()) / 2f,
            paint
        )
    }

    private fun DrawScope.drawLocalUserMarker(marker: LocalUserMarkerData, x: Float, y: Float) {
        val alpha = if (marker.isStale) 0.55f else 1f
        drawCircle(Color(0x334285F4).copy(alpha = alpha), radius = 27f, center = Offset(x, y))
        drawCircle(Color.White.copy(alpha = alpha), radius = 13f, center = Offset(x, y))
        drawCircle(
            color = Color(0xFF1A73E8).copy(alpha = alpha),
            radius = 10f,
            center = Offset(x, y),
            style = Stroke(width = 4f)
        )
    }
}
