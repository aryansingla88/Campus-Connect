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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
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
private val OrangePrimary  = Color(0xFFFF6F00)
private val OrangeLight    = Color(0xFFFFF3E0)
private val OrangeBorder   = Color(0xFFFFD6B0)
private val OrangeBg       = Color(0xFFFFF8F2)
private val ErrorRed       = Color(0xFFD32F2F)
private val ErrorBg        = Color(0xFFFFF0F0)
private val ErrorBorder    = Color(0xFFFFCDD2)
private val LabelColor     = Color(0xFF2A2A2A)
private val HintColor      = Color(0xFFAAAAAA)
private val LockedBg       = Color(0xFFF8F8F8)   // locked field background
private val LockedBorder   = Color(0xFFE8E8E8)   // locked field border
private val LockedText     = Color(0xFFBBBBBB)   // locked label/text colour

// ─── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun FieldLabel(
    text: String,
    required: Boolean = false,
    hasError: Boolean = false,
    locked: Boolean   = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 14.sp,
            color      = when {
                locked   -> LockedText
                hasError -> ErrorRed
                else     -> LabelColor
            },
            maxLines  = 1,
            overflow  = TextOverflow.Ellipsis
        )
        if (required && !locked) {
            Text(" *", color = if (hasError) ErrorRed else OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        if (locked) {
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.Lock, contentDescription = "Read-only", tint = LockedText, modifier = Modifier.size(12.dp))
        }
    }
}

/** Colors for a normal editable field. */
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

/** Colors for a locked (non-editable in edit mode) field. */
@Composable
private fun lockedFieldColors() = OutlinedTextFieldDefaults.colors(
    disabledBorderColor       = LockedBorder,
    disabledTextColor         = LockedText,
    disabledLeadingIconColor  = LockedText,
    disabledTrailingIconColor = LockedText,
    disabledPlaceholderColor  = LockedText,
    disabledLabelColor        = LockedText,
    disabledContainerColor    = LockedBg,
    unfocusedBorderColor      = LockedBorder,
    unfocusedContainerColor   = LockedBg,
    focusedBorderColor        = LockedBorder,
    focusedContainerColor     = LockedBg,
    cursorColor               = Color.Transparent
)

