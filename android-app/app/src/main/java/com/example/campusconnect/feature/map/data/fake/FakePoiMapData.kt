package com.example.campusconnect.feature.map.data.fake

import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerType
import com.example.campusconnect.feature.map.mapengine.MarkerSize
object FakePoiMapData {

    fun getPois(): List<MapMarker> = listOf(


        MapMarker(
            id = "poi_1",
            sourceId = "p1",
            type = MarkerType.POI,
            latitude = 29.943455,
            longitude = 76.818978,
            label = "Main Gate",
            isHighlighted = true,
            size = MarkerSize.LARGE
        ),
        MapMarker(
            id = "poi_2",
            sourceId = "p2",
            type = MarkerType.POI,
            latitude = 29.945381,
            longitude = 76.813989,
            label = "Library",
            size = MarkerSize.MEDIUM
        )
    )
}