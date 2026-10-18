package com.example.campusconnect.feature.map.ui

import android.util.Log
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import kotlinx.coroutines.CompletableDeferred
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.BuildConfig
import com.example.campusconnect.feature.map.ui.components.controls.*
import com.example.campusconnect.feature.map.mapengine.MapView
import com.example.campusconnect.feature.map.mapengine.MapMotion
import com.example.campusconnect.feature.map.mapengine.viewport.rememberMapViewportState
import com.example.campusconnect.feature.map.styling.HomeMarkerPainter
import com.example.campusconnect.feature.map.styling.PoiLabelResolver
import com.example.campusconnect.feature.map.model.EventMarkerData
import com.example.campusconnect.feature.map.model.LocalUserMarkerData
import com.example.campusconnect.feature.map.model.UserMarkerData
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
import com.example.campusconnect.core.location.TrackingMode
import com.example.campusconnect.core.location.model.LocationStatus

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
    isFeatureActive: Boolean = true,
    viewModel: MapViewModel = viewModel()
) {
    val cloudTransition = LocalCloudTransitionState.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapLoadComplete = remember(cloudTransition) { CompletableDeferred<Unit>() }
    val uiState by viewModel.uiState.collectAsState()
    val trackingMode by viewModel.trackingMode.collectAsState()
    var lifecycleStarted by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }
    var locationPermissionGranted by remember(context) {
        mutableStateOf(hasForegroundLocationPermission(context))
    }
    var permissionRequestCount by rememberSaveable { mutableIntStateOf(0) }
    var resumeGeneration by remember { mutableIntStateOf(0) }
    var permissionPermanentlyDenied by rememberSaveable { mutableStateOf(false) }
    var pendingRecenter by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        locationPermissionGranted = hasForegroundLocationPermission(context)
        if (!locationPermissionGranted) {
            val activity = context as? Activity
            val canExplain = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } ?: false
            permissionPermanentlyDenied = permissionRequestCount >= 2 && !canExplain
            viewModel.reportLocationPermissionDenied()
        } else {
            permissionPermanentlyDenied = false
        }
    }

    fun requestLocationPermission() {
        permissionRequestCount += 1
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            lifecycleStarted = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            if (event == Lifecycle.Event.ON_RESUME) {
                resumeGeneration += 1
                locationPermissionGranted = hasForegroundLocationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val mapIsActive = isFeatureActive && lifecycleStarted && uiState.mode != MapMode.POSTER

    LaunchedEffect(mapIsActive) {
        if (mapIsActive) {
            viewModel.startPresenceSession()
        } else {
            viewModel.stopPresenceSession()
            pendingRecenter = false
        }
    }

    LaunchedEffect(mapIsActive, locationPermissionGranted, trackingMode, resumeGeneration) {
        if (!mapIsActive) return@LaunchedEffect
        if (locationPermissionGranted) {
            viewModel.startLocationTracking(trackingMode)
        } else {
            viewModel.reportLocationPermissionDenied()
            if (permissionRequestCount == 0) requestLocationPermission()
        }
    }

    LaunchedEffect(isFeatureActive, cloudTransition, mapLoadComplete) {
        if (isFeatureActive) {
            cloudTransition.runEntry {
                mapLoadComplete.await()
            }
        }
    }

    LaunchedEffect(uiState.mode, mapLoadComplete) {
        if (uiState.mode == MapMode.POSTER) {
            // Poster mode does not compose MapView, so there is no map bitmap to await.
            mapLoadComplete.complete(Unit)
        }
    }

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
    LaunchedEffect(pendingRecenter, uiState.localLocationMarker, uiState.locationStatus, mapIsActive) {
        val marker = uiState.localLocationMarker
        if (!mapIsActive) {
            pendingRecenter = false
        } else if (pendingRecenter && marker != null &&
            (uiState.locationStatus is LocationStatus.Available || uiState.locationStatus is LocationStatus.Approximate)
        ) {
            viewportState.focusOn(
                position = Offset(marker.x, marker.y),
                zoom = viewportState.scale.coerceAtLeast(4f)
            )
            pendingRecenter = false
        }
    }
    val engineMarkers = remember(uiState.renderMarkers, uiState.localLocationMarker, uiState.mode) {
        if (uiState.mode != MapMode.POSTER) {
            uiState.renderMarkers + listOfNotNull(uiState.localLocationMarker)
        } else {
            uiState.renderMarkers
        }
    }
    LaunchedEffect(engineMarkers) {
        if (BuildConfig.DEBUG) {
            Log.d(
                "CampusMapMarkers",
                "rendered self=${engineMarkers.count { it is LocalUserMarkerData }} remoteUsers=${engineMarkers.count { it is UserMarkerData }}"
            )
        }
    }
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
                    markers = engineMarkers,
                    isMarkerTappable = { marker -> marker !is LocalUserMarkerData },
                    onMapLoadComplete = { mapLoadComplete.complete(Unit) },
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
                LocationRecenterButton(
                    onClick = {
                        val marker = uiState.localLocationMarker
                        if (locationPermissionGranted && marker != null &&
                            (uiState.locationStatus is LocationStatus.Available ||
                                uiState.locationStatus is LocationStatus.Approximate)
                        ) {
                            viewportState.focusOn(Offset(marker.x, marker.y), zoom = viewportState.scale.coerceAtLeast(4f))
                        } else {
                            pendingRecenter = uiState.localLocationMarker != null ||
                                    uiState.locationStatus !is LocationStatus.Available
                            when {
                                permissionPermanentlyDenied -> context.startActivity(
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                )
                                uiState.locationStatus is LocationStatus.PreciseLocationPermissionRequired ->
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                    )
                                !locationPermissionGranted -> requestLocationPermission()
                                uiState.locationStatus is LocationStatus.ServicesDisabled ->
                                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 92.dp)
                )

                val locationHint = if (
                    (uiState.locationStatus is LocationStatus.Available ||
                        uiState.locationStatus is LocationStatus.Approximate) && uiState.localLocationMarker == null
                ) {
                    "Your position is outside the campus map area"
                } else {
                    locationStatusHint(
                        uiState.locationStatus,
                        hasLastKnownLocation = uiState.localLocationMarker != null,
                        trackingMode = trackingMode
                    )
                }
                if (mapIsActive && locationHint != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 20.dp, bottom = 148.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.94f),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = locationHint,
                            color = TextDark,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

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

@Composable
private fun LocationRecenterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(48.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 5.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = "Recenter map on my location",
                tint = OrangePrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private fun hasForegroundLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

private fun locationStatusHint(
    status: LocationStatus,
    hasLastKnownLocation: Boolean,
    trackingMode: TrackingMode
): String? = when (status) {
    LocationStatus.Unavailable -> null
    LocationStatus.PermissionDenied -> if (hasLastKnownLocation) {
        "Showing last known position; allow location access to update it"
    } else {
        "Allow location access to show your position"
    }
    LocationStatus.PreciseLocationPermissionRequired ->
        "High Precision requires Precise location permission. Change it in app settings."
    LocationStatus.ServicesDisabled -> if (hasLastKnownLocation) {
        "Showing last known position; turn on device location to update it"
    } else {
        "Turn on device location to find your position"
    }
    LocationStatus.Acquiring -> if (trackingMode == TrackingMode.HIGH_PRECISION) {
        if (hasLastKnownLocation) {
            "Acquiring a fresh GPS fix; showing your last known position"
        } else {
            "Acquiring GPS fix…"
        }
    } else if (hasLastKnownLocation) {
        "Finding a fresh location; showing your last known position"
    } else {
        "Finding your location…"
    }
    is LocationStatus.Available -> null
    is LocationStatus.Approximate -> "Current location is fresh, but GPS accuracy is low (±${status.fix.accuracyMeters.toInt()} m)"
    is LocationStatus.Stale -> if (hasLastKnownLocation) {
        "Showing your last known position while waiting for a fresh fix"
    } else {
        "Waiting for a fresh location"
    }
    is LocationStatus.InsufficientAccuracy -> if (hasLastKnownLocation) {
        "Showing your last known position; new fix is too approximate"
    } else {
        "Location is too approximate for this mode"
    }
    is LocationStatus.Error -> if (hasLastKnownLocation) {
        "Showing your last known position; location is temporarily unavailable"
    } else {
        "Location is temporarily unavailable"
    }
}

