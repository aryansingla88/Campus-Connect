package com.example.campusconnect.feature.map.mapengine.rendering

import android.graphics.Paint
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.map.mapengine.model.MapMarkerItem
import com.example.campusconnect.feature.map.mapengine.styling.MarkerStyleConfig
import com.example.campusconnect.feature.map.mapengine.styling.MarkerStyleResolver
import com.example.campusconnect.feature.map.mapengine.model.MarkerType

/**
 * Shared painter for standard Campus Connect marker types.
 * Implements MarkerCanvasPainter to render domain markers onto Canvas DrawScope.
 * Uses the shared MapMarkerItem contract and MapEngine styling configuration,
 * so features can reuse the same standard visuals without depending on each other.
 */
object StandardMarkerPainter : MarkerCanvasPainter<MapMarkerItem> {

    override fun DrawScope.drawMarker(
        marker: MapMarkerItem,
        screenX: Float,
        screenY: Float,
        zoom: Float,
        showLabel: Boolean
    ) {
        when (marker.type) {
            MarkerType.POI -> drawPoiVisual(marker, screenX, screenY, zoom, showLabel)
            MarkerType.USER -> drawUserVisual(marker, screenX, screenY, zoom)
            MarkerType.EVENT -> drawEventVisual(marker, screenX, screenY, zoom)
            MarkerType.SHOP -> drawShopVisual(marker, screenX, screenY, zoom)
        }
    }

    internal fun DrawScope.drawPoiVisual(
        marker: MapMarkerItem, x: Float, y: Float, zoom: Float, showLabel: Boolean,
        priority: Int = 0, isHighlighted: Boolean = false
    ) {
        val style = MarkerStyleResolver.resolve(marker, zoom, priority = priority, isHighlighted = isHighlighted)
        val radius = style.radius
        drawPoiMarker(
            marker = marker,
            x = x,
            y = y,
            radius = radius,
            markerColor = style.color,
            labelTextSize = style.labelTextSize,
            showLabel = showLabel,
            isHighlighted = isHighlighted
        )
        if (marker.isSelected) {
            drawCircle(
                color = MarkerStyleConfig.SELECTION_RING_WHITE,
                radius = radius + 8f,
                center = Offset(x, y),
                style = Stroke(width = 3f)
            )
            drawCircle(
                color = MarkerStyleConfig.SELECTION_HALO_ORANGE,
                radius = radius + 13f,
                center = Offset(x, y),
                style = Stroke(width = 5f)
            )
        }
    }

    internal fun DrawScope.drawUserVisual(
        marker: MapMarkerItem, x: Float, y: Float, zoom: Float, gender: String? = null
    ) {
        val style = MarkerStyleResolver.resolve(marker, zoom, gender = gender)
        drawUserAvatarMarker(marker, x, y, style.radius * style.selectionScale, style.color)
    }

    internal fun DrawScope.drawEventVisual(
        marker: MapMarkerItem, x: Float, y: Float, zoom: Float, priority: Int = 0
    ) {
        val style = MarkerStyleResolver.resolve(marker, zoom, priority = priority)
        val markerRadius = style.radius * style.selectionScale
        drawEventMarker(
            label = marker.label,
            x = x,
            y = y,
            radius = markerRadius,
            color = MarkerStyleConfig.EVENT_LOW_PRIORITY,
            isSelected = marker.isSelected
        )
    }

    internal fun DrawScope.drawShopVisual(
        marker: MapMarkerItem, x: Float, y: Float, zoom: Float
    ) {
        val style = MarkerStyleResolver.resolve(marker, zoom)
        drawShopMarker(marker, x, y, style.radius, style.color)
    }

    private fun DrawScope.drawPoiMarker(
        marker: MapMarkerItem,
        x: Float,
        y: Float,
        radius: Float,
        markerColor: Color,
        labelTextSize: Float,
        showLabel: Boolean = true,
        isHighlighted: Boolean = false
    ) {
        val ringColor = if (isHighlighted) {
            MarkerStyleConfig.POI_RING_HIGHLIGHTED
        } else {
            MarkerStyleConfig.POI_RING_NORMAL
        }

        if (isHighlighted) {
            drawCircle(
                color = markerColor.copy(alpha = 0.28f),
                radius = radius + 8f,
                center = Offset(x, y)
            )
        }

        drawCircle(
            color = ringColor,
            radius = radius + 3f,
            center = Offset(x, y)
        )

        drawCircle(
            color = markerColor,
            radius = radius,
            center = Offset(x, y)
        )

        if (!showLabel) return

        val paint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = labelTextSize
            isFakeBoldText = isHighlighted
        }

        val textWidth = paint.measureText(marker.label)
        val labelWidth = textWidth + 22f
        val labelHeight = if (isHighlighted) 30f else 26f

        val labelLeft = x + radius + 8f
        val labelTop = y - labelHeight / 2f

        drawRoundRect(
            color = Color.Black.copy(alpha = if (isHighlighted) 0.58f else 0.42f),
            topLeft = Offset(labelLeft, labelTop),
            size = Size(labelWidth, labelHeight),
            cornerRadius = CornerRadius(13f, 13f)
        )

