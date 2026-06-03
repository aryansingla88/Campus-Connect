package com.example.campusconnect.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.feature.map.mapengine.MarkerType

@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showFilters by remember { mutableStateOf(false) }
    var showModes by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        MapView(
            modifier = Modifier.fillMaxSize(),
            markers = uiState.renderData,
            onMarkerClick = { markerId ->
                viewModel.selectMarker(markerId)
            },
            onMapTap = { x, y ->
                android.util.Log.d("MAP_PIXEL", "MapScreen received pixel: x=$x, y=$y")
            },
            initialFocusMarkerId = "test_p1",
            initialZoom = 4.2f
        )

        TopMapControls(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 36.dp),
            onFilterClick = { showFilters = !showFilters },
            onSettingsClick = {}
        )

        SideTab(
            text = "PROFILE",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 10.dp)
        )

        SideTab(
            text = "CHAT",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp)
        )

        ModeButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 52.dp),
            onClick = { showModes = !showModes }
        )

        if (showFilters) {
            FilterPanel(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 94.dp, end = 72.dp),
                onFilterSelected = { type ->
                    viewModel.setFilter(type)
                    showFilters = false
                }
            )
        }

        if (showModes) {
            ModePanel(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 122.dp)
            )
        }

        uiState.selectedMarker?.let { marker ->
            MarkerPreviewCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                title = marker.label,
                type = marker.type.name,
                onClose = { viewModel.clearSelection() }
            )
        }
    }
}

@Composable
private fun TopMapControls(
    modifier: Modifier = Modifier,
    onFilterClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchBarBox(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        DiamondButton(onClick = onFilterClick)

        Spacer(modifier = Modifier.width(14.dp))

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(50.dp)
                .background(Color.White.copy(alpha = 0.9f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.Black
            )
        }
    }
}

@Composable
private fun SearchBarBox(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.88f),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Black
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Search",
                color = Color.Black.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun DiamondButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .graphicsLayer { rotationZ = 45f }
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "◆",
            modifier = Modifier.graphicsLayer { rotationZ = -45f },
            color = Color.Black
        )
    }
}

@Composable
private fun FilterPanel(
    modifier: Modifier = Modifier,
    onFilterSelected: (MarkerType?) -> Unit
) {
    Surface(
        modifier = modifier
            .width(210.dp)
            .height(340.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.9f),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Filters", color = Color.Black)

            FilterButton("All") { onFilterSelected(null) }
            FilterButton("Users") { onFilterSelected(MarkerType.USER) }
            FilterButton("Events") { onFilterSelected(MarkerType.EVENT) }
            FilterButton("POI") { onFilterSelected(MarkerType.POI) }
            FilterButton("Shops") { onFilterSelected(MarkerType.SHOP) }
        }
    }
}

@Composable
private fun FilterButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}

@Composable
private fun SideTab(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(42.dp)
            .height(260.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.78f),
        tonalElevation = 5.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.Black,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .requiredWidth(260.dp)
                    .graphicsLayer {
                        rotationZ = -90f
                    }
            )
        }
    }
}

@Composable
private fun ModeButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .size(70.dp)
            .clickable { onClick() },
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.88f),
        tonalElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("Mode", color = Color.Black)
        }
    }
}

@Composable
private fun ModePanel(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        SmallMode("View")
        SmallMode("Select")
        SmallMode("Event")
    }
}

@Composable
private fun SmallMode(text: String) {
    Surface(
        modifier = Modifier.size(66.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.88f),
        tonalElevation = 5.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = Color.Black)
        }
    }
}

@Composable
private fun MarkerPreviewCard(
    modifier: Modifier = Modifier,
    title: String,
    type: String,
    onClose: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title)
            Text("Type: $type")

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = {}) {
                    Text("Details")
                }

                OutlinedButton(onClick = {}) {
                    Text("Navigate")
                }

                OutlinedButton(onClick = onClose) {
                    Text("Close")
                }
            }
        }
    }
}