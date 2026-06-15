package com.example.campusconnect.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.core.components.PanelSearchBar
import com.example.campusconnect.feature.map.components.markerdialogs.UserMarkerDialog
import com.example.campusconnect.feature.map.mapengine.MarkerType

private enum class MapMode {
    NONE,
    VIEW,
    SELECT,
    EVENT
}

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

    val showUserProfileDialog =
        uiState.selectedMarker?.type == MarkerType.USER &&
                uiState.selectedUserProfile != null

    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var showModes by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf(MapMode.NONE) }

    Box(modifier = Modifier.fillMaxSize()) {

        // Everything behind user dialog gets blurred.
        // User dialog itself is outside this Box, so it stays sharp.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(if (showUserProfileDialog) 3.dp else 0.dp)
        ) {
            MapView(
                modifier = Modifier.fillMaxSize(),
                markers = uiState.renderData,
                onMarkerClick = { markerId ->
                    viewModel.selectMarker(markerId)
                },
                onMapTap = { x, y ->
                    android.util.Log.d("MAP_PIXEL", "MapScreen received pixel: x=$x, y=$y")
                },
                initialFocusMarkerId = "shop_1",
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
                    .padding(bottom = 40.dp),
                onClick = {
                    val nextShowModes = !showModes
                    showModes = nextShowModes

                    if (!nextShowModes) {
                        selectedMode = MapMode.NONE
                    }
                }
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
                    selectedMode = selectedMode,
                    onModeSelected = { mode ->
                        selectedMode = mode
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 90.dp)
                )
            }
        }

        // Dialog / previews should stay outside blur Box.
        uiState.selectedMarker?.let { marker ->
            if (marker.type == MarkerType.USER) {
                uiState.selectedUserProfile?.let { profile ->
                    UserMarkerDialog(
                        profile = profile,
                        onDismiss = { viewModel.clearSelection() },
                        onAddFriendClick = {
                            android.util.Log.d(
                                "MAP_USER",
                                "Add friend clicked: ${profile.id}"
                            )
                        }
                    )
                }
            } else {
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
                    .graphicsLayer { rotationZ = -90f },
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
            .size(66.dp)
            .background(
                brush = OrangeGradient,
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = Color.White.copy(alpha = 0.35f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.GridView,
            contentDescription = "Map Modes",
            tint = Color.White,
            modifier = Modifier.size(27.dp)
        )
    }
}

@Composable
private fun ModePanel(
    selectedMode: MapMode,
    onModeSelected: (MapMode) -> Unit,
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
            icon = Icons.Default.Visibility,
            selected = selectedMode == MapMode.VIEW,
            onClick = { onModeSelected(MapMode.VIEW) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 1.dp, top = 20.dp)
        )

        SmallMode(
            text = "Select",
            icon = Icons.Default.NearMe,
            selected = selectedMode == MapMode.SELECT,
            onClick = { onModeSelected(MapMode.SELECT) },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        SmallMode(
            text = "Event",
            icon = Icons.Default.Event,
            selected = selectedMode == MapMode.EVENT,
            onClick = { onModeSelected(MapMode.EVENT) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 1.dp, top = 20.dp)
        )
    }
}

@Composable
private fun SmallMode(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) {
        OrangeLight.copy(alpha = 0.96f)
    } else {
        Color.White.copy(alpha = 0.94f)
    }

    val borderColor = if (selected) {
        OrangePrimary
    } else {
        Color.Transparent
    }

    val contentColor = if (selected) {
        OrangePrimary
    } else {
        Color(0xFF6F7682)
    }

    Box(
        modifier = modifier
            .size(64.dp)
            .background(
                color = if (selected) OrangePrimary.copy(alpha = 0.18f) else Color.Transparent,
                shape = CircleShape
            )
            .padding(if (selected) 3.dp else 0.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = if (selected) 3.dp else 0.dp,
                    color = borderColor,
                    shape = CircleShape
                ),
            shape = CircleShape,
            color = bgColor,
            tonalElevation = if (selected) 8.dp else 5.dp,
            shadowElevation = if (selected) 10.dp else 5.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = text,
                    color = if (selected) OrangePrimary else TextDark,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
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