package com.example.campusconnect.feature.map

import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerType

class FakeMapService {

    fun getMarkers(): List<MapMarker> {
        return listOf(
            MapMarker(
                id = "event_1",
                sourceId = "1",
                type = MarkerType.EVENT,
                latitude = 29.947859,
                longitude = 76.818179,
                label = "Tech Event",
                priority = 2
            ),
            MapMarker(
                id = "poi_1",
                sourceId = "poi_1",
                type = MarkerType.POI,
                latitude = 29.944632,
                longitude = 76.819063,
                label = "Library"
            ),
            MapMarker(
                id = "shop_1",
                sourceId = "shop_1",
                type = MarkerType.SHOP,
                latitude = 29.946492,
                longitude = 76.815428,
                label = "Canteen",
                priority = 1
            ),
            MapMarker(
                id = "user_1",
                sourceId = "user_1",
                type = MarkerType.USER,
                latitude = 29.946900,
                longitude = 76.817000,
                label = "User"
            )
        )
    }
}