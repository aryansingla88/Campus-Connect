package com.example.campusconnect.feature.map.mapengine.rendering

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
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
            subLabel = marker.subLabel,
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
        subLabel: String,
        x: Float,
        y: Float,
        radius: Float,
        color: Color,
        isSelected: Boolean
    ) {
        val pinRadius = radius * 1.4f
        val center = Offset(x, y - pinRadius * 0.15f)
        val tipY = center.y + pinRadius * 1.75f

        val glowRadius = pinRadius * 2.2f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = if (isSelected) 0.6f else 0.45f), Color.Transparent),
                center = center,
                radius = glowRadius
            ),
            radius = glowRadius,
            center = center
        )

        drawPath(createPinPath(center.x, center.y, pinRadius + 3f, tipY + 3f), Color.White)
        drawPath(
            path = createPinPath(center.x, center.y, pinRadius, tipY),
            brush = Brush.verticalGradient(
                colors = listOf(lerp(color, Color.White, 0.2f), lerp(color, Color.Black, 0.12f)),
                startY = center.y - pinRadius,
                endY = tipY
            )
        )
        drawCalendarGlyph(center, pinRadius * 0.95f, color)
        drawEventCard(label, subLabel, x, tipY + 3f + 3.dp.toPx(), color, isSelected)
    }

    private fun DrawScope.drawCalendarGlyph(center: Offset, size: Float, accent: Color) {
        val left = center.x - size / 2f
        val top = center.y - size / 2f + size * 0.08f
        val corner = CornerRadius(size * 0.16f, size * 0.16f)

        listOf(0.26f, 0.64f).forEach { fx ->
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(left + size * fx, top - size * 0.16f),
                size = Size(size * 0.1f, size * 0.26f),
                cornerRadius = CornerRadius(size * 0.05f, size * 0.05f)
            )
        }
        drawRoundRect(Color.White, Offset(left, top), Size(size, size * 0.88f), corner)
        drawRect(accent, Offset(left, top + size * 0.26f), Size(size, size * 0.06f))
        drawRoundRect(
            color = accent,
            topLeft = Offset(left + size * 0.2f, top + size * 0.5f),
            size = Size(size * 0.22f, size * 0.22f),
            cornerRadius = CornerRadius(size * 0.04f, size * 0.04f)
        )
    }

    private fun DrawScope.drawEventCard(
        label: String,
        subLabel: String,
        centerX: Float,
        top: Float,
        accent: Color,
        isSelected: Boolean
    ) {
        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(0x1F, 0x1F, 0x1F)
            textSize = 11.sp.toPx()
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        }
        val subPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(0x6B, 0x6B, 0x6B)
            textSize = 9.sp.toPx()
        }

        val barInset = 4.dp.toPx()
        val barWidth = 3.dp.toPx()
        val textStart = barInset + barWidth + 10.dp.toPx()
        val endPadding = 24.dp.toPx() // right gap plus chevron
        val verticalPadding = 4.dp.toPx()

        val title = TextUtils.ellipsize(
            label, TextPaint(titlePaint), 170.dp.toPx() - textStart - endPadding, TextUtils.TruncateAt.END
        ).toString()
        val titleHeight = titlePaint.descent() - titlePaint.ascent()
        val subHeight = if (subLabel.isEmpty()) 0f else subPaint.descent() - subPaint.ascent()
        val lineGap = if (subLabel.isEmpty()) 0f else 0.5.dp.toPx()

        val width = textStart + maxOf(titlePaint.measureText(title), subPaint.measureText(subLabel)) + endPadding
        val height = verticalPadding * 2f + titleHeight + lineGap + subHeight
        val left = centerX - width / 2f
        val corner = CornerRadius(12.dp.toPx(), 12.dp.toPx())
        val cardColor = Color(0xFFFFF7F0)

        drawRoundRect(Color.Black.copy(alpha = 0.14f), Offset(left, top + 2.dp.toPx()), Size(width, height), corner)
        drawRoundRect(cardColor, Offset(left, top), Size(width, height), corner)
        if (isSelected) {
            drawRoundRect(accent, Offset(left, top), Size(width, height), corner, style = Stroke(1.5.dp.toPx()))
        }
        drawRoundRect(
            color = accent,
            topLeft = Offset(left + barInset, top + barInset),
            size = Size(barWidth, height - barInset * 2f),
            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
        )

        val canvas = drawContext.canvas.nativeCanvas
        canvas.drawText(title, left + textStart, top + verticalPadding - titlePaint.ascent(), titlePaint)
        if (subLabel.isNotEmpty()) {
            canvas.drawText(
                subLabel, left + textStart, top + verticalPadding + titleHeight + lineGap - subPaint.ascent(), subPaint
            )
        }

        val chevronX = left + width - 12.dp.toPx()
        val chevronY = top + height / 2f
        val arm = 3.5.dp.toPx()
        drawPath(
            path = Path().apply {
                moveTo(chevronX - arm / 2f, chevronY - arm)
                lineTo(chevronX + arm / 2f, chevronY)
                lineTo(chevronX - arm / 2f, chevronY + arm)
            },
            color = Color(0xFF555555),
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}