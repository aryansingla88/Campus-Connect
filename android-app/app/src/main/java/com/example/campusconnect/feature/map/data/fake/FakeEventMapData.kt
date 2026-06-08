package com.example.campusconnect.feature.map.data.fake

import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerType
import com.example.campusconnect.feature.map.mapengine.MarkerSize
object FakeEventMapData {

    fun getEvents(): List<MapMarker> = listOf(

        MapMarker(
            id = "event_1",
            sourceId = "e1",
            type = MarkerType.EVENT,
            latitude = 29.947859,
            longitude = 76.818179,
            label = "Workshop",
            priority = 1,
            size = MarkerSize.SMALL
        ),
        MapMarker(
            id = "event_2",
            sourceId = "e2",
            type = MarkerType.EVENT,
            latitude = 29.948639,
            longitude = 76.817287,
            label = "Hackathon",
            priority = 3,
            size = MarkerSize.LARGE
        )
    )
}