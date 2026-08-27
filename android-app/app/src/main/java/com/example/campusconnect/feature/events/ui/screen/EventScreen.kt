package com.example.campusconnect.feature.events.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.feature.events.model.Event
import com.example.campusconnect.feature.events.model.EventStatus
import com.example.campusconnect.feature.events.ui.components.EventMarker
import com.example.campusconnect.feature.events.ui.components.ModeToggle
import com.example.campusconnect.feature.events.ui.components.ToolIcon
import com.example.campusconnect.feature.events.ui.dialog.EventAccessDialog
import com.example.campusconnect.feature.events.ui.dialog.EventCreateDialog
import com.example.campusconnect.feature.events.ui.drawer.EventHistoryDrawer
import com.example.campusconnect.feature.events.ui.drawer.EventParticipantsDrawer
import com.example.campusconnect.feature.events.ui.preview.EventPreviewSheet
import com.example.campusconnect.feature.events.viewmodel.EventViewModel
import kotlinx.coroutines.delay

private enum class ToastType { CREATED, UPDATED, DELETED }

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun EventScreen() {

    val viewModel: EventViewModel = viewModel()

    val state              by viewModel.uiState.collectAsState()
    val events             by viewModel.events.collectAsState()
    val activeIndex        by viewModel.activeEventIndex.collectAsState()
    val showPreview        by viewModel.showPreview.collectAsState()
    val isEditMode         by viewModel.isEditMode.collectAsState()
    val pendingDeleteEvent by viewModel.pendingDeleteEvent.collectAsState()

    var dialogKey           by remember { mutableStateOf(0) }
    var showDialog          by remember { mutableStateOf(false) }
    var selectedMode        by remember { mutableStateOf<String?>(null) }
    var isSelectingLocation by remember { mutableStateOf(false) }
    var wasEditMode         by remember { mutableStateOf(false) }

    var showAccessDialog    by remember { mutableStateOf(false) }
    var accessEvent         by remember { mutableStateOf<Event?>(null) }

    var showParticipants    by remember { mutableStateOf(false) }
    var showHistoryDrawer   by remember { mutableStateOf(false) }

    var showViewMode by remember { mutableStateOf(false) }

    var toastMessage        by remember { mutableStateOf<String?>(null) }

    val boxWidth  = remember { mutableStateOf(0) }
    val boxHeight = remember { mutableStateOf(0) }

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

    // Force-close history drawer when preview opens
    LaunchedEffect(showPreview) {
        if (showPreview) showHistoryDrawer = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { boxWidth.value = it.width; boxHeight.value = it.height }
            .pointerInput(isSelectingLocation) {
                if (isSelectingLocation) {
                    detectTapGestures { offset ->
                        viewModel.setScreenLocation(
                            offset.x / size.width,
                            offset.y / size.height
                        )
                        isSelectingLocation = false
                        showDialog = true
                    }
                }
            }
    ) {

        // ── BACKGROUND ────────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize())

        // ── MARKERS ───────────────────────────────────────────────────────────
        events.forEachIndexed { index, event ->
            val isPast = event.status == EventStatus.PAST
            EventMarker(
                event    = event,
                isActive = index == activeIndex && !isPast,
                onClick  = {
                    if (!isSelectingLocation && !isPast) viewModel.onMarkerTapped(index)
                },
                modifier = Modifier.offset {
                    IntOffset(
                        x = (event.xRatio * boxWidth.value).toInt(),
                        y = (event.yRatio * boxHeight.value).toInt()
                    )
                }
            )
        }

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
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModeToggle(
                    text     = "Self",
                    icon     = Icons.Default.Person,
                    selected = selectedMode == "self",
                    onClick  = { selectedMode = if (selectedMode == "self") null else "self" }
                )
                ModeToggle(
                    text     = "Shared",
                    icon     = Icons.Default.Group,
                    selected = selectedMode == "shared",
                    onClick  = { selectedMode = if (selectedMode == "shared") null else "shared" }
                )
            }

            // History nib — top-right corner, only when drawer closed AND preview not open
            if (!showHistoryDrawer && !showPreview) {
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
                            indication        = null,
                            interactionSource = remember {
                                MutableInteractionSource()
                            }
                        ) { showHistoryDrawer = true },
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

        // ── BOTTOM LEFT — View mode button ────────────────────────────────────
        if (!isSelectingLocation) {
            ToolIcon(
                icon     = Icons.Default.Visibility,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp),
                onClick  = { showViewMode = true }   // ← add this line
            )
        }

        // ── BOTTOM RIGHT — Create event button ────────────────────────────────
        if (!isSelectingLocation) {
            ToolIcon(
                icon     = Icons.Default.AddLocation,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
                onClick  = { isSelectingLocation = true }
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
        if (!isSelectingLocation && !showDialog && !showPreview) {
            Box(modifier = Modifier.fillMaxSize().zIndex(2f)) {
                EventHistoryDrawer(
                    isOpen = showHistoryDrawer,
                    onToggle = { showHistoryDrawer = !showHistoryDrawer },
                    events = events,

                    medals = viewModel.medals.collectAsState().value,

                    teams = viewModel.teams.collectAsState().value,

                    soloParticipants =
                        viewModel.soloParticipants.collectAsState().value,

                    onLoadMedals = {
                        viewModel.loadMedals(it)
                    },

                    onLoadParticipants = {
                        viewModel.loadParticipants(it)
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
                        onPosterUrlChange        = viewModel::updatePosterUrl,
                        onClubNameChange         = viewModel::updateClubName,
                        onCategoryChange         = viewModel::updateCategory,
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
                    onOpenDetail = { showViewMode = false }
                )
            }
        }
    }
}