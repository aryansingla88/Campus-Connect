package com.example.campusconnect.feature.map.model

import com.example.campusconnect.feature.map.mapengine.model.MapMarkerItem
import com.example.campusconnect.feature.map.mapengine.model.MarkerType

/** Shared map identity, geographic source location, and positioned selection state. */
sealed class MapMarker(
    final override val id: String,
    final override val x: Float = 0f,
    final override val y: Float = 0f,
    final override val isSelected: Boolean = false
) : MapMarkerItem {
    abstract val sourceId: Int
    abstract val latitude: Double
    abstract val longitude: Double
    abstract override val type: MarkerType

    abstract fun withMapPosition(x: Float, y: Float): MapMarker
    abstract fun withSelection(isSelected: Boolean): MapMarker
}

data class UserMarkerData(
    override val sourceId: Int,
    override val latitude: Double,
    override val longitude: Double,
    val username: String,
    val userGender: String? = null,
    val insideCampus: Boolean? = null,
    private val markerId: String = "USER_$sourceId",
    private val mapX: Float = 0f,
    private val mapY: Float = 0f,
    private val selected: Boolean = false
) : MapMarker(markerId, mapX, mapY, selected) {
    override val type: MarkerType get() = MarkerType.USER
    override val label: String get() = username

    override fun withMapPosition(x: Float, y: Float) = copy(mapX = x, mapY = y)
    override fun withSelection(isSelected: Boolean) = copy(selected = isSelected)
}

/** Device-local position. It is never mapped from or uploaded as a remote user marker. */
data class LocalUserMarkerData(
    override val sourceId: Int = 0,
    override val latitude: Double,
    override val longitude: Double,
    val isStale: Boolean,
    private val mapX: Float,
    private val mapY: Float
) : MapMarker("LOCAL_DEVICE", mapX, mapY, false) {
    override val type: MarkerType get() = MarkerType.USER
    override val label: String get() = "You"

    override fun withMapPosition(x: Float, y: Float) = copy(mapX = x, mapY = y)
    override fun withSelection(isSelected: Boolean) = this
}

data class PoiMarkerData(
    override val sourceId: Int,
    override val latitude: Double,
    override val longitude: Double,
    val name: String,
    val category: String?,
    val iconType: String?,
    val visibility: String?,
    val priority: Int,
    private val markerId: String = "POI_$sourceId",
    private val mapX: Float = 0f,
    private val mapY: Float = 0f,
    private val selected: Boolean = false
) : MapMarker(markerId, mapX, mapY, selected) {
    override val type: MarkerType get() = MarkerType.POI
    override val label: String get() = name
    val isHighlighted: Boolean get() = priority >= 4

    override fun withMapPosition(x: Float, y: Float) = copy(mapX = x, mapY = y)
    override fun withSelection(isSelected: Boolean) = copy(selected = isSelected)
}

data class EventMarkerData(
    override val sourceId: Int,
    override val latitude: Double,
    override val longitude: Double,
    val title: String,
    val priority: Int,
    private val markerId: String = "EVENT_$sourceId",
    private val mapX: Float = 0f,
    private val mapY: Float = 0f,
    private val selected: Boolean = false
) : MapMarker(markerId, mapX, mapY, selected) {
    override val type: MarkerType get() = MarkerType.EVENT
    override val label: String get() = title
    override fun withMapPosition(x: Float, y: Float) = copy(mapX = x, mapY = y)
    override fun withSelection(isSelected: Boolean) = copy(selected = isSelected)
}

data class ShopMarkerData(
    override val sourceId: Int,
    override val latitude: Double,
    override val longitude: Double,
    val name: String,
    val category: String? = null,
    val isOpen: Boolean? = null,
    private val markerId: String = "SHOP_$sourceId",
    private val mapX: Float = 0f,
    private val mapY: Float = 0f,
    private val selected: Boolean = false
) : MapMarker(markerId, mapX, mapY, selected) {
    override val type: MarkerType get() = MarkerType.SHOP
    override val label: String get() = name

    override fun withMapPosition(x: Float, y: Float) = copy(mapX = x, mapY = y)
    override fun withSelection(isSelected: Boolean) = copy(selected = isSelected)
}
