package com.example.campusconnect.feature.map.data.fake

import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerType
import com.example.campusconnect.feature.map.mapengine.MarkerSize

object FakeUserMapData {

    fun getUsers(): List<MapMarker> = listOf(
        MapMarker(
            id = 2, // Strict Int ID
            sourceId = 2,
            type = MarkerType.USER,
            latitude = 29.946400,
            longitude = 76.818000,
            label = "Priya",
            gender = "female",
            size = MarkerSize.MEDIUM
        ),
        MapMarker(
            id = 1, // Strict Int ID
            sourceId = 1,
            type = MarkerType.USER,
            latitude = 29.946900,
            longitude = 76.817000,
            label = "Aryan",
            gender = "male",
            size = MarkerSize.MEDIUM
        )
    )
}