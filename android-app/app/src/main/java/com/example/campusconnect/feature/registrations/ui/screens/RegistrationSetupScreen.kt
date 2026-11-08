package com.example.campusconnect.feature.registrations.ui.screens
import com.example.campusconnect.feature.registrations.ui.components.*
import com.example.campusconnect.feature.registrations.ui.components.setup.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.registrations.model.RegistrationSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import java.util.Calendar
import java.util.TimeZone
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


// ── Extra colors (only used on this screen) ───────────────────────────────────

private val BlueTile   = Color(0xFFE3F2FD)
private val BlueIcon   = Color(0xFF1E88E5)
private val PurpleTile = Color(0xFFEDE7F6)
private val PurpleIcon = Color(0xFF7E57C2)
private val ErrorRed   = Color(0xFFE53935)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationSetupScreen(
    eventName: String,
    isPublished: Boolean,
    initialSettings: RegistrationSettings,
    onBack: () -> Unit,
    onContinue: (RegistrationSettings) -> Unit,
    onPublish: (RegistrationSettings) -> Unit = {},
    isLoading: Boolean = false,
    isSaving: Boolean = false,
    publishError: String? = null,
    loadFailed: Boolean = false,
    publishSucceeded: Boolean = false,
    onRetryLoad: () -> Unit = {},
    onTeamQuestions: (RegistrationSettings) -> Unit = {},
    onViewResponses: () -> Unit = {},
    onSettingsChange: (RegistrationSettings) -> Unit = {},
) {
    var settings by remember(initialSettings) { mutableStateOf(initialSettings) }

    fun changeSettings(updated: RegistrationSettings) {
        settings = updated
        onSettingsChange(updated)
    }

    val hasType = settings.allowIndividual || settings.allowTeam

    var showDeadlinePicker by remember { mutableStateOf(false) }
    var showDeadlineTimePicker by remember { mutableStateOf(false) }
    var selectedDeadlineDateMillis by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        containerColor = PageBg,

        topBar = {
            RegistrationHeader(
                eventName = eventName,
                isPublished = isPublished,
                onBack = onBack,
                action = {
                    HeaderActionButton(
                        text = if (isSaving) "Saving…" else "Publish",
                        icon = Icons.Outlined.Send,
                        onClick = { onPublish(settings) },
                        enabled = !isLoading && !isSaving && !loadFailed && hasType,
                    )
                }
            )
        },
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),

            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            if (publishError != null) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(publishError, modifier = Modifier.weight(1f), color = ErrorRed, fontSize = 13.sp)
                        if (loadFailed) TextButton(onClick = onRetryLoad) { Text("Retry") }
                    }
                }
            } else if (publishSucceeded) {
                Text(
                    "Registration form published successfully. All settings and questions have been saved.",
                    color = GreenText,
                    fontSize = 13.sp,
                )
            }

            // Intro
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Configure registration",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Registration type
            Column(
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SectionLabel("Registration type")

                    Text(
                        "Required",
                        fontSize = 11.sp,
                        color = OrangePrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(OrangeLight)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                RegistrationTypeCard(
                    icon = Icons.Outlined.Person,
                    title = "Individual registration",
                    subtitle = "Participants register on their own.",
                    checked = settings.allowIndividual,
                    onChecked = { enabled ->
                        changeSettings(if (enabled) {
                            if (settings.allowTeam) {
                                // individual + team: (1, max > 1)
                                settings.copy(minMembers = 1, maxMembers = maxOf(settings.maxMembers, 2))
                            } else {
                                // individual-only: (1, 1)
                                settings.copy(minMembers = 1, maxMembers = 1)
                            }
                        } else {
                            // team-only: (min >= 2, max >= min)
                            val newMin = maxOf(settings.minMembers, 2)
                            settings.copy(
                                minMembers = newMin,
                                maxMembers = maxOf(settings.maxMembers, newMin),
                            )
                        })
                    }
                )

                RegistrationTypeCard(
                    icon = Icons.Outlined.Groups,
                    title = "Team registration",
                    subtitle = "Participants register together as a team.",
                    checked = settings.allowTeam,
                    onChecked = { enabled ->
                        changeSettings(if (enabled) {
                            if (settings.allowIndividual) {
                                // individual + team: (1, max > 1)
                                settings.copy(minMembers = 1, maxMembers = maxOf(settings.maxMembers, 2))
                            } else {
                                // team-only: (min >= 2, max >= min)
                                val newMin = maxOf(settings.minMembers, 2)
                                settings.copy(
                                    minMembers = newMin,
                                    maxMembers = maxOf(settings.maxMembers, newMin),
                                )
                            }
                        } else {
                            // individual-only: (1, 1)
                            settings.copy(minMembers = 1, maxMembers = 1)
                        })
                    }
                )

                AnimatedVisibility(visible = !hasType) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(16.dp)
                        )

                        Text(
                            "Select at least one registration type.",
                            fontSize = 12.sp,
                            color = ErrorRed
                        )
                    }
                }
            }

            // Team configuration
            AnimatedVisibility(visible = settings.allowTeam) {
                TeamSettingsCard(
                    settings = settings,
                    onSettingsChange = ::changeSettings
                )
            }

            // Registration deadline — keep it before the divider that separates the form section.
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(OrangeLight),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Registration deadline",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                            )
                            Text(
                                text = settings.deadline?.let {
                                    SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault()).format(Date(it))
                                } ?: "No deadline set",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (settings.deadline != null) TextPrimary else TextSecondary,
                            )
                        }
                        if (settings.deadline != null) {
                            IconButton(
                                onClick = { changeSettings(settings.copy(deadline = null)) },
                                modifier = Modifier.size(34.dp),
                            ) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = "Remove deadline",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = { showDeadlinePicker = true },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(11.dp),
                        border = BorderStroke(1.dp, OrangeBorder),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                    ) {
                        Icon(Icons.Outlined.EditCalendar, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (settings.deadline == null) "Set date & time" else "Change date & time", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE0E0E0),
            )

            // Form configuration
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionLabel("Registration form")

                Text(
                    "Choose what information participants need to provide.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )


                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBg
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {

                        SetupMenuRow(
                            icon = Icons.Outlined.DynamicForm,
                            tileColor = OrangeLight,
                            iconColor = OrangePrimary,
                            title = "Form Builder",
                            subtitle = "Build individual registration questions",
                            onClick = {
                                if (hasType) onContinue(settings)
                            }
                        )

                        if (settings.allowTeam) {
                            HorizontalDivider(color = Color(0xFFEFEFEF))
                            SetupMenuRow(
                                icon = Icons.Outlined.Groups,
                                tileColor = BlueTile,
                                iconColor = BlueIcon,
                                title = "Team Questions",
                                subtitle = "Build questions answered once per team",
                            onClick = { onTeamQuestions(settings) }
                            )
                        }
                    }
                }
            }

            // Responses
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBg
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                SetupMenuRow(
                    icon = Icons.Outlined.BarChart,
                    tileColor = PurpleTile,
                    iconColor = PurpleIcon,
                    title = "View Responses",
                    subtitle = "Review participant registrations",
                    onClick = onViewResponses
                )
            }

        }
    }
    if (showDeadlinePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = settings.deadline
                ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = {
                showDeadlinePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { date ->
                            selectedDeadlineDateMillis = date
                            showDeadlinePicker = false
                            showDeadlineTimePicker = true
                        } ?: run {
                            showDeadlinePicker = false
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeadlinePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeadlineTimePicker) {
        val existingDeadline = settings.deadline?.let { Calendar.getInstance().apply { timeInMillis = it } }
        val timePickerState = rememberTimePickerState(
            initialHour = existingDeadline?.get(Calendar.HOUR_OF_DAY) ?: 23,
            initialMinute = existingDeadline?.get(Calendar.MINUTE) ?: 59,
            is24Hour = false,
        )

        AlertDialog(
            onDismissRequest = { showDeadlineTimePicker = false },
            title = { Text("Choose deadline time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDeadlineDateMillis?.let { selectedDate ->
                            val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = selectedDate
                            }
                            val localDeadline = Calendar.getInstance().apply {
                                clear()
                                set(
                                    utcCalendar.get(Calendar.YEAR),
                                    utcCalendar.get(Calendar.MONTH),
                                    utcCalendar.get(Calendar.DAY_OF_MONTH),
                                    timePickerState.hour,
                                    timePickerState.minute,
                                    0,
                                )
                            }
                            changeSettings(settings.copy(deadline = localDeadline.timeInMillis))
                        }
                        showDeadlineTimePicker = false
                    }
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showDeadlineTimePicker = false }) { Text("Cancel") }
            },
        )
    }
}


// ── Registration type card ────────────────────────────────────────────────────
