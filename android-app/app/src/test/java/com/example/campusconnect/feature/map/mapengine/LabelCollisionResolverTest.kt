package com.example.campusconnect.feature.map.mapengine

import androidx.compose.ui.geometry.Offset
import com.example.campusconnect.feature.map.mapengine.model.MapMarkerItem
import com.example.campusconnect.feature.map.mapengine.model.MarkerType
import com.example.campusconnect.feature.map.mapengine.rendering.LabelCollisionResolver
import com.example.campusconnect.feature.map.mapengine.rendering.LabelRect
import com.example.campusconnect.feature.map.mapengine.viewport.MapViewport
import org.junit.Assert.assertEquals
import org.junit.Test

private data class LabelMarker(
    override val id: String,
    override val x: Float,
    override val y: Float,
    override val type: MarkerType = MarkerType.POI,
) : MapMarkerItem

class LabelCollisionResolverTest {

    private val bounds = MapViewport.calculateImageBounds(1000f, 1000f)

    @Test
    fun testNonOverlappingLabels_allVisible() {
        val m1 = LabelMarker(id = "m1", x = 500f, y = 500f)
        val m2 = LabelMarker(id = "m2", x = 2500f, y = 2500f)

        val result = LabelCollisionResolver.resolveLabelVisibility(
            markers = listOf(m1, m2),
            bounds = bounds,
            scale = 1f,
            offset = Offset.Zero,
            measureLabel = { _, screenPos ->
                LabelRect(
                    left = screenPos.x,
                    top = screenPos.y,
                    right = screenPos.x + 50f,
                    bottom = screenPos.y + 20f
                )
            }
        )

        assertEquals(true, result["m1"])
        assertEquals(true, result["m2"])
    }

    @Test
    fun testCollidingLabels_prioritizesHigherPriority() {
        // Two markers placed right next to each other
        val highPriorityMarker = LabelMarker(id = "high", x = 1500f, y = 1500f)
        val lowPriorityMarker = LabelMarker(id = "low", x = 1505f, y = 1505f)

        val result = LabelCollisionResolver.resolveLabelVisibility(
            markers = listOf(lowPriorityMarker, highPriorityMarker),
            bounds = bounds,
            scale = 1f,
            offset = Offset.Zero,
            measureLabel = { _, screenPos ->
                LabelRect(
                    left = screenPos.x,
                    top = screenPos.y,
                    right = screenPos.x + 100f,
                    bottom = screenPos.y + 30f
                )
            },
            prioritySelector = { if (it.id == "high") 5 else 1 }
        )

        assertEquals(true, result["high"])
        assertEquals(false, result["low"])
    }
}
