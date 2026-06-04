package com.example.campusconnect.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.core.components.PanelSearchBar
import com.example.campusconnect.feature.map.mapengine.MarkerType
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Chat
import androidx.compose.ui.graphics.vector.ImageVector
private val OrangePrimary = Color(0xFFFF6F00)
private val OrangeTop = Color(0xFFFFA726)
private val OrangeLight = Color(0xFFFFF3E0)
private val TextDark = Color(0xFF2A2A2A)
private val HintColor = Color(0xFFAAAAAA)

private val OrangeGradient = Brush.verticalGradient(
    listOf(OrangeTop, OrangePrimary)
)

@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
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
                .padding(top = 20.dp),
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onFilterClick = { showFilters = !showFilters },
            onSettingsClick = {}
        )

        SideTab(
            text = "PROFILE",
            icon = Icons.Default.Person,
            isLeftSide = true,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 0.dp)
        )

        SideTab(
            text = "CHAT",
            icon = Icons.Default.Chat,
            isLeftSide = false,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 0.dp)
        )

        ModeButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 35.dp),
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
                    .padding(bottom = 90.dp)
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
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            PanelSearchBar(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = "Search"
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        DiamondButton(onClick = onFilterClick)

        Spacer(modifier = Modifier.width(14.dp))

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(OrangeLight.copy(alpha = 0.95f))
                .clickable { onSettingsClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = OrangePrimary,
                modifier = Modifier.size(18.dp)
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
            .size(30.dp)
            .graphicsLayer { rotationZ = 45f }
            .clip(RoundedCornerShape(8.dp))
            .background(OrangeGradient)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "◆",
            modifier = Modifier.graphicsLayer { rotationZ = -45f },
            color = Color.White
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
        color = Color.White.copy(alpha = 0.92f),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Filters",
                color = TextDark,
                fontWeight = FontWeight.SemiBold
            )

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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = OrangePrimary
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SideTab(
    text: String,
    icon: ImageVector,
    isLeftSide: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(28.dp)
            .height(96.dp),
        shape = if (isLeftSide) {
            RoundedCornerShape(
                topStart = 0.dp,
                bottomStart = 0.dp,
                topEnd = 14.dp,
                bottomEnd = 14.dp
            )
        } else {
            RoundedCornerShape(
                topStart = 14.dp,
                bottomStart = 14.dp,
                topEnd = 0.dp,
                bottomEnd = 0.dp
            )
        },
        color = OrangeLight.copy(alpha = 0.92f),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .requiredWidth(96.dp)
                    .graphicsLayer {
                        rotationZ = -90f
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = TextDark,
                    modifier = Modifier.size(12.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = text,
                    color = TextDark,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun ModeButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(60.dp)
            .background(OrangeGradient, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Mode",
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ModePanel(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(220.dp)
            .height(90.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        SmallMode(
            text = "View",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 1.dp, top = 20.dp)
        )

        SmallMode(
            text = "Select",
            modifier = Modifier.align(Alignment.TopCenter)
        )

        SmallMode(
            text = "Event",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 1.dp, top = 20.dp)
        )
    }
}

@Composable
private fun SmallMode(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(60.dp),
        shape = CircleShape,
        color = OrangeLight.copy(alpha = 0.96f),
        tonalElevation = 5.dp,
        shadowElevation = 5.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = TextDark,
                fontWeight = FontWeight.Medium
            )
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = TextDark,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Type: $type",
                color = HintColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary
                    )
                ) {
                    Text("Details")
                }

                OutlinedButton(
                    onClick = {},
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = OrangePrimary
                    )
                ) {
                    Text("Navigate")
                }

                OutlinedButton(
                    onClick = onClose,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = OrangePrimary
                    )
                ) {
                    Text("Close")
                }
            }
        }
    }
}