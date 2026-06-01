package com.example.campusconnect.feature.map.data.fake

import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerType

object FakeShopMapData {

    fun getShops(): List<MapMarker> = listOf(

        MapMarker(
            id = "shop_1",
            sourceId = "s1",
            type = MarkerType.SHOP,
            latitude = 29.946492,
            longitude = 76.815428,
            label = "Canteen"
        ),

        MapMarker(
            id = "shop_2",
            sourceId = "s2",
            type = MarkerType.SHOP,
            latitude = 29.946800,
            longitude = 76.816100,
            label = "Stationery"
        ),

        MapMarker(
            id = "shop_3",
            sourceId = "s3",
            type = MarkerType.SHOP,
            latitude = 29.945900,
            longitude = 76.815800,
            label = "Juice Corner"
        )
    )
}