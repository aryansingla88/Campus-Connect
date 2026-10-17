package com.example.campusconnect.feature.map.ui

import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.feature.map.ui.components.controls.*
import com.example.campusconnect.feature.map.mapengine.MapView
import com.example.campusconnect.feature.map.mapengine.MapMotion
import com.example.campusconnect.feature.map.mapengine.viewport.rememberMapViewportState
import com.example.campusconnect.feature.map.styling.HomeMarkerPainter
import com.example.campusconnect.feature.map.styling.PoiLabelResolver
import com.example.campusconnect.feature.map.model.EventMarkerData
import com.example.campusconnect.feature.map.model.PoiMarkerData
import com.example.campusconnect.feature.map.model.MapMode
import com.example.campusconnect.feature.map.model.MapFilterOption
import com.example.campusconnect.feature.map.model.MapFilterContext
import com.example.campusconnect.feature.map.model.ALL_POIS_FILTER_ID
import com.example.campusconnect.feature.map.model.MapFilterAction
import com.example.campusconnect.feature.map.model.resolveMapFilterTap
import com.example.campusconnect.feature.map.model.selectedMapFilterKeys
import androidx.compose.foundation.shape.CircleShape
import com.example.campusconnect.feature.map.ui.components.events.EventMarkerDialog
import com.example.campusconnect.feature.map.ui.components.markers.PoiMarkerDialog
import com.example.campusconnect.feature.map.ui.components.markers.UserMarkerDialog
import com.example.campusconnect.feature.map.ui.components.markers.SelectedMarkerContent
import com.example.campusconnect.feature.map.mapengine.model.MarkerType
import androidx.compose.ui.geometry.Offset

internal val OrangePrimary = Color(0xFFFF6F00)
internal val OrangeLight = Color(0xFFFFF3E0)
internal val TextDark = Color(0xFF2A2A2A)
internal val HintColor = Color(0xFFAAAAAA)

