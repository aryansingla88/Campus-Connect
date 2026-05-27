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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.feature.events.components.*
import kotlinx.coroutines.delay

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

    // Separate flag: was the last successful operation an edit?
    // Captured before resetForm() wipes isEditMode.
    var lastOperationWasEdit by remember { mutableStateOf(false) }

    val boxWidth  = remember { mutableStateOf(0) }
    val boxHeight = remember { mutableStateOf(0) }

    // ── Success handler ───────────────────────────────────────────────────────
    // Snapshot isEditMode NOW (before resetForm clears it), close the dialog,
    // wait 2 s for the toast, then reset.
    LaunchedEffect(state.success) {
        if (state.success) {
            lastOperationWasEdit = isEditMode   // capture before reset
            showDialog = false                  // close dialog, preview stays visible
            delay(2000)
            viewModel.resetForm()
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

        // ── MARKERS ───────────────────────────────────────────────────────────
        events.forEachIndexed { index, event ->
            EventMarker(
                event    = event,
                isActive = index == activeIndex,
                onClick  = {
                    if (!isSelectingLocation) viewModel.onMarkerTapped(index)
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

        // ── SUCCESS TOAST ─────────────────────────────────────────────────────
        // Uses lastOperationWasEdit (snapshotted before resetForm) so the message
        // is always correct even after isEditMode is cleared.
        if (state.success) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    if (lastOperationWasEdit) "Event Updated Successfully"
                    else                      "Event Created Successfully",
                    color      = Color(0xFF2A2A2A),
                    fontWeight = FontWeight.Medium,
                    fontSize   = 16.sp
                )
            }
        }

        // ── PREVIEW SHEET ─────────────────────────────────────────────────────
        // Rendered whenever showPreview is true — INCLUDING when showDialog is
        // true (edit dialog sits on top). This means preview stays visible
        // behind the edit dialog and reappears when dialog is dismissed.
        if (showPreview && events.isNotEmpty() && !isSelectingLocation) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                EventPreviewSheet(
                    events        = events,
                    activeIndex   = activeIndex,
                    onPageChanged = viewModel::onPreviewPageChanged,
                    onClose       = viewModel::closePreview,
                    onEdit        = { event ->
                        viewModel.loadEventForEdit(event)
                        // Do NOT closePreview() — keep preview rendered behind dialog
                        dialogKey++
                        showDialog = true
                    },
                    onRegistration = { /* navigate */ },
                    onChat         = { /* navigate */ },
                    onAccess       = { /* navigate */ },
                    onDelete       = { event ->
                        viewModel.requestDelete(event)
                    }
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

        // ── CREATE / EDIT DIALOG ──────────────────────────────────────────────
        // Rendered on top of everything including the preview sheet.
        if (showDialog) {
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
                        // Cancel: close dialog, reset form but keep preview open
                        viewModel.resetForm()
                        showDialog = false
                        // showPreview stays true → preview reappears automatically
                    },

                    onCreate = {
                        viewModel.createEvent(createdBy = 1)
                        dialogKey++
                    },

                    onUpdate = {
                        viewModel.updateEvent()
                        // No dialogKey++ here — LaunchedEffect(state.success) closes it cleanly
                    }
                )
            }
        }
    }
}