package com.example.campusconnect.feature.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background

import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.offset

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import com.example.campusconnect.feature.events.components.ToolIcon
import com.example.campusconnect.feature.events.components.ModeToggle
import com.example.campusconnect.feature.events.components.EventCreateDialog
import com.example.campusconnect.feature.events.components.EventMarker
import com.example.campusconnect.feature.events.components.EventPreviewSheet

import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Composable
fun EventScreen() {

    val viewModel: EventViewModel = viewModel()

    val state           by viewModel.uiState.collectAsState()
    val events          by viewModel.events.collectAsState()
    val activeIndex     by viewModel.activeEventIndex.collectAsState()
    val showPreview     by viewModel.showPreview.collectAsState()

    var dialogKey           by remember { mutableStateOf(0) }
    var showDialog          by remember { mutableStateOf(false) }
    var selectedMode        by remember { mutableStateOf<String?>(null) }
    var isSelectingLocation by remember { mutableStateOf(false) }

    val boxWidth  = remember { mutableStateOf(0) }
    val boxHeight = remember { mutableStateOf(0) }

    LaunchedEffect(state.success) {
        if (state.success) showDialog = false
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
                    if (!isSelectingLocation) {
                        viewModel.onMarkerTapped(index)
                    }
                },
                modifier = Modifier.offset {
                    IntOffset(
                        x = (event.xRatio * boxWidth.value).toInt(),
                        y = (event.yRatio * boxHeight.value).toInt()
                    )
                }
            )
        }

        // ── TAP ANYWHERE POPUP ────────────────────────────────────────────────
        if (isSelectingLocation) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .background(Color(0xFFFFF3E0), shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text       = "Tap anywhere to place event",
                    color      = Color(0xFF2A2A2A),
                    fontWeight = FontWeight.Medium,
                    fontSize   = 16.sp
                )
            }
        }

        // ── NORMAL UI ─────────────────────────────────────────────────────────
        if (!isSelectingLocation) {

            // TOP CENTER — Self / Shared toggle
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

            // TOP RIGHT
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ToolIcon(Icons.Default.Settings)
                ToolIcon(Icons.Default.Visibility)
            }

            // RIGHT SIDE
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

            // ➕ ADD EVENT BUTTON
            ToolIcon(
                icon     = Icons.Default.AddLocation,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick  = { isSelectingLocation = true }
            )

            // LEFT SIDE
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ToolIcon(Icons.Default.PersonAdd)
                ToolIcon(Icons.Default.Group)
            }

            // CHAT
            ToolIcon(
                icon     = Icons.Default.Chat,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }

        // ── SUCCESS POPUP ─────────────────────────────────────────────────────
        if (state.success) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .background(Color(0xFFFFF3E0), shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text       = "Event Created Successfully",
                    color      = Color(0xFF2A2A2A),
                    fontWeight = FontWeight.Medium,
                    fontSize   = 16.sp
                )
            }
            LaunchedEffect(Unit) {
                delay(2000)
                viewModel.resetForm()
            }
        }

        // ── PREVIEW SHEET ─────────────────────────────────────────────────────
        if (showPreview && events.isNotEmpty() && !showDialog && !isSelectingLocation) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                EventPreviewSheet(
                    events        = events,
                    activeIndex   = activeIndex,          // drives auto-scroll
                    onPageChanged = viewModel::onPreviewPageChanged,
                    onClose       = viewModel::closePreview,
                    onEdit        = {
                        viewModel.closePreview()
                        showDialog = true
                    },
                    onRegistration = { /* navigate to registration screen */ },
                    onChat         = { /* navigate to chat screen */ },
                    onAccess       = { /* navigate to access screen */ },
                    onDelete       = { /* show confirmation dialog */ }
                )
            }
        }

        // ── CREATE DIALOG ─────────────────────────────────────────────────────
        if (showDialog) {
            key(dialogKey) {
                EventCreateDialog(
                    state = state,

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
                        viewModel.resetForm()
                        showDialog = false
                    },

                    onCreate = {
                        viewModel.createEvent(createdBy = 1)
                        dialogKey++
                    }
                )
            }
        }
    }
}