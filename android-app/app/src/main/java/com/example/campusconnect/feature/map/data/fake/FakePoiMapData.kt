package com.example.campusconnect.feature.map.data.fake

import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerType

object FakePoiMapData {

    fun getPois(): List<MapMarker> = listOf(

        MapMarker(
            id = "poi_1",
            sourceId = "p1",
            type = MarkerType.POI,
            latitude = 29.944632,
            longitude = 76.819063,
            label = "Library"
        ),

        MapMarker(
            id = "poi_2",
            sourceId = "p2",
            type = MarkerType.POI,
            latitude = 29.946700,
            longitude = 76.816600,
            label = "Lab"
        )
    )
}