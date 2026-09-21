package com.example.campusconnect.feature.map

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Chat
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.core.components.PanelSearchBar
import com.example.campusconnect.feature.map.mapengine.*
import androidx.compose.foundation.shape.CircleShape
import com.example.campusconnect.feature.map.components.EventMarkerDialog
import com.example.campusconnect.feature.map.components.PoiMarkerDialog
import com.example.campusconnect.feature.map.components.UserMarkerDialog
import com.example.campusconnect.feature.map.mapengine.model.MarkerType

private enum class MapMode {
    POSTER,
    HOME,
    EVENT,
    SHOP
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
    listOf(
        OrangeTop,
        OrangePrimary
    )
)

@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val showCenteredMarkerDialog =
        uiState.selectedMarker?.type == MarkerType.USER ||
                uiState.selectedMarker?.type == MarkerType.EVENT

    val animatedBlur by animateDpAsState(
        targetValue = if (showCenteredMarkerDialog) 3.dp else 0.dp,
        animationSpec = MapMotion.tweenSlow(),
        label = "map_background_blur"
    )

    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf(MapMode.HOME) }
    var selectedSidePanel by remember { mutableStateOf(SidePanel.NONE) }

    var lastSelectedMarker by remember { mutableStateOf(uiState.selectedMarker) }
    var lastSelectedProfile by remember { mutableStateOf(uiState.selectedUserProfile) }
    var lastSelectedPoi by remember { mutableStateOf(uiState.selectedPoiInfo) }
    var lastSelectedEvent by remember { mutableStateOf(uiState.selectedEventInfo) }

    LaunchedEffect(
        uiState.selectedMarker,
        uiState.selectedUserProfile,
        uiState.selectedPoiInfo,
        uiState.selectedEventInfo
    ) {
        if (uiState.selectedMarker != null) {
            lastSelectedMarker = uiState.selectedMarker
            lastSelectedProfile = uiState.selectedUserProfile
            lastSelectedPoi = uiState.selectedPoiInfo
            lastSelectedEvent = uiState.selectedEventInfo
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

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

                    Log.d(
                        "MAP_PIXEL",
                        "MapScreen received pixel: x=$x, y=$y"
                    )
                },
                initialFocusMarkerId = "shop_1",
                initialZoom = 4.2f
            )

            TopMapControls(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp),
                searchQuery = searchQuery,
                onSearchQueryChange = { query ->
                    searchQuery = query
                },
                onProfileClick = {
                    selectedSidePanel =
                        if (selectedSidePanel == SidePanel.PROFILE) {
                            SidePanel.NONE
                        } else {
                            SidePanel.PROFILE
                        }
                },
                onSettingsClick = {}
            )

            // AFTER (Positioned near the top-right, just below the top search bar)
            RightSideTabs(
                showFilters = showFilters,
                selectedSidePanel = selectedSidePanel,
                onFiltersClick = { showFilters = !showFilters },
                onBoardClick = {
                    selectedSidePanel = if (selectedSidePanel == SidePanel.CHAT) SidePanel.NONE else SidePanel.CHAT
                },
                onPostsClick = {
                    selectedSidePanel = if (selectedSidePanel == SidePanel.PROFILE) SidePanel.NONE else SidePanel.PROFILE
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 90.dp)
                    .offset(x = 6.dp)
            )

            ModeBar(
                selectedMode = selectedMode,
                onModeSelected = { mode ->
                    selectedMode = mode

                    when (mode) {
                        MapMode.POSTER -> {
                            viewModel.setFilter(MarkerType.EVENT)
                        }

                        MapMode.HOME -> {
                            viewModel.setFilter(null)
                        }

                        MapMode.EVENT -> {
                            viewModel.setFilter(MarkerType.EVENT)
                        }

                        MapMode.SHOP -> {
                            viewModel.setFilter(MarkerType.SHOP)
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 42.dp,
                        end = 42.dp,
                        bottom = 24.dp
                    )
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

                        selectedMode = when (type) {
                            null -> MapMode.HOME
                            MarkerType.EVENT -> MapMode.EVENT
                            MarkerType.SHOP -> MapMode.SHOP
                            else -> MapMode.HOME
                        }
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.selectedMarker != null &&
                    uiState.selectedMarker?.type != MarkerType.EVENT,
            enter = markerCardEnter(),
            exit = markerCardExit()
        ) {
            lastSelectedMarker?.let { marker ->
                when (marker.type) {

                    MarkerType.USER -> {
                        lastSelectedProfile?.let { profile ->
                            UserMarkerDialog(
                                profile = profile,
                                onDismiss = {
                                    viewModel.clearSelection()
                                },
                                onAddFriendClick = {
                                    viewModel.sendConnectionRequest(profile.id)

                                    Log.d(
                                        "MAP_USER",
                                        "Add friend clicked: ${profile.id}"
                                    )
                                }
                            )
                        }
                    }

                    MarkerType.POI -> {
                        lastSelectedPoi?.let { poiInfo ->
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                PoiMarkerDialog(
                                    poi = poiInfo,
                                    onNavigateClick = {
                                        Log.d(
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
                    }

                    MarkerType.EVENT -> {
                        // Handled below via EventMarkerDialog, outside this
                        // AnimatedVisibility — ModalBottomSheet is a Popup with
                        // its own enter/exit animation, so it should not be
                        // wrapped in another slide/scale AnimatedVisibility.
                    }

                    MarkerType.SHOP -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            MarkerPreviewCard(
                                modifier = Modifier.padding(16.dp),
                                title = marker.label,
                                type = marker.type.name,
                                onClose = {
                                    viewModel.clearSelection()
                                }
                            )
                        }
                    }
                }
            }
        }

        if (uiState.selectedMarker?.type == MarkerType.EVENT) {
            lastSelectedEvent?.let { eventInfo ->
                EventMarkerDialog(
                    event = eventInfo,
                    onDismiss = {
                        viewModel.clearSelection()
                    },
                    onNavigateClick = {
                        Log.d(
                            "MAP_EVENT",
                            "Navigate clicked: ${eventInfo.id}"
                        )
                    },
                    onRegisterClick = {

                        Log.d(
                            "MAP_EVENT",
                            "Register clicked: ${eventInfo.id}"
                        )

                        // TODO:
                        // Handle event.registrationType:
                        // NONE
                        // THROUGH_APP -> navigate to in-app registration form
                        // THROUGH_LINK -> open registrationLink
                    }
                )
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading map...",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (uiState.isDetailLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading details...",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
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
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundIconButton(
            icon = Icons.Default.Person,
            contentDescription = "Profile",
            onClick = onProfileClick
        )

        Spacer(modifier = Modifier.width(14.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(30.dp))
        ) {
            PanelSearchBar(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = "Search"
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        RoundIconButton(
            icon = Icons.Default.Settings,
            contentDescription = "Settings",
            onClick = onSettingsClick
        )
    }
}

@Composable
private fun RoundIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = OrangePrimary,
            modifier = Modifier.size(20.dp)
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

            FilterButton("All") {
                onFilterSelected(null)
            }

            FilterButton("Users") {
                onFilterSelected(MarkerType.USER)
            }

            FilterButton("Events") {
                onFilterSelected(MarkerType.EVENT)
            }

            FilterButton("POI") {
                onFilterSelected(MarkerType.POI)
            }

            FilterButton("Shops") {
                onFilterSelected(MarkerType.SHOP)
            }
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
    showFilters: Boolean,
    selectedSidePanel: SidePanel,
    onFiltersClick: () -> Unit,
    onBoardClick: () -> Unit,
    onPostsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SideTab(
            text = "FILTERS",
            icon = Icons.Default.FilterList,
            selected = showFilters,
            onClick = onFiltersClick
        )

        SideTab(
            text = "BOARD",
            icon = Icons.Default.Chat,
            selected = selectedSidePanel == SidePanel.CHAT,
            onClick = onBoardClick
        )

        SideTab(
            text = "POSTS",
            icon = Icons.Default.Article,
            selected = selectedSidePanel == SidePanel.PROFILE,
            onClick = onPostsClick
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
    val tabShape = RoundedCornerShape(
        topStart = 16.dp,
        bottomStart = 16.dp,
        topEnd = 0.dp,
        bottomEnd = 0.dp
    )

    val singleTabColor = Color.White

    Surface(
        modifier = modifier
            .width(40.dp)
            .height(108.dp)
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null
            ) {
                onClick()
            },
        shape = tabShape,
        color = singleTabColor,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(singleTabColor)
                .border(
                    width = if (selected) 1.5.dp else 0.dp,
                    color = if (selected) OrangePrimary else Color.Transparent,
                    shape = tabShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .requiredWidth(108.dp)
                    .graphicsLayer {
                        rotationZ = -90f
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = if (selected) OrangePrimary else TextDark,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = text,
                    color = if (selected) OrangePrimary else TextDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun ModeBar(
    selectedMode: MapMode,
    onModeSelected: (MapMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        tonalElevation = 8.dp,
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 10.dp,
                    vertical = 6.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeBarItem(
                    icon = Icons.Default.Image,
                    label = "Poster",
                    selected = selectedMode == MapMode.POSTER,
                    onClick = {
                        onModeSelected(MapMode.POSTER)
                    },
                    modifier = Modifier.weight(1f)
                )

                ModeBarItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    selected = selectedMode == MapMode.HOME,
                    onClick = {
                        onModeSelected(MapMode.HOME)
                    },
                    modifier = Modifier.weight(1f)
                )

                ModeBarItem(
                    icon = Icons.Default.Event,
                    label = "Events",
                    selected = selectedMode == MapMode.EVENT,
                    onClick = {
                        onModeSelected(MapMode.EVENT)
                    },
                    modifier = Modifier.weight(1f)
                )

                ModeBarItem(
                    icon = Icons.Default.Storefront,
                    label = "Shop",
                    selected = selectedMode == MapMode.SHOP,
                    onClick = {
                        onModeSelected(MapMode.SHOP)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ModeBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(
                if (selected) OrangeLight else Color.Transparent
            )
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) OrangePrimary else TextDark,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = label,
                color = if (selected) OrangePrimary else TextDark,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false
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
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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