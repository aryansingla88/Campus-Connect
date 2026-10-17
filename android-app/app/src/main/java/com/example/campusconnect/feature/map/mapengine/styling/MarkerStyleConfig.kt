package com.example.campusconnect.feature.map.mapengine.styling

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.campusconnect.feature.map.mapengine.model.MarkerType

/**
 * Shared styling configuration in MapEngine for all standard Campus Connect marker types.
 * Defines shared marker appearance, zoom visibility, and layer-order rules.
 */
object MarkerStyleConfig {

    // Zoom thresholds for POI visibility
    const val POI_ZOOM_MID_THRESHOLD = 2.3f
    const val POI_ZOOM_HIGH_THRESHOLD = 4.2f

    // POI Colors
    val POI_DOT_NORMAL = Color(0xFF66BB6A)
    val POI_DOT_HIGHLIGHTED = Color(0xFF00C853)
    val POI_RING_NORMAL = Color.White.copy(alpha = 0.75f)
    val POI_RING_HIGHLIGHTED = Color(0xFFB9F6CA)

    // User Colors
    val USER_FEMALE = Color(0xFFE83E8C)
    val USER_MALE = Color(0xFF4285F4)

    // Shop Colors
    val SHOP_PRIMARY = Color(0xFFC99924)
    val SHOP_WINDOW = Color(0xFFFFEFE6)

    // Event Colors
    val EVENT_HIGH_PRIORITY = Color(0xFFE63100)
    val EVENT_LOW_PRIORITY = Color(0xFFFF6F00)

    // Selection Colors
    val SELECTION_RING_WHITE = Color.White
    val SELECTION_HALO_ORANGE = Color(0xFFFFA726).copy(alpha = 0.35f)

    /** Computes the base radius from marker type and the type-specific priority. */
    fun calculateRadius(
        type: MarkerType,
        priority: Int = 0,
        isHighlighted: Boolean = false,
        isSelected: Boolean = false
    ): Float {
        val baseRadius = when (type) {
            MarkerType.POI -> when (priority) {
                2 -> 13f
                3, 4 -> 18f
                else -> 8f
            }
            MarkerType.USER, MarkerType.SHOP, MarkerType.EVENT -> 13f
        }

        val typeRadius = when (type) {
            MarkerType.POI -> {
                if (isHighlighted) baseRadius + 4f else baseRadius
            }
            MarkerType.USER -> baseRadius + 2f
            MarkerType.SHOP -> baseRadius + 1f
            MarkerType.EVENT -> baseRadius + priority.coerceIn(0, 3)
        }

        return if (isSelected) typeRadius + 4f else typeRadius
    }

    /** Keeps the existing visual-radius-plus-45px tap target in engine policy. */
    fun getHitRadius(type: MarkerType, priority: Int = 0, isSelected: Boolean = false): Float =
        calculateRadius(
            type = type,
            priority = priority,
            isHighlighted = type == MarkerType.POI && priority >= 4,
            isSelected = isSelected
        ) + 45f

    /**
     * Resolves the standard Color for given attributes.
     */
    fun getMarkerColor(
        type: MarkerType,
        gender: String? = null,
        isHighlighted: Boolean = false,
        priority: Int = 0
    ): Color {
        return when (type) {
            MarkerType.POI -> if (isHighlighted) POI_DOT_HIGHLIGHTED else POI_DOT_NORMAL
            MarkerType.USER -> if (gender == "female") USER_FEMALE else USER_MALE
            MarkerType.SHOP -> SHOP_PRIMARY
            MarkerType.EVENT -> if (priority >= 2) EVENT_HIGH_PRIORITY else EVENT_LOW_PRIORITY
        }
    }

    /**
     * Preserved for backward compatibility with Long-based color fields.
     */
    fun getMarkerColorLong(
        type: MarkerType,
        gender: String?,
        isHighlighted: Boolean,
        priority: Int
    ): Long {
        return getMarkerColor(type, gender, isHighlighted, priority).toArgb().toLong() and 0xFFFFFFFFL
    }

    /**
     * Determines minimum zoom required for marker visibility.
     */
    fun getMinZoomForMarker(type: MarkerType, priority: Int): Float {
        if (type != MarkerType.POI) return 0f
        return when (priority) {
            4 -> 0f
            3 -> POI_ZOOM_MID_THRESHOLD
            2, 1 -> POI_ZOOM_HIGH_THRESHOLD
            else -> 0f
        }
    }

    /**
     * Keeps layer order in one engine rule. Event markers remain above ordinary
     * markers, while selection raises other marker types over their peers.
     */
    fun getDrawOrder(type: MarkerType, priority: Int, isSelected: Boolean): Int = when {
        type == MarkerType.EVENT && isSelected -> 1_000 + priority.coerceIn(0, 99)
        isSelected -> 100 + priority
        type == MarkerType.EVENT -> 100 + priority.coerceIn(0, 99)
        else -> priority
    }

    /**
     * Halo scale factor for event markers.
     */
    fun getEventHaloScale(priority: Int): Float {
        return when {
            priority >= 3 -> 2.65f
            priority == 2 -> 2.25f
            else -> 1.90f
        }
    }
}
