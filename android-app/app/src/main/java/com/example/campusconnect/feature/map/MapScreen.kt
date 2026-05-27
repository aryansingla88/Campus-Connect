package com.example.campusconnect.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.feature.map.mapengine.MarkerType

@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        MapView(
            modifier = Modifier.fillMaxSize(),
            markers = uiState.renderData,
            onMarkerClick = { markerId ->
                viewModel.selectMarker(markerId)
            }
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = { viewModel.setFilter(null) },
                label = { Text("All") }
            )

            AssistChip(
                onClick = { viewModel.setFilter(MarkerType.EVENT) },
                label = { Text("Events") }
            )

            AssistChip(
                onClick = { viewModel.setFilter(MarkerType.POI) },
                label = { Text("POI") }
            )

            AssistChip(
                onClick = { viewModel.setFilter(MarkerType.SHOP) },
                label = { Text("Shops") }
            )
        }

        uiState.selectedMarker?.let { marker ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clickable { viewModel.clearSelection() },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = marker.label)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Type: ${marker.type}")
                    Text(text = "Tap card to close")
                }
            }
        }
    }
}