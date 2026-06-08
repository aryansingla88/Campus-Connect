package com.example.campusconnect.feature.map.data.fake

import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerType

object FakeUserMapData {

    fun getUsers(): List<MapMarker> = listOf(
        MapMarker(
            id = "user_1",
            sourceId = "u1",
            type = MarkerType.USER,
            latitude = 29.946900,
            longitude = 76.817000,
            label = "Aryan",
            gender = "male"
        ),
        MapMarker(
            id = "user_2",
            sourceId = "u2",
            type = MarkerType.USER,
            latitude = 29.946400,
            longitude = 76.818000,
            label = "Priya",
            gender = "female"
        )
    )
}