@Composable
fun MapScreen(
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onHomePostsClick: () -> Unit = {},
    onEventPostsClick: () -> Unit = {},
    onEventFeatureClick: () -> Unit = {},
    onPosterClick: () -> Unit = {},
    viewModel: MapViewModel = viewModel()
) {
    val cloudTransition = LocalCloudTransitionState.current
    val coroutineScope = rememberCoroutineScope()
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
    var selectedSidePanel by remember { mutableStateOf(SidePanel.NONE) }
    var expandedFilterSection by remember { mutableStateOf<String?>(null) }
    var expandedFilterRows by remember { mutableIntStateOf(0) }

    var lastSelectedMarker by remember { mutableStateOf(uiState.selectedMarker) }
    var lastSelectedProfile by remember { mutableStateOf(uiState.selectedUserProfile) }
    var lastSelectedPoi by remember { mutableStateOf(uiState.selectedPoiInfo) }

    LaunchedEffect(uiState.mode) {
        showFilters = false
        expandedFilterSection = null
        expandedFilterRows = 0
        selectedSidePanel = SidePanel.NONE
    }

    LaunchedEffect(
        uiState.selectedMarker,
        uiState.selectedUserProfile,
        uiState.selectedPoiInfo,
    ) {
        if (uiState.selectedMarker != null) {
            lastSelectedMarker = uiState.selectedMarker
            lastSelectedProfile = uiState.selectedUserProfile
            lastSelectedPoi = uiState.selectedPoiInfo
        }
    }

    val viewportState = rememberMapViewportState(initialZoom = 4.2f)
    var initialFocusApplied by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.renderMarkers) {
        if (!initialFocusApplied && uiState.renderMarkers.isNotEmpty()) {
            val focusMarker = uiState.renderMarkers.firstOrNull { it.id == "shop_1" }
                ?: uiState.renderMarkers.firstOrNull()
            focusMarker?.let { marker ->
                viewportState.focusOn(Offset(marker.x, marker.y), zoom = 4.2f)
                initialFocusApplied = true
            }
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
            if (uiState.mode != MapMode.POSTER) {

                MapView(
                    modifier = Modifier.fillMaxSize(),
                    viewportState = viewportState,
                    markers = uiState.renderMarkers,
                    markerPriority = { marker ->
                        when (marker) {
                            is PoiMarkerData -> marker.priority
                            is EventMarkerData -> marker.priority
                            else -> 0
                        }
                    },
                    markerPainter = HomeMarkerPainter,
                    onMarkerClick = { markerId ->
                        selectedSidePanel = SidePanel.NONE
                        viewModel.selectMarker(markerId)
                        uiState.renderMarkers.firstOrNull { it.id == markerId }?.let { marker ->
                            viewportState.focusOn(
                                position = Offset(marker.x, marker.y),
                                zoom = viewportState.scale.coerceAtLeast(3.5f)
                            )
                        }
                    },
                    onMapTap = { _, _ ->
                        selectedSidePanel = SidePanel.NONE
                        if (uiState.selectedMarker?.type == MarkerType.POI) {
                            viewModel.clearSelection()
                        }
                    },
                    labelVisibilityResolver = { visibleMarkers, bounds, scale, offset ->
                        PoiLabelResolver.resolvePoiLabelVisibility(
                            poiMarkers = visibleMarkers.filterIsInstance<PoiMarkerData>(),
                            bounds = bounds,
                            scale = scale,
                            offset = offset
                        )
                    }
                )
            }

            TopMapControls(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp),
                searchQuery = searchQuery,
                onSearchQueryChange = { query ->
                    searchQuery = query
                },
                onProfileClick = onProfileClick,
                onSettingsClick = onSettingsClick
            )

            if (uiState.mode != MapMode.POSTER) {

                RightSideTabs(
                    showFilters = showFilters,
                    filtersAvailable = uiState.filterContext != MapFilterContext.NONE,
                    selectedSidePanel = selectedSidePanel,

                    onFiltersClick = {
                        showFilters = !showFilters
                        expandedFilterSection = null
                        expandedFilterRows = 0
                    },
                    expandedFilterRows = expandedFilterRows,

                    onBoardClick = {
                        selectedSidePanel =
                            if (selectedSidePanel == SidePanel.CHAT)
                                SidePanel.NONE
                            else
                                SidePanel.CHAT
                    },

                    onPostsClick = {
                        when (uiState.mode) {

                            MapMode.HOME ->
                                onHomePostsClick()

                            MapMode.EVENT ->
                                onEventPostsClick()

                            MapMode.SHOP -> {
                                // Ignore
                            }

                            MapMode.POSTER -> {
                                // Never visible
                            }
                        }
                    },

                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 90.dp)
                        .offset(x = 6.dp)
                )
            }

            ModeBar(
                selectedMode = uiState.mode,
                onModeSelected = { mode ->
                    if (mode != uiState.mode && !cloudTransition.isRunning) {
                        coroutineScope.launch {
                            cloudTransition.run {
                                viewModel.selectMode(mode)
                                when (mode) {
                                    MapMode.POSTER -> onPosterClick()
                                    MapMode.HOME -> viewportState.resetViewport()
                                    MapMode.EVENT, MapMode.SHOP -> Unit
                                }
                            }
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

            if (uiState.mode == MapMode.EVENT) {

                EventShortcutButton(
                    onClick = onEventFeatureClick,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(
                            start = 24.dp,
                            bottom = 38.dp
                        )
                )
            }

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
                    .padding(top = 90.dp)
            ) {
                val allPoiOption = MapFilterOption(ALL_POIS_FILTER_ID, "All POIs")
                val filterOptions = listOf(allPoiOption) + uiState.poiCategories
                val selectedFilterKeys = selectedMapFilterKeys(uiState)
                FilterPanel(
                    filterContext = uiState.filterContext,
                    userOptions = listOf(MapFilterOption("users", "Users")),
                    poiCategories = filterOptions,
                    eventCategories = uiState.eventCategories.map {
                        MapFilterOption(it.id.toString(), it.name)
                    },
                    selectedFilterKeys = selectedFilterKeys,
                    expandedSection = expandedFilterSection,
                    onExpandedSectionChange = { section, visibleRows ->
                        expandedFilterSection = section
                        expandedFilterRows = visibleRows
                    },
                    onOptionSelected = { section, option ->
                        when (val action = resolveMapFilterTap(uiState, section, option.id)) {
                            MapFilterAction.ClearHomeFilters -> when (section) {
                                "Users" -> viewModel.setUserFilter(null)
                                "Places" -> viewModel.clearPoiFilter()
                                else -> Unit
                            }
                            MapFilterAction.ClearUserFilter -> viewModel.setUserFilter(null)
                            is MapFilterAction.SelectUserFilter -> viewModel.setUserFilter(action.filterId)
                            MapFilterAction.ClearPoiFilter -> viewModel.clearPoiFilter()
                            MapFilterAction.SelectAllPois -> viewModel.setPoiCategory(null)
                            is MapFilterAction.SelectPoiCategory -> viewModel.setPoiCategory(action.category)
                            MapFilterAction.ShowAllEvents -> viewModel.setEventCategory(null)
                            is MapFilterAction.SelectEventCategory ->
                                viewModel.setEventCategory(action.categoryId)
                            MapFilterAction.Ignore -> Unit
                        }
                    },
                    onClose = {
                        showFilters = false
                        expandedFilterSection = null
                        expandedFilterRows = 0
                    }
                )
            }
        }

        SelectedMarkerContent(
            selectedMarker = uiState.selectedMarker,
            lastSelectedMarker = lastSelectedMarker,
            selectedProfile = lastSelectedProfile,
            selectedPoi = lastSelectedPoi,
            onDismiss = viewModel::clearSelection,
            onAddFriendClick = viewModel::sendConnectionRequest
        )
        val selectedEventMarker = uiState.selectedMarker as? EventMarkerData
        val selectedEventInfo = uiState.selectedEventInfo
        if (
            selectedEventMarker != null &&
            selectedEventInfo != null &&
            selectedEventInfo.id == selectedEventMarker.sourceId
        ) {
            EventMarkerDialog(
                event = selectedEventInfo,
                onDismiss = viewModel::clearSelection,
                endTime = selectedEventInfo.endTime,
                description = selectedEventInfo.description,
                registrationsCount = selectedEventInfo.registrationCount,
                eventType = selectedEventInfo.eventType,
                onNavigateClick = {},
                onRegisterClick = {
                    // Registration actions remain owned by the Events feature.
                }
            )
        }

        if (
            uiState.selectedMarker is EventMarkerData &&
            !uiState.isDetailLoading &&
            uiState.detailErrorMessage != null
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "Couldn't load event preview: ${uiState.detailErrorMessage}",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFFB3261E), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
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

