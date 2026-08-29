package com.example.campusconnect.feature.map.data.fake

import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerType
import com.example.campusconnect.feature.map.mapengine.MarkerSize

object FakeShopMapData {

    fun getShops(): List<MapMarker> = listOf(
        MapMarker(
            id = 101, // Strict Int ID
            sourceId = 101,
            type = MarkerType.SHOP,
            latitude = 29.946492,
            longitude = 76.815428,
            label = "Canteen",
            size = MarkerSize.MEDIUM
        )
    )
}