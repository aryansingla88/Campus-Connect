package com.example.campusconnect.event.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

data class LatLngStub(
    val latitude: Double,
    val longitude: Double
)

enum class MapMode {
    VIEW,
    SELECT
}

@Composable
fun MapView(
    mode: MapMode,
    markers: List<Any>,
    onMapClick: (LatLngStub) -> Unit
) {
    Column {
        Text("Map Placeholder")

        Button(onClick = {
            onMapClick(LatLngStub(28.61, 77.23))
        }) {
            Text("Select Dummy Location")
        }
    }
}