@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.campusconnect.feature.events.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.events.EventUiState

// ─── Theme ───────────────────────────────────────────────────────────────────
private val OrangePrimary = Color(0xFFFF6F00)
private val OrangeLight   = Color(0xFFFFF3E0)
private val OrangeBorder  = Color(0xFFFFD6B0)
private val OrangeBg      = Color(0xFFFFF8F2)
private val ErrorRed      = Color(0xFFD32F2F)
private val ErrorBg       = Color(0xFFFFF0F0)
private val ErrorBorder   = Color(0xFFFFCDD2)
private val LabelColor    = Color(0xFF2A2A2A)
private val HintColor     = Color(0xFFAAAAAA)

// ─── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun FieldLabel(text: String, required: Boolean = false, hasError: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = if (hasError) ErrorRed else LabelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (required) {
            Text(" *", color = if (hasError) ErrorRed else OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun fieldColors(hasError: Boolean = false) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor        = if (hasError) ErrorRed else OrangePrimary,
    unfocusedBorderColor      = if (hasError) ErrorBorder else OrangeBorder,
    focusedLabelColor         = if (hasError) ErrorRed else OrangePrimary,
    cursorColor               = if (hasError) ErrorRed else OrangePrimary,
    focusedContainerColor     = if (hasError) ErrorBg else Color.Transparent,
    unfocusedContainerColor   = if (hasError) ErrorBg else Color.Transparent,
    disabledBorderColor       = if (hasError) ErrorBorder else OrangeBorder,
    disabledTextColor         = LabelColor,
    disabledLeadingIconColor  = HintColor,
    disabledTrailingIconColor = HintColor,
    disabledPlaceholderColor  = HintColor,
    disabledLabelColor        = HintColor,
    disabledContainerColor    = if (hasError) ErrorBg else Color.Transparent
)

