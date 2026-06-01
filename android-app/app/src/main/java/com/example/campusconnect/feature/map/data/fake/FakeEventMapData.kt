package com.example.campusconnect.feature.map.data.fake

import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerType

object FakeEventMapData {

    fun getEvents(): List<MapMarker> = listOf(

        MapMarker(
            id = "event_1",
            sourceId = "e1",
            type = MarkerType.EVENT,
            latitude = 29.947859,
            longitude = 76.818179,
            label = "Tech Event",
            priority = 3
        ),

        MapMarker(
            id = "event_2",
            sourceId = "e2",
            type = MarkerType.EVENT,
            latitude = 29.946900,
            longitude = 76.817300,
            label = "Workshop",
            priority = 2
        )
    )
}