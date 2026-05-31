package com.example.campusconnect.feature.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.feature.events.components.EventAccessDialog
import com.example.campusconnect.feature.events.components.EventCreateDialog
import com.example.campusconnect.feature.events.components.EventHistoryDrawer
import com.example.campusconnect.feature.events.components.EventMarker
import com.example.campusconnect.feature.events.components.EventParticipantsDrawer
import com.example.campusconnect.feature.events.components.EventPreviewSheet
import com.example.campusconnect.feature.events.components.ModeToggle
import com.example.campusconnect.feature.events.components.ToolIcon
import com.example.campusconnect.model.EventStatus
import kotlinx.coroutines.delay

// ─── Toast message type ───────────────────────────────────────────────────────
private enum class ToastType { CREATED, UPDATED, DELETED }

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

    // Tracks whether the dialog was last opened in edit mode.
    // Set at the CALL SITE so it is never corrupted by recomposition timing.
    var wasEditMode by remember { mutableStateOf(false) }

    // ── Access dialog state ───────────────────────────────────────────────────
    var showAccessDialog by remember { mutableStateOf(false) }
    var accessEvent      by remember { mutableStateOf<com.example.campusconnect.model.Event?>(null) }

    // ── Participants drawer state ─────────────────────────────────────────────
    var showParticipants by remember { mutableStateOf(false) }

    // ── History drawer state ──────────────────────────────────────────────────
    var showHistoryDrawer by remember { mutableStateOf(false) }

    // ── Toast state ───────────────────────────────────────────────────────────
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // ── Preview always shows only LIVE events ─────────────────────────────────
    val filteredEvents = remember(events) {
        events.filter { it.status != EventStatus.PAST }
    }

    // Remap activeIndex into the filtered list
    val filteredActiveIndex = remember(activeIndex, filteredEvents, events) {
        val activeEvent = events.getOrNull(activeIndex)
        if (activeEvent != null) filteredEvents.indexOf(activeEvent).coerceAtLeast(0)
        else 0
    }

    val boxWidth  = remember { mutableStateOf(0) }
    val boxHeight = remember { mutableStateOf(0) }

    // ── Show toast for 2 s then clear ────────────────────────────────────────
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2000)
            toastMessage = null
        }
    }

    // ── Handle create / update success ───────────────────────────────────────
    LaunchedEffect(state.success) {
        if (state.success) {
            showDialog = false
            viewModel.resetForm()
            toastMessage = if (wasEditMode) "Event Updated Successfully"
            else             "Event Created Successfully"
        }
    }

    // ── Handle delete success ─────────────────────────────────────────────────
    val deleteSuccess by viewModel.deleteSuccess.collectAsState()
    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            toastMessage = "Event Deleted Successfully"
            viewModel.clearDeleteSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged {
                boxWidth.value  = it.width
                boxHeight.value = it.height
            }
            .pointerInput(isSelectingLocation) {
                if (isSelectingLocation) {
                    detectTapGestures { offset ->
                        val xRatio = offset.x / size.width
                        val yRatio = offset.y / size.height
                        viewModel.setScreenLocation(xRatio, yRatio)
                        isSelectingLocation = false
                        showDialog = true
                    }
                }
            }
    ) {

        // ── BACKGROUND ────────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize())

        // ── MARKERS — past events grey + unclickable ──────────────────────────
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

        // ── NORMAL UI ─────────────────────────────────────────────────────────
        if (!isSelectingLocation) {

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

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ToolIcon(Icons.Default.Settings)
                ToolIcon(Icons.Default.Visibility)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ToolIcon(Icons.Default.Notifications)
                ToolIcon(Icons.Default.GroupAdd)
                ToolIcon(Icons.Default.Delete)
            }

            ToolIcon(
                icon     = Icons.Default.AddLocation,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick  = { isSelectingLocation = true }
            )

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ToolIcon(Icons.Default.PersonAdd)
                ToolIcon(Icons.Default.Group)
            }

            ToolIcon(
                icon     = Icons.Default.Chat,
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
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
                        // Map filtered index back to global index
                        val globalEvent = filteredEvents.getOrNull(index)
                        val globalIndex = if (globalEvent != null) events.indexOf(globalEvent) else index
                        viewModel.onPreviewPageChanged(globalIndex)
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
                    onRegistration = { /* navigate */ },
                    onChat         = { /* navigate */ },
                    onAccess       = { event ->
                        accessEvent      = event
                        showAccessDialog = true
                    },
                    onDelete       = { event ->
                        viewModel.requestDelete(event)
                    }
                )
            }
        }

        // ── PARTICIPANTS DRAWER ───────────────────────────────────────────────
        if (showPreview && filteredEvents.isNotEmpty() && !isSelectingLocation && !showDialog) {
            val currentEvent = filteredEvents.getOrNull(filteredActiveIndex)
            if (currentEvent != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(2f)
                ) {
                    EventParticipantsDrawer(
                        event    = currentEvent,
                        isOpen   = showParticipants,
                        onToggle = { showParticipants = !showParticipants }
                    )
                }
            }
        }

        // ── HISTORY DRAWER ────────────────────────────────────────────────────
        // Hidden when preview is open, selecting location, or dialog is open.
        // Nib disappears when drawer is open (close only via X button inside).
        if (!isSelectingLocation && !showDialog && !showPreview) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f)
            ) {
                EventHistoryDrawer(
                    isOpen   = showHistoryDrawer,
                    onToggle = { showHistoryDrawer = !showHistoryDrawer }
                )
            }
        }

        // Force-close history drawer when preview opens
        LaunchedEffect(showPreview) {
            if (showPreview) showHistoryDrawer = false
        }

        // ── TOAST ─────────────────────────────────────────────────────────────
        // zIndex(3f) — floats above both drawers and preview sheet.
        toastMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(3f)
                    .padding(bottom = 320.dp)
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text       = msg,
                    color      = Color(0xFF2A2A2A),
                    fontWeight = FontWeight.Medium,
                    fontSize   = 16.sp
                )
            }
        }

        // ── DELETE CONFIRMATION ───────────────────────────────────────────────
        pendingDeleteEvent?.let { event ->
            AlertDialog(
                onDismissRequest = viewModel::cancelDelete,
                shape            = RoundedCornerShape(20.dp),
                containerColor   = Color.White,
                title = {
                    Text(
                        "Delete Event?",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = Color(0xFF2A2A2A)
                    )
                },
                text = {
                    Text(
                        "Are you sure you want to delete \"${event.title}\"? This cannot be undone.",
                        fontSize   = 14.sp,
                        color      = Color(0xFF555555),
                        lineHeight = 20.sp
                    )
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = viewModel::cancelDelete,
                        shape   = RoundedCornerShape(12.dp),
                        border  = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
                        colors  = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6F00))
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = viewModel::confirmDelete,
                        shape   = RoundedCornerShape(12.dp),
                        colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Delete", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        // ── ACCESS DIALOG ─────────────────────────────────────────────────────
        if (showAccessDialog && accessEvent != null) {
            EventAccessDialog(
                event     = accessEvent!!,
                onDismiss = {
                    showAccessDialog = false
                    accessEvent      = null
                }
            )
        }

        // ── CREATE / EDIT DIALOG — highest zIndex, on top of everything ───────
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

                        onEditLocation = {
                            showDialog = false
                            isSelectingLocation = true
                        },

                        onDismiss = {
                            wasEditMode = false
                            viewModel.resetForm()
                            showDialog = false
                        },

                        onCreate = {
                            wasEditMode = false
                            viewModel.createEvent(createdBy = 1)
                        },

                        onUpdate = {
                            wasEditMode = true
                            viewModel.updateEvent()
                        }
                    )
                }
            }
        }
    }
}