/** Single-line field with label above. */
@Composable
private fun LabeledField(
    label: String,
    required: Boolean = false,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hasError: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FieldLabel(label, required, hasError)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            placeholder = {
                Text(
                    placeholder,
                    color = HintColor,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(leadingIcon, contentDescription = null, tint = if (hasError) ErrorRed else HintColor, modifier = Modifier.size(20.dp))
            },
            trailingIcon = trailingIcon,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors(hasError),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        )
        if (hasError) {
            Text(
                "This field is required",
                color = ErrorRed,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/** Yes / No segmented toggle */
@Composable
private fun YesNoToggle(
    value: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(46.dp)
            .border(1.dp, OrangeBorder, RoundedCornerShape(12.dp))
    ) {
        Button(
            onClick = { onToggle(true) },
            modifier = Modifier.weight(1f).fillMaxHeight(),
            shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (value) OrangePrimary else Color.White
            ),
            contentPadding = PaddingValues(0.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text("Yes", color = if (value) Color.White else Color(0xFF777777), fontSize = 14.sp)
        }
        Button(
            onClick = { onToggle(false) },
            modifier = Modifier.weight(1f).fillMaxHeight(),
            shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!value) OrangePrimary else Color.White
            ),
            contentPadding = PaddingValues(0.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text("No", color = if (!value) Color.White else Color(0xFF777777), fontSize = 14.sp)
        }
    }
}

/** Dropdown with label above. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabeledDropdown(
    label: String,
    required: Boolean = false,
    value: String,
    placeholder: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    hasError: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FieldLabel(label, required, hasError)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text(
                        placeholder,
                        color = HintColor,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(leadingIcon, contentDescription = null, tint = if (hasError) ErrorRed else HintColor, modifier = Modifier.size(20.dp))
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(hasError),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .height(54.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt, fontSize = 14.sp) },
                        onClick = { onSelect(opt); expanded = false }
                    )
                }
            }
        }
        if (hasError) {
            Text(
                "This field is required",
                color = ErrorRed,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// ─── TimePickerDialog helper ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    timePickerState: TimePickerState
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = OrangePrimary) }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("OK", color = OrangePrimary) }
        },
        text = {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor                      = OrangeLight,
                    clockDialSelectedContentColor       = Color.White,
                    clockDialUnselectedContentColor     = LabelColor,
                    selectorColor                       = OrangePrimary,
                    timeSelectorSelectedContainerColor   = OrangePrimary,
                    timeSelectorUnselectedContainerColor = OrangeLight,
                    timeSelectorSelectedContentColor     = Color.White,
                    timeSelectorUnselectedContentColor   = LabelColor,
                    periodSelectorSelectedContainerColor   = OrangePrimary,
                    periodSelectorUnselectedContainerColor = OrangeLight,
                    periodSelectorSelectedContentColor     = Color.White,
                    periodSelectorUnselectedContentColor   = LabelColor,
                    periodSelectorBorderColor              = OrangeBorder
                )
            )
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}

// ─── Main dialog ──────────────────────────────────────────────────────────────

@Composable
fun EventCreateDialog(
    state: EventUiState,

    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,

    onDateChange: (String) -> Unit,
    onVenueChange: (String) -> Unit,

    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,

    onPosterToggle: (Boolean) -> Unit,
    onPosterUrlChange: (String) -> Unit,

    onDismiss: () -> Unit,
    onCreate: () -> Unit,

    onClubNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,

    onVisibilityTypeChange: (String) -> Unit,
    onVisibilityValueChange: (String) -> Unit,

    onRegistrationToggle: (Boolean) -> Unit,
    onRegistrationLinkChange: (String) -> Unit,
    onInAppRegistrationToggle: () -> Unit,

    onEnableChatToggle: (Boolean) -> Unit,

    onEditLocation: () -> Unit = {},

    clubOptions: List<String> = listOf("Tech Club", "Art Club", "Drama Club"),
    visibilityTypeOptions: List<String> = listOf("Public", "Private", "Club"),
    visibilityValueOptions: List<String> = listOf("All", "Members Only")
) {
    // ── Validation state ──────────────────────────────────────────────────────
    // Tracks whether the user has attempted to submit; only show errors after that
    var submitted by remember { mutableStateOf(false) }

    // Derived error flags — only active after first submit attempt
    val titleError        = submitted && state.title.isBlank()
    val descriptionError  = submitted && state.description.isBlank()
    val dateError         = submitted && state.date.isBlank()
    val venueError        = submitted && state.venue.isBlank()
    val startTimeError    = submitted && state.startTime.isBlank()
    val clubNameError     = submitted && state.clubName.isBlank()
    val categoryError     = submitted && state.category.isBlank()
    val visibilityError   = submitted && state.visibilityType.isBlank()
    val visValueError     = submitted && state.visibilityValue.isBlank()

    fun validate(): Boolean =
        state.title.isNotBlank() &&
                state.description.isNotBlank() &&
                state.date.isNotBlank() &&
                state.venue.isNotBlank() &&
                state.startTime.isNotBlank() &&
                state.clubName.isNotBlank() &&
                state.category.isNotBlank() &&
                state.visibilityType.isNotBlank() &&
                state.visibilityValue.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 900.dp)
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.93f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── HEADER ────────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(OrangeLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = OrangePrimary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("Create Event", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = LabelColor)
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // ── SCROLLABLE BODY ───────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ── TITLE ─────────────────────────────────────────────────
                    LabeledField(
                        label = "Title", required = true, hasError = titleError,
                        value = state.title, onValueChange = onTitleChange,
                        placeholder = "Enter event title",
                        leadingIcon = Icons.Default.Edit,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // ── DESCRIPTION ───────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        FieldLabel("Description", required = true, hasError = descriptionError)
                        Box {
                            OutlinedTextField(
                                value = state.description,
                                onValueChange = { if (it.length <= 200) onDescriptionChange(it) },
                                placeholder = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp),
                                        contentAlignment = Alignment.TopStart
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.MailOutline, null, tint = HintColor, modifier = Modifier.size(18.dp))
                                            Text("Enter event description", color = HintColor, fontSize = 13.sp)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors(descriptionError),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                textStyle = TextStyle(fontSize = 13.sp)
                            )
                            Text(
                                text = "${state.description.length} / 200",
                                fontSize = 11.sp,
                                color = HintColor,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 12.dp, bottom = 8.dp)
                            )
                        }
                        if (descriptionError) {
                            Text("This field is required", color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                        }
                    }

                    // ── IS POSTER? + POSTER SOURCE ────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FieldLabel("Is Poster?")
                            YesNoToggle(value = state.isPoster, onToggle = onPosterToggle, modifier = Modifier.fillMaxWidth())
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FieldLabel("Poster Source")
                            OutlinedTextField(
                                value = state.posterUrl,
                                onValueChange = onPosterUrlChange,
                                enabled = state.isPoster,
                                placeholder = {
                                    Text("Add image URL or upload", color = HintColor, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Image, null, tint = HintColor, modifier = Modifier.size(20.dp))
                                },
                                trailingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(if (state.isPoster) OrangePrimary else HintColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Add, "Upload", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors(),
                                modifier = Modifier.fillMaxWidth().height(54.dp)
                            )
                        }
                    }

                    // ── LOCATION CARD ─────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OrangeBg, RoundedCornerShape(14.dp))
                            .border(1.dp, OrangeBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = OrangePrimary, modifier = Modifier.size(26.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Location (From Tap)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = LabelColor)
                            val lat  = state.selectedRatio?.first  ?: 0.0
                            val long = state.selectedRatio?.second ?: 0.0
                            Text("Lat: $lat, Long: $long", fontSize = 12.sp, color = HintColor)
                        }
                        TextButton(onClick = onEditLocation, contentPadding = PaddingValues(horizontal = 8.dp)) {
                            Icon(Icons.Default.Edit, "Edit", tint = OrangePrimary, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit", color = OrangePrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }

                    // ── DATE + VENUE ──────────────────────────────────────────
                    var showDatePicker by remember { mutableStateOf(false) }
                    val datePickerState = rememberDatePickerState()

                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val calendar = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                                        val formatted = "%02d/%02d/%04d".format(
                                            calendar.get(java.util.Calendar.DAY_OF_MONTH),
                                            calendar.get(java.util.Calendar.MONTH) + 1,
                                            calendar.get(java.util.Calendar.YEAR)
                                        )
                                        onDateChange(formatted)
                                    }
                                    showDatePicker = false
                                }) { Text("OK", color = OrangePrimary) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = OrangePrimary) }
                            },
                            colors = DatePickerDefaults.colors(
                                containerColor            = Color.White,
                                todayDateBorderColor      = OrangePrimary,
                                todayContentColor         = OrangePrimary,
                                selectedDayContainerColor = OrangePrimary,
                                selectedDayContentColor   = Color.White,
                                selectedYearContainerColor = OrangePrimary,
                                selectedYearContentColor  = Color.White,
                                currentYearContentColor   = OrangePrimary,
                                dayInSelectionRangeContainerColor = OrangeLight,
                                dayInSelectionRangeContentColor   = OrangePrimary
                            )
                        ) {
                            DatePicker(
                                state = datePickerState,
                                colors = DatePickerDefaults.colors(
                                    containerColor            = Color.White,
                                    todayDateBorderColor      = OrangePrimary,
                                    todayContentColor         = OrangePrimary,
                                    selectedDayContainerColor = OrangePrimary,
                                    selectedDayContentColor   = Color.White,
                                    selectedYearContainerColor = OrangePrimary,
                                    selectedYearContentColor  = Color.White,
                                    currentYearContentColor   = OrangePrimary
                                )
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            FieldLabel("Date", required = true, hasError = dateError)
                            OutlinedTextField(
                                value = state.date,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Select date", color = HintColor, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.DateRange, null, tint = if (dateError) ErrorRed else HintColor, modifier = Modifier.size(20.dp))
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors(dateError),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .clickable { showDatePicker = true },
                                enabled = false
                            )
                            if (dateError) {
                                Text("This field is required", color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                        LabeledField(
                            label = "Venue", required = true, hasError = venueError,
                            value = state.venue, onValueChange = onVenueChange,
                            placeholder = "Enter venue",
                            leadingIcon = Icons.Default.LocationOn,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // ── START TIME + END TIME ─────────────────────────────────
                    var showStartTimePicker by remember { mutableStateOf(false) }
                    var showEndTimePicker   by remember { mutableStateOf(false) }
                    val startTimeState = rememberTimePickerState(is24Hour = false)
                    val endTimeState   = rememberTimePickerState(is24Hour = false)

                    if (showStartTimePicker) {
                        TimePickerDialog(
                            onDismiss = { showStartTimePicker = false },
                            onConfirm = {
                                val hour = startTimeState.hour; val minute = startTimeState.minute
                                val amPm = if (hour < 12) "AM" else "PM"
                                val h    = if (hour % 12 == 0) 12 else hour % 12
                                onStartTimeChange("%02d:%02d %s".format(h, minute, amPm))
                                showStartTimePicker = false
                            },
                            timePickerState = startTimeState
                        )
                    }
                    if (showEndTimePicker) {
                        TimePickerDialog(
                            onDismiss = { showEndTimePicker = false },
                            onConfirm = {
                                val hour = endTimeState.hour; val minute = endTimeState.minute
                                val amPm = if (hour < 12) "AM" else "PM"
                                val h    = if (hour % 12 == 0) 12 else hour % 12
                                onEndTimeChange("%02d:%02d %s".format(h, minute, amPm))
                                showEndTimePicker = false
                            },
                            timePickerState = endTimeState
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            FieldLabel("Start Time", required = true, hasError = startTimeError)
                            OutlinedTextField(
                                value = state.startTime,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Select start time", color = HintColor, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                leadingIcon = {
                                    Icon(Icons.Default.DateRange, null, tint = if (startTimeError) ErrorRed else HintColor, modifier = Modifier.size(20.dp))
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors(startTimeError),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .clickable { showStartTimePicker = true },
                                enabled = false
                            )
                            if (startTimeError) {
                                Text("This field is required", color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            FieldLabel("End Time (Optional)")
                            OutlinedTextField(
                                value = state.endTime,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Select end time", color = HintColor, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                leadingIcon = {
                                    Icon(Icons.Default.DateRange, null, tint = HintColor, modifier = Modifier.size(20.dp))
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .clickable { showEndTimePicker = true },
                                enabled = false
                            )
                        }
                    }

                    // ── CLUB NAME + CATEGORY ──────────────────────────────────
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LabeledDropdown(
                            label = "Club Name", required = true, hasError = clubNameError,
                            value = state.clubName,
                            placeholder = "Select Club",
                            options = clubOptions,
                            onSelect = onClubNameChange,
                            leadingIcon = Icons.Default.Image,
                            modifier = Modifier.weight(1f)
                        )
                        LabeledField(
                            label = "Category", required = true, hasError = categoryError,
                            value = state.category, onValueChange = onCategoryChange,
                            placeholder = "Enter category",
                            leadingIcon = Icons.Default.List,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // ── VISIBILITY TYPE + VISIBILITY VALUE ────────────────────
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LabeledDropdown(
                            label = "Visibility", required = true, hasError = visibilityError,
                            value = state.visibilityType,
                            placeholder = "Select Type",
                            options = visibilityTypeOptions,
                            onSelect = onVisibilityTypeChange,
                            leadingIcon = Icons.Default.Group,
                            modifier = Modifier.weight(1f)
                        )
                        LabeledDropdown(
                            label = "Visibility Value", required = true, hasError = visValueError,
                            value = state.visibilityValue,
                            placeholder = "Select Value",
                            options = visibilityValueOptions,
                            onSelect = onVisibilityValueChange,
                            leadingIcon = Icons.Default.Group,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // ── REGISTRATION REQUIRED (left) + ENABLE CHAT (right) ────
                    // These two toggles always appear together in one row.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FieldLabel("Registration Required?")
                            YesNoToggle(
                                value = state.registrationRequired,
                                onToggle = onRegistrationToggle,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FieldLabel("Enable Chat?")
                            YesNoToggle(
                                value = state.enableChat,
                                onToggle = onEnableChatToggle,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // ── IN-APP REGISTRATION ROW — only when registration is Yes ──
                    if (state.registrationRequired) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FieldLabel("In-App Registration")
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onInAppRegistrationToggle,
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size(54.dp)   // matches field height
                                ) {
                                    Icon(Icons.Default.Add, "Add", modifier = Modifier.size(20.dp))
                                }
                                OutlinedTextField(
                                    value = state.registrationLink,
                                    onValueChange = onRegistrationLinkChange,
                                    placeholder = {
                                        Text("Add registration link (optional)", color = HintColor, fontSize = 11.sp)
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Share, null, tint = HintColor, modifier = Modifier.size(18.dp))
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = fieldColors(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                }

                // ── BOTTOM BUTTONS ────────────────────────────────────────────
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cancel", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }

                    Button(
                        onClick = {
                            submitted = true         // arm the error flags
                            if (validate()) onCreate()  // only proceed if all required fields filled
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Icon(Icons.Default.DateRange, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Create Event", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}