        drawContext.canvas.nativeCanvas.drawText(
            marker.label,
            labelLeft + 11f,
            y + labelTextSize / 3f,
            paint
        )
    }

    private fun DrawScope.drawUserAvatarMarker(
        marker: MapMarkerItem,
        x: Float,
        y: Float,
        radius: Float,
        markerColor: Color
    ) {
        val pinRadius = radius * 1.30f
        val circleCenter = Offset(x, y - pinRadius * 0.15f)
        val circleRadius = pinRadius
        val tipY = y + pinRadius * 1.60f

        val borderPath = createPinPath(
            centerX = circleCenter.x,
            centerY = circleCenter.y,
            r = circleRadius + 3.5f,
            tip = tipY + 3f
        )

        val pinPath = createPinPath(
            centerX = circleCenter.x,
            centerY = circleCenter.y,
            r = circleRadius,
            tip = tipY
        )

        if (marker.isSelected) {
            drawPath(
                path = borderPath,
                color = Color.White.copy(alpha = 0.42f),
                style = Stroke(width = radius * 0.65f)
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.22f),
                radius = radius * 1.45f,
                center = Offset(x, y)
            )
        }

        drawCircle(
            color = Color.Black.copy(alpha = 0.18f),
            radius = pinRadius * 0.55f,
            center = Offset(x + 1.5f, tipY + 2f)
        )

        drawPath(
            path = borderPath,
            color = Color.White
        )

        drawPath(
            path = pinPath,
            color = markerColor
        )

        drawCircle(
            color = Color.White,
            radius = pinRadius * 0.25f,
            center = Offset(
                x,
                circleCenter.y - pinRadius * 0.18f
            )
        )

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(
                x - pinRadius * 0.45f,
                circleCenter.y + pinRadius * 0.18f
            ),
            size = Size(
                pinRadius * 0.9f,
                pinRadius * 0.38f
            ),
            cornerRadius = CornerRadius(
                pinRadius * 0.18f,
                pinRadius * 0.18f
            )
        )
    }

    private fun createPinPath(
        centerX: Float,
        centerY: Float,
        r: Float,
        tip: Float
    ): Path {
        return Path().apply {
            moveTo(centerX, tip)

            cubicTo(
                centerX - r * 0.45f,
                centerY + r * 0.65f,
                centerX - r,
                centerY + r * 0.45f,
                centerX - r,
                centerY
            )

            cubicTo(
                centerX - r,
                centerY - r * 0.62f,
                centerX - r * 0.55f,
                centerY - r,
                centerX,
                centerY - r
            )

            cubicTo(
                centerX + r * 0.55f,
                centerY - r,
                centerX + r,
                centerY - r * 0.62f,
                centerX + r,
                centerY
            )

            cubicTo(
                centerX + r,
                centerY + r * 0.45f,
                centerX + r * 0.45f,
                centerY + r * 0.65f,
                centerX,
                tip
            )

            close()
        }
    }

    private fun DrawScope.drawShopMarker(
        marker: MapMarkerItem,
        x: Float,
        y: Float,
        radius: Float,
        markerColor: Color
    ) {
        val windowColor = MarkerStyleConfig.SHOP_WINDOW

        val iconWidth = radius * 3.3f
        val iconHeight = radius * 2.9f

        val left = x - iconWidth / 2f
        val top = y - iconHeight / 2f

        val roofPeakY = top
        val roofShoulderY = top + iconHeight * 0.26f
        val roofBaseY = top + iconHeight * 0.43f
        val scallopDepth = iconHeight * 0.16f

        val bodyTop = roofBaseY + scallopDepth * 0.75f
        val bodyLeft = left + iconWidth * 0.18f
        val bodyWidth = iconWidth * 0.64f
        val bodyHeight = iconHeight * 0.50f
        val bodyBottom = bodyTop + bodyHeight

        val roofPath = Path().apply {
            moveTo(x, roofPeakY)

            lineTo(left + iconWidth * 0.18f, roofShoulderY)

            quadraticBezierTo(
                left + iconWidth * 0.06f,
                roofShoulderY + iconHeight * 0.04f,
                left + iconWidth * 0.08f,
                roofBaseY
            )

            val startX = left + iconWidth * 0.08f
            val scallopWidth = iconWidth * 0.21f

            repeat(4) { index ->
                val sx = startX + index * scallopWidth
                val ex = sx + scallopWidth
                val cx = sx + scallopWidth / 2f

                quadraticBezierTo(
                    cx,
                    roofBaseY + scallopDepth,
                    ex,
                    roofBaseY
                )
            }

            quadraticBezierTo(
                left + iconWidth * 0.94f,
                roofShoulderY + iconHeight * 0.04f,
                left + iconWidth * 0.82f,
                roofShoulderY
            )

            lineTo(x, roofPeakY)
            close()
        }

        drawPath(
            path = roofPath,
            color = Color.White.copy(alpha = 0.45f),
            style = Stroke(width = radius * 1.25f)
        )

        drawRoundRect(
            color = Color.White.copy(alpha = 0.28f),
            topLeft = Offset(
                bodyLeft - radius * 0.45f,
                bodyTop - radius * 0.2f
            ),
            size = Size(
                bodyWidth + radius * 0.9f,
                bodyHeight + radius * 0.55f
            ),
            cornerRadius = CornerRadius(radius * 0.3f, radius * 0.3f)
        )

        drawPath(
            path = roofPath,
            color = Color.Black.copy(alpha = 0.16f),
            style = Stroke(width = radius * 0.35f)
        )

        drawRect(
            color = Color.Black.copy(alpha = 0.14f),
            topLeft = Offset(bodyLeft + 2f, bodyTop + 3f),
            size = Size(bodyWidth, bodyHeight)
        )

        drawPath(
            path = roofPath,
            color = markerColor
        )

        drawRect(
            color = markerColor,
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyWidth, bodyHeight)
        )

        drawRect(
            color = windowColor,
            topLeft = Offset(
                bodyLeft + bodyWidth * 0.58f,
                bodyTop + bodyHeight * 0.28f
            ),
            size = Size(
                bodyWidth * 0.24f,
                bodyHeight * 0.27f
            )
        )

        drawShopLabel(
            label = marker.label,
            centerX = x,
            topY = bodyBottom + 10f
        )
    }

    private fun DrawScope.drawShopLabel(
        label: String,
        centerX: Float,
        topY: Float
    ) {
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textSize = 22f
            textAlign = Paint.Align.LEFT
            isFakeBoldText = true
        }

        val textWidth = textPaint.measureText(label)

        val horizontalPadding = 10f
        val bgHeight = 28f
        val bgWidth = textWidth + horizontalPadding * 2f

        val bgLeft = centerX - bgWidth / 2f
        val bgTop = topY
        val bgCorner = 10f

        drawRoundRect(
            color = Color.Black.copy(alpha = 0.48f),
            topLeft = Offset(bgLeft, bgTop),
            size = Size(bgWidth, bgHeight),
            cornerRadius = CornerRadius(bgCorner, bgCorner)
        )

        val textX = centerX - textWidth / 2f
        val textY = bgTop + bgHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2f

        drawContext.canvas.nativeCanvas.drawText(
            label,
            textX,
            textY,
            textPaint
        )
    }

    private fun DrawScope.drawEventMarker(
        label: String,
        x: Float,
        y: Float,
        radius: Float,
        color: Color,
        isSelected: Boolean
    ) {
        val iconSize = radius * 3.5f
        val iconLeft = x - iconSize / 2f
        val iconTop = y - iconSize / 2f
        val pinPath = createLocationOnPath(iconLeft, iconTop, iconSize)
        drawPath(path = pinPath, color = color)

        val textPaint = Paint().apply {
            isAntiAlias = true
            this.color = android.graphics.Color.rgb(0x2A, 0x2A, 0x2A)
            textSize = 10.sp.toPx()
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        }
        val horizontalPadding = 7.dp.toPx()
        val verticalPadding = 3.dp.toPx()
        val textWidth = textPaint.measureText(label)
        val labelHeight = textPaint.descent() - textPaint.ascent() + verticalPadding * 2f
        val labelWidth = textWidth + horizontalPadding * 2f
        val labelTop = iconTop + iconSize + 2.dp.toPx()
        val labelLeft = x - labelWidth / 2f

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(labelLeft, labelTop),
            size = Size(labelWidth, labelHeight),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )
        drawContext.canvas.nativeCanvas.drawText(
            label,
            x - textWidth / 2f,
            labelTop + verticalPadding - textPaint.ascent(),
            textPaint
        )

        val pulseWidth = (if (isSelected) 34.dp else 22.dp).toPx()
        val pulseHeight = (if (isSelected) 13.dp else 9.dp).toPx()
        val pulseTop = labelTop + labelHeight + 2.dp.toPx()
        drawRoundRect(
            color = color.copy(alpha = if (isSelected) 0.4f else 0.2f),
            topLeft = Offset(x - pulseWidth / 2f, pulseTop),
            size = Size(pulseWidth, pulseHeight),
            cornerRadius = CornerRadius(pulseHeight / 2f, pulseHeight / 2f)
        )
    }

    /** Material `Icons.Default.LocationOn` silhouette, including its circular cutout. */
    private fun createLocationOnPath(left: Float, top: Float, size: Float): Path {
        fun px(value: Float) = left + value / 24f * size
        fun py(value: Float) = top + value / 24f * size

        return Path().apply {
            fillType = PathFillType.EvenOdd
            moveTo(px(12f), py(2f))
            cubicTo(px(8.13f), py(2f), px(5f), py(5.13f), px(5f), py(9f))
            cubicTo(px(5f), py(14.25f), px(12f), py(22f), px(12f), py(22f))
            cubicTo(px(12f), py(22f), px(19f), py(14.25f), px(19f), py(9f))
            cubicTo(px(19f), py(5.13f), px(15.87f), py(2f), px(12f), py(2f))
            close()
            addOval(Rect(px(9.5f), py(6.5f), px(14.5f), py(11.5f)))
        }
    }
}
