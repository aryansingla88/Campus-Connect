package com.example.campusconnect.feature.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.campusconnect.feature.map.components.MapMotion
import com.example.campusconnect.feature.map.components.markerdialogs.PoiMarkerDialog
import com.example.campusconnect.feature.map.components.markerdialogs.UserMarkerDialog
import com.example.campusconnect.feature.map.data.fake.FakeMapPoiInfoService
import com.example.campusconnect.feature.map.mapengine.MarkerType

private enum class MapMode {
    NONE,
    VIEW,
    SELECT,
    EVENT
}

private enum class SidePanel {
    NONE,
    PROFILE,
    CHAT
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

    val animatedBlur by animateDpAsState(
        targetValue = if (showUserProfileDialog) 3.dp else 0.dp,
        animationSpec = MapMotion.tweenSlow(),
        label = "map_background_blur"
    )

    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var showModes by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf(MapMode.NONE) }
    var selectedSidePanel by remember { mutableStateOf(SidePanel.NONE) }

    var lastSelectedMarker by remember { mutableStateOf(uiState.selectedMarker) }
    var lastSelectedProfile by remember { mutableStateOf(uiState.selectedUserProfile) }

    LaunchedEffect(uiState.selectedMarker, uiState.selectedUserProfile) {
        if (uiState.selectedMarker != null) {
            lastSelectedMarker = uiState.selectedMarker
            lastSelectedProfile = uiState.selectedUserProfile
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(animatedBlur)
        ) {
            MapView(
                modifier = Modifier.fillMaxSize(),
                markers = uiState.renderData,
                onMarkerClick = { markerId ->
                    selectedSidePanel = SidePanel.NONE
                    viewModel.selectMarker(markerId)
                },
                onMapTap = { x, y ->
                    selectedSidePanel = SidePanel.NONE
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

            RightSideTabs(
                selectedSidePanel = selectedSidePanel,
                onProfileClick = {
                    selectedSidePanel =
                        if (selectedSidePanel == SidePanel.PROFILE) SidePanel.NONE
                        else SidePanel.PROFILE
                },
                onChatClick = {
                    selectedSidePanel =
                        if (selectedSidePanel == SidePanel.CHAT) SidePanel.NONE
                        else SidePanel.CHAT
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 0.dp)
            )

            ModeButton(
                expanded = showModes,
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

            AnimatedVisibility(
                visible = showFilters,
                enter = fadeIn(MapMotion.tweenMedium()) +
                        slideInHorizontally(
                            animationSpec = MapMotion.tweenMedium(),
                            initialOffsetX = { it / 3 }
                        ),
                exit = fadeOut(MapMotion.tweenFast()) +
                        slideOutHorizontally(
                            animationSpec = MapMotion.tweenFast(),
                            targetOffsetX = { it / 3 }
                        ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 94.dp, end = 72.dp)
            ) {
                FilterPanel(
                    onFilterSelected = { type ->
                        viewModel.setFilter(type)
                        showFilters = false
                    }
                )
            }

            AnimatedVisibility(
                visible = showModes,
                enter = modePanelEnter(),
                exit = modePanelExit(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp)
            ) {
                ModePanel(
                    selectedMode = selectedMode,
                    onModeSelected = { mode ->
                        selectedMode = mode
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.selectedMarker != null,
            enter = markerCardEnter(),
            exit = markerCardExit()
        ) {
            lastSelectedMarker?.let { marker ->
                when (marker.type) {
                    MarkerType.USER -> {
                        lastSelectedProfile?.let { profile ->
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
                    }

                    MarkerType.POI -> {
                        val poiInfo = remember(marker.id, marker.label) {
                            FakeMapPoiInfoService.getPoiInfo(
                                poiId = marker.id,
                                fallbackName = marker.label
                            )
                        }

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            PoiMarkerDialog(
                                poi = poiInfo,
                                onNavigateClick = {
                                    android.util.Log.d(
                                        "MAP_POI",
                                        "Navigate clicked: ${poiInfo.id}"
                                    )
                                },
                                onCloseClick = {
                                    viewModel.clearSelection()
                                }
                            )
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            MarkerPreviewCard(
                                modifier = Modifier.padding(16.dp),
                                title = marker.label,
                                type = marker.type.name,
                                onClose = { viewModel.clearSelection() }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun modePanelEnter(): EnterTransition {
    return fadeIn(MapMotion.tweenMedium()) +
            slideInVertically(
                animationSpec = MapMotion.tweenMedium(),
                initialOffsetY = { it / 2 }
            ) +
            expandVertically(
                animationSpec = MapMotion.tweenMedium(),
                expandFrom = Alignment.Bottom
            )
}

private fun modePanelExit(): ExitTransition {
    return fadeOut(MapMotion.tweenFast()) +
            slideOutVertically(
                animationSpec = MapMotion.tweenFast(),
                targetOffsetY = { it / 2 }
            ) +
            shrinkVertically(
                animationSpec = MapMotion.tweenFast(),
                shrinkTowards = Alignment.Bottom
            )
}

private fun markerCardEnter(): EnterTransition {
    return fadeIn(MapMotion.tweenMedium()) +
            slideInVertically(
                animationSpec = MapMotion.tweenMedium(),
                initialOffsetY = { it / 3 }
            ) +
            scaleIn(
                initialScale = 0.98f,
                animationSpec = MapMotion.springSoft()
            )
}

private fun markerCardExit(): ExitTransition {
    return fadeOut(MapMotion.tweenFast()) +
            slideOutVertically(
                animationSpec = MapMotion.tweenFast(),
                targetOffsetY = { it / 3 }
            ) +
            scaleOut(
                targetScale = 0.98f,
                animationSpec = MapMotion.tweenFast()
            )
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
private fun RightSideTabs(
    selectedSidePanel: SidePanel,
    onProfileClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SideTab(
            text = "PROFILE",
            icon = Icons.Default.Person,
            selected = selectedSidePanel == SidePanel.PROFILE,
            onClick = onProfileClick
        )

        SideTab(
            text = "CHAT",
            icon = Icons.Default.Chat,
            selected = selectedSidePanel == SidePanel.CHAT,
            onClick = onChatClick
        )
    }
}

@Composable
private fun SideTab(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabWidth by animateDpAsState(
        targetValue = if (selected) 46.dp else 40.dp,
        animationSpec = MapMotion.springSoft(),
        label = "side_tab_width"
    )

    val tabHeight by animateDpAsState(
        targetValue = if (selected) 122.dp else 108.dp,
        animationSpec = MapMotion.springSoft(),
        label = "side_tab_height"
    )

    val rotatedWidth by animateDpAsState(
        targetValue = if (selected) 122.dp else 108.dp,
        animationSpec = MapMotion.springSoft(),
        label = "side_tab_rotated_width"
    )

    val iconSize by animateDpAsState(
        targetValue = if (selected) 16.dp else 14.dp,
        animationSpec = MapMotion.springSoft(),
        label = "side_tab_icon_size"
    )

    val elevation by animateDpAsState(
        targetValue = if (selected) 12.dp else 8.dp,
        animationSpec = MapMotion.springSoft(),
        label = "side_tab_elevation"
    )

    val textSize = if (selected) 12.sp else 11.sp

    val tabShape = RoundedCornerShape(
        topStart = 16.dp,
        bottomStart = 16.dp,
        topEnd = 0.dp,
        bottomEnd = 0.dp
    )

    val singleTabColor = Color(0xFFFFF3E0)

    Surface(
        modifier = modifier
            .width(tabWidth)
            .height(tabHeight)
            .clickable { onClick() },
        shape = tabShape,
        color = singleTabColor,
        tonalElevation = elevation,
        shadowElevation = elevation
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(singleTabColor)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.35f),
                    shape = tabShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .requiredWidth(rotatedWidth)
                    .graphicsLayer { rotationZ = -90f },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = TextDark,
                    modifier = Modifier.size(iconSize)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = text,
                    color = TextDark,
                    fontSize = textSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun ModeButton(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1.06f else 1f,
        animationSpec = MapMotion.springSoft(),
        label = "mode_button_scale"
    )

    val iconRotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = MapMotion.springSoft(),
        label = "mode_button_rotation"
    )

    Box(
        modifier = modifier
            .size(66.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
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
            modifier = Modifier
                .size(27.dp)
                .graphicsLayer { rotationZ = iconRotation }
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
            .width(230.dp)
            .height(100.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        SmallMode(
            text = "View",
            icon = Icons.Default.Visibility,
            selected = selectedMode == MapMode.VIEW,
            onClick = { onModeSelected(MapMode.VIEW) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 0.dp, top = 22.dp)
        )

        SmallMode(
            text = "Select",
            icon = Icons.Default.NearMe,
            selected = selectedMode == MapMode.SELECT,
            onClick = { onModeSelected(MapMode.SELECT) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 0.dp)
        )

        SmallMode(
            text = "Event",
            icon = Icons.Default.Event,
            selected = selectedMode == MapMode.EVENT,
            onClick = { onModeSelected(MapMode.EVENT) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 0.dp, top = 22.dp)
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
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.10f else 1f,
        animationSpec = MapMotion.springSoft(),
        label = "small_mode_scale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (selected) 0.20f else 0f,
        animationSpec = MapMotion.tweenMedium(),
        label = "small_mode_glow"
    )

    val borderAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = MapMotion.tweenMedium(),
        label = "small_mode_border_alpha"
    )

    val bgColor by animateColorAsState(
        targetValue = if (selected) {
            OrangeLight.copy(alpha = 0.98f)
        } else {
            Color.White.copy(alpha = 0.94f)
        },
        animationSpec = MapMotion.tweenMedium(),
        label = "small_mode_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            OrangePrimary
        } else {
            Color(0xFF6F7682)
        },
        animationSpec = MapMotion.tweenMedium(),
        label = "small_mode_content"
    )

    Box(
        modifier = modifier.size(68.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(66.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .background(
                    color = OrangePrimary.copy(alpha = glowAlpha),
                    shape = CircleShape
                )
                .padding(3.dp)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 3.dp,
                        color = OrangePrimary.copy(alpha = borderAlpha),
                        shape = CircleShape
                    ),
                shape = CircleShape,
                color = bgColor,
                tonalElevation = if (selected) 10.dp else 5.dp,
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
                        color = contentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
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