@Composable
private fun LabeledField(
    label: String,
    required: Boolean = false,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean   = true,
    hasError: Boolean  = false,
    locked: Boolean    = false,           // ← locked = greyed, not interactive
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FieldLabel(label, required, hasError, locked)
        OutlinedTextField(
            value         = value,
            onValueChange = if (locked) { _ -> } else onValueChange,
            enabled       = !locked && enabled,
            placeholder   = {
                Text(placeholder, color = if (locked) LockedText else HintColor, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            leadingIcon = {
                Icon(leadingIcon, null, tint = if (locked) LockedText else if (hasError) ErrorRed else HintColor, modifier = Modifier.size(20.dp))
            },
            trailingIcon = trailingIcon,
            singleLine   = true,
            shape        = RoundedCornerShape(12.dp),
            colors       = if (locked) lockedFieldColors() else fieldColors(hasError),
            modifier     = Modifier.fillMaxWidth().height(54.dp)
        )
        if (hasError) {
            Text("This field is required", color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

/** Yes / No toggle — optionally locked (greyed, not clickable). */
@Composable
private fun YesNoToggle(
    value: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    locked: Boolean    = false
) {
    val activeBg   = if (locked) LockedText   else OrangePrimary
    val inactiveBg = if (locked) LockedBg     else Color.White
    val activeText = if (locked) Color.White  else Color.White
    val inactiveText = if (locked) LockedText else Color(0xFF777777)

    Row(
        modifier = modifier
            .height(46.dp)
            .border(1.dp, if (locked) LockedBorder else OrangeBorder, RoundedCornerShape(12.dp))
    ) {
        Button(
            onClick = { if (!locked) onToggle(true) },
            modifier = Modifier.weight(1f).fillMaxHeight(),
            shape    = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = if (value) activeBg else inactiveBg),
            contentPadding = PaddingValues(0.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) { Text("Yes", color = if (value) activeText else inactiveText, fontSize = 14.sp) }
        Button(
            onClick = { if (!locked) onToggle(false) },
            modifier = Modifier.weight(1f).fillMaxHeight(),
            shape    = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = if (!value) activeBg else inactiveBg),
            contentPadding = PaddingValues(0.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) { Text("No", color = if (!value) activeText else inactiveText, fontSize = 14.sp) }
    }
}

/** Three-segment toggle — optionally locked. */
@Composable
private fun ThreeSegmentToggle(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    locked: Boolean    = false
) {
    Row(
        modifier = modifier
            .height(46.dp)
            .border(1.dp, if (locked) LockedBorder else OrangeBorder, RoundedCornerShape(12.dp))
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = selected == option
            val shape = when (index) {
                0                 -> RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                options.lastIndex -> RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                else              -> RoundedCornerShape(0.dp)
            }
            Button(
                onClick = { if (!locked) onSelect(option) },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape    = shape,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isSelected && locked -> LockedText
                        isSelected           -> OrangePrimary
                        locked               -> LockedBg
                        else                 -> Color.White
                    }
                ),
                contentPadding = PaddingValues(horizontal = 4.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    option,
                    color = when {
                        isSelected -> Color.White
                        locked     -> LockedText
                        else       -> Color(0xFF777777)
                    },
                    fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

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
    hasError: Boolean  = false,
    locked: Boolean    = false
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FieldLabel(label, required, hasError, locked)
        ExposedDropdownMenuBox(
            expanded = if (locked) false else expanded,
            onExpandedChange = { if (!locked) expanded = !expanded }
        ) {
            OutlinedTextField(
                value        = value,
                onValueChange = {},
                readOnly     = true,
                placeholder  = {
                    Text(placeholder, color = if (locked) LockedText else HintColor, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                leadingIcon = {
                    Icon(leadingIcon, null, tint = if (locked) LockedText else if (hasError) ErrorRed else HintColor, modifier = Modifier.size(20.dp))
                },
                trailingIcon = { if (!locked) ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                singleLine   = true,
                shape        = RoundedCornerShape(12.dp),
                colors       = if (locked) lockedFieldColors() else fieldColors(hasError),
                enabled      = !locked,
                modifier     = Modifier.menuAnchor().fillMaxWidth().height(54.dp)
            )
            if (!locked) {
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { opt ->
                        DropdownMenuItem(
                            text    = { Text(opt, fontSize = 14.sp) },
                            onClick = { onSelect(opt); expanded = false }
                        )
                    }
                }
            }
        }
        if (hasError) {
            Text("This field is required", color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    timePickerState: TimePickerState
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = OrangePrimary) } },
        confirmButton = { TextButton(onClick = onConfirm) { Text("OK", color = OrangePrimary) } },
        text = {
            TimePicker(
                state  = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor                         = OrangeLight,
                    clockDialSelectedContentColor          = Color.White,
                    clockDialUnselectedContentColor        = LabelColor,
                    selectorColor                          = OrangePrimary,
                    timeSelectorSelectedContainerColor     = OrangePrimary,
                    timeSelectorUnselectedContainerColor   = OrangeLight,
                    timeSelectorSelectedContentColor       = Color.White,
                    timeSelectorUnselectedContentColor     = LabelColor,
                    periodSelectorSelectedContainerColor   = OrangePrimary,
                    periodSelectorUnselectedContainerColor = OrangeLight,
                    periodSelectorSelectedContentColor     = Color.White,
                    periodSelectorUnselectedContentColor   = LabelColor,
                    periodSelectorBorderColor              = OrangeBorder
                )
            )
        },
        shape          = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}

// ─── Main Composable ──────────────────────────────────────────────────────────

/**
 * Unified create / edit dialog.
 *
 * @param isEditMode   When true the dialog shows "Update Event" / "Save Changes"
 *                     and locks the fields that cannot be changed after creation.
 * @param onCreate     Called in create mode when validation passes.
 * @param onUpdate     Called in edit mode when validation passes.
 */
@Composable
fun EventCreateDialog(
    state: EventUiState,
    isEditMode: Boolean = false,

    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onVenueChange: (String) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onPosterToggle: (Boolean) -> Unit,
    onPosterUrlChange: (String) -> Unit,
    onClubNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onVisibilityTypeChange: (String) -> Unit,
    onVisibilityValueChange: (String) -> Unit,
    onRegistrationTypeChange: (String) -> Unit,
    onRegistrationLinkChange: (String) -> Unit,
    onEnableChatToggle: (Boolean) -> Unit,
    onEditLocation: () -> Unit = {},
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
    onUpdate: () -> Unit = {},

    clubOptions: List<String>        = listOf("Tech Club", "Art Club", "Drama Club"),
    visibilityTypeOptions: List<String>  = listOf("Public", "Private", "Club"),
    visibilityValueOptions: List<String> = listOf("All", "Members Only")
) {
    // Fields locked in edit mode — shown greyed, non-interactive
    val titleLocked        = isEditMode
    val clubNameLocked     = isEditMode
    val categoryLocked     = isEditMode
    val registrationLocked = isEditMode
    val chatLocked         = isEditMode

    // ── Validation ────────────────────────────────────────────────────────────
    var submitted by remember { mutableStateOf(false) }

    val descriptionError = submitted && state.description.isBlank()
    val dateError        = submitted && state.date.isBlank()
    val venueError       = submitted && state.venue.isBlank()
    val startTimeError   = submitted && state.startTime.isBlank()
    val visibilityError  = submitted && state.visibilityType.isBlank()
    val visValueError    = submitted && state.visibilityValue.isBlank()
    val linkError        = submitted && state.registrationType == "Link" && state.registrationLink.isBlank()

    // Title / club / category only validated in create mode
    val titleError    = submitted && !isEditMode && state.title.isBlank()
    val clubNameError = submitted && !isEditMode && state.clubName.isBlank()
    val categoryError = submitted && !isEditMode && state.category.isBlank()

    fun validate(): Boolean {
        val baseOk = state.description.isNotBlank() &&
                state.date.isNotBlank() &&
                state.venue.isNotBlank() &&
                state.startTime.isNotBlank() &&
                state.visibilityType.isNotBlank() &&
                state.visibilityValue.isNotBlank() &&
                (state.registrationType != "Link" || state.registrationLink.isNotBlank())
        return if (isEditMode) baseOk
        else baseOk && state.title.isNotBlank() && state.clubName.isNotBlank() && state.category.isNotBlank()
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier  = Modifier
                .widthIn(max = 900.dp)
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.93f),
            shape     = RoundedCornerShape(24.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── HEADER ────────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(OrangeLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isEditMode) Icons.Default.Edit else Icons.Default.LocationOn,
                            contentDescription = null,
                            tint     = OrangePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (isEditMode) "Update Event" else "Create Event",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = LabelColor
                    )
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
                        label         = "Title",
                        required      = !isEditMode,
                        hasError      = titleError,
                        locked        = titleLocked,
                        value         = state.title,
                        onValueChange = onTitleChange,
                        placeholder   = "Enter event title",
                        leadingIcon   = Icons.Default.Edit,
                        modifier      = Modifier.fillMaxWidth()
                    )

                    // ── DESCRIPTION ───────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        FieldLabel("Description", required = true, hasError = descriptionError)
                        Box {
                            OutlinedTextField(
                                value         = state.description,
                                onValueChange = { if (it.length <= 200) onDescriptionChange(it) },
                                placeholder   = {
                                    Box(
                                        modifier         = Modifier.fillMaxWidth().height(100.dp),
                                        contentAlignment = Alignment.TopStart
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment     = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.MailOutline, null, tint = HintColor, modifier = Modifier.size(18.dp))
                                            Text("Enter event description", color = HintColor, fontSize = 13.sp)
                                        }
                                    }
                                },
                                shape    = RoundedCornerShape(12.dp),
                                colors   = fieldColors(descriptionError),
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                textStyle = TextStyle(fontSize = 13.sp)
                            )
                            Text(
                                "${state.description.length} / 200",
                                fontSize = 11.sp, color = HintColor,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 8.dp)
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
                        verticalAlignment     = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FieldLabel("Is Poster?")
                            YesNoToggle(value = state.isPoster, onToggle = onPosterToggle, modifier = Modifier.fillMaxWidth())
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FieldLabel("Poster Source")
                            OutlinedTextField(
                                value         = state.posterUrl,
                                onValueChange = onPosterUrlChange,
                                enabled       = state.isPoster,
                                placeholder   = {
                                    Text("Add image URL or upload", color = HintColor, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                },
                                leadingIcon = { Icon(Icons.Default.Image, null, tint = HintColor, modifier = Modifier.size(20.dp)) },
                                trailingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(if (state.isPoster) OrangePrimary else HintColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                },
                                singleLine = true,
                                shape      = RoundedCornerShape(12.dp),
                                colors     = fieldColors(),
                                modifier   = Modifier.fillMaxWidth().height(54.dp)
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
                                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                                        onDateChange("%02d/%02d/%04d".format(
                                            cal.get(java.util.Calendar.DAY_OF_MONTH),
                                            cal.get(java.util.Calendar.MONTH) + 1,
                                            cal.get(java.util.Calendar.YEAR)
                                        ))
                                    }
                                    showDatePicker = false
                                }) { Text("OK", color = OrangePrimary) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = OrangePrimary) }
                            },
                            colors = DatePickerDefaults.colors(
                                containerColor                    = Color.White,
                                todayDateBorderColor              = OrangePrimary,
                                todayContentColor                 = OrangePrimary,
                                selectedDayContainerColor         = OrangePrimary,
                                selectedDayContentColor           = Color.White,
                                selectedYearContainerColor        = OrangePrimary,
                                selectedYearContentColor          = Color.White,
                                currentYearContentColor           = OrangePrimary,
                                dayInSelectionRangeContainerColor = OrangeLight,
                                dayInSelectionRangeContentColor   = OrangePrimary
                            )
                        ) {
                            DatePicker(
                                state  = datePickerState,
                                colors = DatePickerDefaults.colors(
                                    containerColor             = Color.White,
                                    todayDateBorderColor       = OrangePrimary,
                                    todayContentColor          = OrangePrimary,
                                    selectedDayContainerColor  = OrangePrimary,
                                    selectedDayContentColor    = Color.White,
                                    selectedYearContainerColor = OrangePrimary,
                                    selectedYearContentColor   = Color.White,
                                    currentYearContentColor    = OrangePrimary
                                )
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            FieldLabel("Date", required = true, hasError = dateError)
                            OutlinedTextField(
                                value         = state.date,
                                onValueChange = {},
                                readOnly      = true,
                                placeholder   = { Text("Select date", color = HintColor, fontSize = 13.sp) },
                                leadingIcon   = { Icon(Icons.Default.DateRange, null, tint = if (dateError) ErrorRed else HintColor, modifier = Modifier.size(20.dp)) },
                                shape         = RoundedCornerShape(12.dp),
                                colors        = fieldColors(dateError),
                                modifier      = Modifier.fillMaxWidth().height(54.dp).clickable { showDatePicker = true },
                                enabled       = false
                            )
                            if (dateError) Text("This field is required", color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                        }
                        LabeledField(
                            label = "Venue", required = true, hasError = venueError,
                            value = state.venue, onValueChange = onVenueChange,
                            placeholder = "Enter venue", leadingIcon = Icons.Default.LocationOn,
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
                                val h = startTimeState.hour; val m = startTimeState.minute
                                val amPm = if (h < 12) "AM" else "PM"
                                val hr   = if (h % 12 == 0) 12 else h % 12
                                onStartTimeChange("%02d:%02d %s".format(hr, m, amPm))
                                showStartTimePicker = false
                            },
                            timePickerState = startTimeState
                        )
                    }
                    if (showEndTimePicker) {
                        TimePickerDialog(
                            onDismiss = { showEndTimePicker = false },
                            onConfirm = {
                                val h = endTimeState.hour; val m = endTimeState.minute
                                val amPm = if (h < 12) "AM" else "PM"
                                val hr   = if (h % 12 == 0) 12 else h % 12
                                onEndTimeChange("%02d:%02d %s".format(hr, m, amPm))
                                showEndTimePicker = false
                            },
                            timePickerState = endTimeState
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            FieldLabel("Start Time", required = true, hasError = startTimeError)
                            OutlinedTextField(
                                value         = state.startTime,
                                onValueChange = {},
                                readOnly      = true,
                                placeholder   = { Text("Select start time", color = HintColor, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                leadingIcon   = { Icon(Icons.Default.DateRange, null, tint = if (startTimeError) ErrorRed else HintColor, modifier = Modifier.size(20.dp)) },
                                shape         = RoundedCornerShape(12.dp),
                                colors        = fieldColors(startTimeError),
                                modifier      = Modifier.fillMaxWidth().height(54.dp).clickable { showStartTimePicker = true },
                                enabled       = false
                            )
                            if (startTimeError) Text("This field is required", color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            FieldLabel("End Time (Optional)")
                            OutlinedTextField(
                                value         = state.endTime,
                                onValueChange = {},
                                readOnly      = true,
                                placeholder   = { Text("Select end time", color = HintColor, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                leadingIcon   = { Icon(Icons.Default.DateRange, null, tint = HintColor, modifier = Modifier.size(20.dp)) },
                                shape         = RoundedCornerShape(12.dp),
                                colors        = fieldColors(),
                                modifier      = Modifier.fillMaxWidth().height(54.dp).clickable { showEndTimePicker = true },
                                enabled       = false
                            )
                        }
                    }

                    // ── CLUB NAME + CATEGORY ──────────────────────────────────
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LabeledDropdown(
                            label = "Club Name", required = !isEditMode, hasError = clubNameError,
                            locked = clubNameLocked,
                            value = state.clubName, placeholder = "Select Club",
                            options = clubOptions, onSelect = onClubNameChange,
                            leadingIcon = Icons.Default.Image, modifier = Modifier.weight(1f)
                        )
                        LabeledField(
                            label = "Category", required = !isEditMode, hasError = categoryError,
                            locked = categoryLocked,
                            value = state.category, onValueChange = onCategoryChange,
                            placeholder = "Enter category", leadingIcon = Icons.Default.List,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // ── VISIBILITY TYPE + VISIBILITY VALUE ────────────────────
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LabeledDropdown(
                            label = "Visibility", required = true, hasError = visibilityError,
                            value = state.visibilityType, placeholder = "Select Type",
                            options = visibilityTypeOptions, onSelect = onVisibilityTypeChange,
                            leadingIcon = Icons.Default.Group, modifier = Modifier.weight(1f)
                        )
                        LabeledDropdown(
                            label = "Visibility Value", required = true, hasError = visValueError,
                            value = state.visibilityValue, placeholder = "Select Value",
                            options = visibilityValueOptions, onSelect = onVisibilityValueChange,
                            leadingIcon = Icons.Default.Group, modifier = Modifier.weight(1f)
                        )
                    }

                    // ── REGISTRATION + ENABLE CHAT ────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment     = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FieldLabel("Registration", locked = registrationLocked)
                            ThreeSegmentToggle(
                                selected = state.registrationType,
                                options  = listOf("In-App", "Link", "No"),
                                onSelect = onRegistrationTypeChange,
                                locked   = registrationLocked,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FieldLabel("Enable Chat?", locked = chatLocked)
                            YesNoToggle(
                                value    = state.enableChat,
                                onToggle = onEnableChatToggle,
                                locked   = chatLocked,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // ── REGISTRATION LINK ─────────────────────────────────────
                    if (state.registrationType == "Link") {
                        LabeledField(
                            label         = "Registration Link",
                            required      = true,
                            hasError      = linkError,
                            locked        = registrationLocked,
                            value         = state.registrationLink,
                            onValueChange = onRegistrationLinkChange,
                            placeholder   = "Enter registration link",
                            leadingIcon   = Icons.Default.Share,
                            modifier      = Modifier.fillMaxWidth()
                        )
                    }

                    // Hint row in edit mode
                    if (isEditMode) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(Icons.Default.Lock, null, tint = LockedText, modifier = Modifier.size(13.dp))
                            Text(
                                "Greyed fields cannot be changed after creation",
                                fontSize = 11.sp,
                                color    = LockedText
                            )
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
                        onClick         = onDismiss,
                        modifier        = Modifier.weight(1f).height(50.dp),
                        shape           = RoundedCornerShape(14.dp),
                        border          = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
                        colors          = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                        contentPadding  = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Cancel",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 14.sp,
                            maxLines   = 1
                        )
                    }
                    Button(
                        onClick = {
                            submitted = true
                            if (validate()) {
                                if (isEditMode) onUpdate() else onCreate()
                            }
                        },
                        modifier        = Modifier.weight(1f).height(50.dp),
                        shape           = RoundedCornerShape(14.dp),
                        colors          = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        contentPadding  = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(
                            if (isEditMode) Icons.Default.Check else Icons.Default.DateRange,
                            null,
                            tint     = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (isEditMode) "Save Changes" else "Create Event",
                            color      = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 14.sp,
                            maxLines   = 1
                        )
                    }
                }
            }
        }
    }
}