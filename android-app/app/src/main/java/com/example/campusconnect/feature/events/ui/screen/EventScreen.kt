package com.example.campusconnect.feature.events.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.feature.events.model.Event
import com.example.campusconnect.feature.events.model.EventFilter
import com.example.campusconnect.feature.events.model.EventStatus
import com.example.campusconnect.feature.events.ui.components.BackButton
import com.example.campusconnect.feature.events.ui.components.CreateEventButton
import com.example.campusconnect.feature.events.ui.components.MapViewButton
import com.example.campusconnect.feature.events.ui.components.ModeToggle
import com.example.campusconnect.feature.events.ui.dialog.EventAccessDialog
import com.example.campusconnect.feature.events.ui.dialog.EventCreateDialog
import com.example.campusconnect.feature.events.ui.drawer.EventHistoryDrawer
import com.example.campusconnect.feature.events.ui.drawer.EventParticipantsDrawer
import com.example.campusconnect.feature.events.ui.preview.EventPreviewSheet
import com.example.campusconnect.feature.events.viewmodel.EventViewModel
import com.example.campusconnect.feature.map.mapengine.MapCalibration
import com.example.campusconnect.feature.map.mapengine.MapView
import com.example.campusconnect.feature.map.mapengine.model.MapMarkerItem
import com.example.campusconnect.feature.map.mapengine.model.MarkerType
import com.example.campusconnect.feature.map.mapengine.rendering.StandardMarkerPainter
import com.example.campusconnect.feature.map.mapengine.viewport.DEFAULT_ZOOM
import com.example.campusconnect.feature.map.mapengine.viewport.MAP_IMAGE_HEIGHT
import com.example.campusconnect.feature.map.mapengine.viewport.MAP_IMAGE_WIDTH
import com.example.campusconnect.feature.map.mapengine.viewport.rememberMapViewportState
import kotlinx.coroutines.delay

