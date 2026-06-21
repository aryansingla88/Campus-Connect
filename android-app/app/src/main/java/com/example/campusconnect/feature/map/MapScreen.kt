package com.example.campusconnect.feature.map

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.R
import com.example.campusconnect.core.components.PanelSearchBar
import com.example.campusconnect.feature.map.components.markerdialogs.EventMarkerDialog
import com.example.campusconnect.feature.map.components.markerdialogs.PoiMarkerDialog
import com.example.campusconnect.feature.map.components.markerdialogs.UserMarkerDialog
import com.example.campusconnect.feature.map.mapengine.MapMotion
import com.example.campusconnect.feature.map.mapengine.MapView
import com.example.campusconnect.feature.map.mapengine.MarkerType

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
                onFilterClick = {
                    showFilters = !showFilters
                },
                onSettingsClick = {}
            )

            RightSideTabs(
                selectedSidePanel = selectedSidePanel,
                onProfileClick = {
                    selectedSidePanel =
                        if (selectedSidePanel == SidePanel.PROFILE) {
                            SidePanel.NONE
                        } else {
                            SidePanel.PROFILE
                        }
                },
                onChatClick = {
                    selectedSidePanel =
                        if (selectedSidePanel == SidePanel.CHAT) {
                            SidePanel.NONE
                        } else {
                            SidePanel.CHAT
                        }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(
                        x = 6.dp,
                        y = 110.dp
                    )
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
                        start = 8.dp,
                        end = 8.dp,
                        bottom = 6.dp
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
                                    viewModel.registerEvent(eventInfo.id)

                                    Log.d(
                                        "MAP_EVENT",
                                        "Register clicked: ${eventInfo.id}"
                                    )
                                }
                            )
                        }
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
    onFilterClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f)
        ) {
            PanelSearchBar(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = "Search"
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        DiamondButton(
            onClick = onFilterClick
        )

        Spacer(modifier = Modifier.width(14.dp))

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(OrangeLight.copy(alpha = 0.95f))
                .clickable {
                    onSettingsClick()
                },
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
            .graphicsLayer {
                rotationZ = 45f
            }
            .clip(RoundedCornerShape(8.dp))
            .background(OrangeGradient)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "◆",
            modifier = Modifier.graphicsLayer {
                rotationZ = -45f
            },
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
    val tabShape = RoundedCornerShape(
        topStart = 16.dp,
        bottomStart = 16.dp,
        topEnd = 0.dp,
        bottomEnd = 0.dp
    )

    val singleTabColor = Color(0xFFFFF3E0)

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
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.35f),
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
                    tint = TextDark,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = text,
                    color = TextDark,
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
            .height(54.dp),
        shape = RoundedCornerShape(24.dp),
        color = OrangeLight.copy(alpha = 0.92f),
        tonalElevation = 10.dp,
        shadowElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = OrangePrimary.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(
                    start = 6.dp,
                    end = 6.dp,
                    top = 4.dp,
                    bottom = 4.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeBarItem(
                    emoji = "🧾",
                    selected = selectedMode == MapMode.POSTER,
                    onClick = {
                        onModeSelected(MapMode.POSTER)
                    },
                    modifier = Modifier.weight(1f)
                )

                ModeBarItem(
                    imageRes = R.drawable.home_mode,
                    selected = selectedMode == MapMode.HOME,
                    onClick = {
                        onModeSelected(MapMode.HOME)
                    },
                    modifier = Modifier.weight(1f)
                )

                ModeBarItem(
                    imageRes = R.drawable.event_mode,
                    selected = selectedMode == MapMode.EVENT,
                    onClick = {
                        onModeSelected(MapMode.EVENT)
                    },
                    modifier = Modifier.weight(1f)
                )

                ModeBarItem(
                    imageRes = R.drawable.shop_mode,
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
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    imageRes: Int? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = MapMotion.springSoft(),
        label = "mode_bar_item_scale"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) {
                    OrangePrimary.copy(alpha = 0.96f)
                } else {
                    Color.Transparent
                }
            )
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (imageRes != null) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.size(
                    if (selected) 40.dp else 36.dp
                ),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = emoji ?: "",
                fontSize = if (selected) 25.sp else 23.sp,
                lineHeight = 25.sp
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