private data class EventMapMarker(
    override val id: String,
    override val x: Float,
    override val y: Float,
    override val label: String,
    override val isSelected: Boolean,
    val priority: Int
) : MapMarkerItem {
    override val type: MarkerType = MarkerType.EVENT
}

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun EventScreen(
    onBack: () -> Unit
) {


    val viewModel: EventViewModel = viewModel()

    val state by viewModel.uiState.collectAsState()
    val events by viewModel.displayedEvents.collectAsState()
    val eventFilter by viewModel.eventFilter.collectAsState()
    val liveHistory by viewModel.liveHistory.collectAsState()
    val upcomingHistory by viewModel.upcomingHistory.collectAsState()
    val pastHistory by viewModel.pastHistory.collectAsState()
    val medalCandidates by viewModel.medalCandidates.collectAsState()
    val activeIndex        by viewModel.activeEventIndex.collectAsState()
    val showPreview        by viewModel.showPreview.collectAsState()
    val isEditMode         by viewModel.isEditMode.collectAsState()
    val pendingDeleteEvent by viewModel.pendingDeleteEvent.collectAsState()

    var dialogKey           by remember { mutableStateOf(0) }
    var showDialog          by remember { mutableStateOf(false) }
    var isSelectingLocation by remember { mutableStateOf(false) }
    var wasEditMode         by remember { mutableStateOf(false) }

    var showAccessDialog    by remember { mutableStateOf(false) }
    var accessEvent         by remember { mutableStateOf<Event?>(null) }

    var showParticipants    by remember { mutableStateOf(false) }
    var showHistoryDrawer   by remember { mutableStateOf(false) }

    var showViewMode by remember { mutableStateOf(false) }

    var toastMessage        by remember { mutableStateOf<String?>(null) }

    val mapViewportState = rememberMapViewportState(initialZoom = DEFAULT_ZOOM)

    val clubs by viewModel.clubs.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val eventPreviews by viewModel.eventPreviews.collectAsState()

    val eventMapMarkers = remember(events, activeIndex) {
        events.mapIndexedNotNull { index, event ->
            if (event.status == EventStatus.PAST) {
                return@mapIndexedNotNull null
            }

            val point = MapCalibration.converter.latLngToPoint(
                lat = event.latitude,
                lng = event.longitude
            )

            EventMapMarker(
                id = event.id.toString(),
                x = point.x,
                y = point.y,
                label = event.title,
                isSelected = index == activeIndex,
                priority = if (index == activeIndex) 2 else 0
            )
        }
    }


    // Only non-past events appear in the preview pager
    val filteredEvents = remember(events) {
        events.filter { it.status != EventStatus.PAST }
    }


    val filteredActiveIndex = remember(activeIndex, filteredEvents, events) {
        val activeEvent = events.getOrNull(activeIndex)
        if (activeEvent != null) filteredEvents.indexOf(activeEvent).coerceAtLeast(0)
        else 0
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) { delay(2000); toastMessage = null }
    }

    LaunchedEffect(state.success) {
        if (state.success) {
            showDialog = false
            viewModel.resetForm()
            toastMessage = if (wasEditMode) "Event Updated Successfully"
            else             "Event Created Successfully"
        }
    }

    val deleteSuccess by viewModel.deleteSuccess.collectAsState()
    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            toastMessage = "Event Deleted Successfully"
            viewModel.clearDeleteSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        MapView(
            modifier = Modifier.fillMaxSize(),
            viewportState = mapViewportState,
            markers = eventMapMarkers,
            markerPriority = { marker -> marker.priority },
            markerPainter = StandardMarkerPainter,
            initialFocusPosition = Offset(
                MAP_IMAGE_WIDTH / 2f,
                MAP_IMAGE_HEIGHT / 2f
            ),
            initialZoom = DEFAULT_ZOOM,
            onMarkerClick = { markerId ->
                if (!isSelectingLocation) {
                    val index = events.indexOfFirst { it.id.toString() == markerId }
                    if (index >= 0 && events[index].status != EventStatus.PAST) {
                        viewModel.onMarkerTapped(index)
                    }
                }
            },
            onMapTap = { mapX, mapY ->
                if (isSelectingLocation) {
                    val (latitude, longitude) =
                        MapCalibration.converter.pointToLatLng(mapX, mapY)

                    viewModel.setLocation(
                        lat = latitude,
                        lng = longitude
                    )

                    isSelectingLocation = false
                    showDialog = true
                }
            }
        )

        // ── TAP ANYWHERE HINT ─────────────────────────────────────────────────
        if (isSelectingLocation) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    "Tap anywhere to place event",
                    color      = Color(0xFF2A2A2A),
                    fontWeight = FontWeight.Medium,
                    fontSize   = 16.sp
                )
            }
        }

        // ── TOP BAR ───────────────────────────────────────────────────────────
        if (!isSelectingLocation) {
            // Self / Shared toggles — centered
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(30.dp),
                        ambientColor = Color.Black.copy(alpha = 0.20f),
                        spotColor = Color.Black.copy(alpha = 0.20f)
                    )
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                ModeToggle(
                    text = "Mine",
                    icon = Icons.Default.Person,
                    selected = eventFilter == EventFilter.SELF,
                    onClick = {
                        viewModel.setEventFilter(EventFilter.SELF)
                    }
                )

                ModeToggle(
                    text = "Shared",
                    icon = Icons.Default.Group,
                    selected = eventFilter == EventFilter.SHARED,
                    onClick = {
                        viewModel.setEventFilter(EventFilter.SHARED)
                    }
                )
            }

            // Keep history available while the event preview is open.
            if (!showHistoryDrawer) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                        .size(46.dp)
                        .background(
                            color = Color(0xFFFF6F00),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember {
                                MutableInteractionSource()
                            }
                        ) {
                            viewModel.loadEventHistory()
                            showHistoryDrawer = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .height(2.dp)
                                    .background(Color.White, RoundedCornerShape(1.dp))
                            )
                        }
                    }
                }
            }
        }

        if (!isSelectingLocation) {
            BackButton (
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart)
            )
        }

        // ── BOTTOM LEFT — View mode button ────────────────────────────────────
        if (!isSelectingLocation) {
            MapViewButton(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp),
                onClick = {
                    showViewMode = true
                }
            )
        }

        // ── BOTTOM RIGHT — Create event button ────────────────────────────────
        if (!isSelectingLocation) {
            CreateEventButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
                onClick = {
                    isSelectingLocation = true
                }
            )
        }

        // ── PREVIEW SHEET ─────────────────────────────────────────────────────
        if (showPreview && filteredEvents.isNotEmpty() && !isSelectingLocation) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .zIndex(1f)
            ) {
                EventPreviewSheet(
                    events        = filteredEvents,
                    activeIndex   = filteredActiveIndex,
                    onPageChanged = { index ->
                        val globalIndex = events.indexOf(filteredEvents.getOrNull(index))
                        if (globalIndex >= 0) viewModel.onPreviewPageChanged(globalIndex)
                        showParticipants = false
                    },
                    onClose       = {
                        showParticipants = false
                        viewModel.closePreview()
                    },
                    onEdit        = { event ->
                        viewModel.loadEventForEdit(event)
                        wasEditMode = true
                        dialogKey++
                        showDialog = true
                    },
                    onRegistration = { },
                    onChat         = { },
                    onAccess       = { event -> accessEvent = event; viewModel.loadAccessUsers(event.id); showAccessDialog = true },
                    onDelete       = { event -> viewModel.requestDelete(event) }
                )
            }
        }

        // ── PARTICIPANTS DRAWER — flush against right edge ────────────────────
        if (showPreview && filteredEvents.isNotEmpty() && !isSelectingLocation && !showDialog) {
            val currentEvent = filteredEvents.getOrNull(filteredActiveIndex)
            if (currentEvent != null) {
                Box(modifier = Modifier.fillMaxSize().zIndex(2f)) {
                    EventParticipantsDrawer(
                        event = currentEvent,
                        teams = viewModel.teams.collectAsState().value,
                        solo = viewModel.soloParticipants.collectAsState().value,
                        total = viewModel.participantsCount.collectAsState().value,
                        isOpen = showParticipants,
                        onToggle = {
                            viewModel.loadParticipants(currentEvent.id)
                            showParticipants = !showParticipants
                        }
                    )
                }
            }
        }

        // ── HISTORY DRAWER ────────────────────────────────────────────────────
        if (!isSelectingLocation && !showDialog) {
            Box(modifier = Modifier.fillMaxSize().zIndex(2f)) {
                EventHistoryDrawer(
                    isOpen = showHistoryDrawer,
                    onToggle = { showHistoryDrawer = !showHistoryDrawer },

                    liveEvents = liveHistory,
                    upcomingEvents = upcomingHistory,
                    pastEvents = pastHistory,

                    medals = viewModel.medals.collectAsState().value,

                    teams = viewModel.teams.collectAsState().value,

                    soloParticipants =
                        viewModel.soloParticipants.collectAsState().value,

                    medalCandidates =
                        medalCandidates,

                    onLoadMedals = { eventId: Int ->
                        viewModel.loadMedals(eventId)
                    },

                    onLoadParticipants = {
                        viewModel.loadParticipants(it)
                    },

                    onLoadMedalCandidates = {
                        viewModel.loadMedalCandidates(it)
                    },

                    onAwardMedal = { award ->
                        viewModel.awardMedal(award)
                    },

                    onRemoveMedal = { eventId, medalType ->
                        viewModel.removeMedal(
                            eventId,
                            medalType
                        )
                    },
                )
            }
        }

        // ── TOAST ─────────────────────────────────────────────────────────────
        toastMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(3f)
                    .padding(bottom = 320.dp)
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(msg, color = Color(0xFF2A2A2A), fontWeight = FontWeight.Medium, fontSize = 16.sp)
            }
        }

        // ── DELETE CONFIRMATION ───────────────────────────────────────────────
        pendingDeleteEvent?.let { event ->
            AlertDialog(
                onDismissRequest = viewModel::cancelDelete,
                shape            = RoundedCornerShape(20.dp),
                containerColor   = Color.White,
                title = { Text("Delete Event?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2A2A2A)) },
                text  = { Text("Are you sure you want to delete \"${event.title}\"? This cannot be undone.", fontSize = 14.sp, color = Color(0xFF555555), lineHeight = 20.sp) },
                dismissButton = {
                    OutlinedButton(onClick = viewModel::cancelDelete, shape = RoundedCornerShape(12.dp), border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6F00))) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                },
                confirmButton = {
                    Button(onClick = viewModel::confirmDelete, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) {
                        Text("Delete", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        // ── ACCESS DIALOG ─────────────────────────────────────────────────────
        if (showAccessDialog && accessEvent != null) {
            EventAccessDialog(
                event = accessEvent!!,

                users = viewModel.accessUsers.collectAsState().value,

                searchResults = viewModel.searchResults.collectAsState().value,

                onSearch = { query ->
                    viewModel.searchAccessUsers(
                        accessEvent!!.id,
                        query
                    )
                },

                onGrantAccess = { userId ->
                    viewModel.grantAccess(
                        eventId = accessEvent!!.id,
                        userId = userId
                    )
                },

                onRevokeAccess = { userId ->
                    viewModel.revokeAccess(
                        eventId = accessEvent!!.id,
                        userId = userId
                    )
                },

                onDismiss = {
                    showAccessDialog = false
                    accessEvent = null
                }
            )
        }

        // ── CREATE / EDIT DIALOG ──────────────────────────────────────────────
        if (showDialog) {
            Box(modifier = Modifier.zIndex(4f)) {
                key(dialogKey) {
                    EventCreateDialog(
                        state      = state,
                        isEditMode = isEditMode,
                        onTitleChange            = viewModel::updateTitle,
                        onDescriptionChange      = viewModel::updateDescription,
                        onDateChange             = viewModel::updateDate,
                        onVenueChange            = viewModel::updateVenue,
                        onStartTimeChange        = viewModel::updateStartTime,
                        onEndTimeChange          = viewModel::updateEndTime,
                        onPosterToggle           = viewModel::updatePosterEnabled,
                        onPosterImagePicked      = viewModel::updatePosterImage,
                        onClubChange             = viewModel::updateClub,
                        clubOptions = clubs,
                        categoryOptions = categories,
                        onCategoryChange         = { name, id -> viewModel.updateCategory(name, id) },
                        onVisibilityTypeChange   = viewModel::updateVisibilityType,
                        onVisibilityValueChange  = viewModel::updateVisibilityValue,
                        onRegistrationTypeChange = viewModel::updateRegistrationType,
                        onRegistrationLinkChange = viewModel::updateRegistrationLink,
                        onEnableChatToggle       = viewModel::updateEnableChat,
                        onEditLocation = { showDialog = false; isSelectingLocation = true },
                        onDismiss = { wasEditMode = false; viewModel.resetForm(); showDialog = false },
                        onCreate  = { wasEditMode = false; viewModel.createEvent(createdBy = 1) },
                        onUpdate  = { wasEditMode = true; viewModel.updateEvent() }
                    )
                }

            }
        }

        // ── VIEW MODE OVERLAY ─────────────────────────────────────────────────  ← ADD HERE
        if (showViewMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
            ) {
                EventViewModeScreen(
                    events       = filteredEvents,
                    onBack       = { showViewMode = false },
                    onRegister   = { },
                    onNotify     = { },
                    onOpenDetail = { showViewMode = false },
                    showActionButtons = false,
                    categories = categories,
                    clubs = clubs,
                    eventPreviews = eventPreviews,
                    onLoadEventPreview = viewModel::loadEventPreview
                )
            }
        }
    }
}
