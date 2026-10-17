package com.example.campusconnect.feature.registrations.ui.screens
import com.example.campusconnect.feature.registrations.ui.components.*

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

// ── Extra colors (only used on this screen) ───────────────────────────────────

private val BlueTile   = Color(0xFFE3F2FD)
private val BlueIcon   = Color(0xFF1E88E5)
private val PurpleTile = Color(0xFFEDE7F6)
private val PurpleIcon = Color(0xFF7E57C2)
private val GreenBg    = Color(0xFFE8F5E9)
private val GreenText  = Color(0xFF2E7D32)
private val ErrorRed   = Color(0xFFE53935)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun RegistrationSetupScreen(
    eventName: String,
    isPublished: Boolean,
    initialSettings: RegistrationSettings,
    onBack: () -> Unit,
    onContinue: (RegistrationSettings) -> Unit,
    onQuickFields: () -> Unit = {},
    onTeamQuestions: () -> Unit = {},
    onViewResponses: () -> Unit = {},
) {
    var settings by remember { mutableStateOf(initialSettings) }

    val hasType = settings.allowIndividual || settings.allowTeam

    Scaffold(
        containerColor = PageBg,

        topBar = {
            RegistrationHeader(
                eventName = eventName,
                isPublished = isPublished,
                onBack = onBack,
                action = {
                    HeaderActionButton(
                        text = "Save",
                        icon = Icons.Outlined.Save,
                        onClick = {
                            onContinue(settings)
                        }
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

            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {

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
                    onChecked = {
                        settings = settings.copy(allowIndividual = it)
                    }
                )

                RegistrationTypeCard(
                    icon = Icons.Outlined.Groups,
                    title = "Team registration",
                    subtitle = "Participants register together as a team.",
                    checked = settings.allowTeam,
                    onChecked = {
                        settings = settings.copy(allowTeam = it)
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
                    onSettingsChange = { settings = it }
                )
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE0E0E0)
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
                                onClick = onTeamQuestions
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

            Spacer(Modifier.height(8.dp))
        }
    }
}
// ── Registration type card ────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
}

@Composable
private fun RegistrationTypeCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = if (checked) OrangeSurface else CardBg),
        border   = BorderStroke(1.dp, if (checked) OrangeBorder else Color(0xFFE0E0E0)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconTile(icon = icon, tileColor = OrangeLight, iconColor = OrangePrimary)
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(subtitle, fontSize = 12.sp, color = TextSecondary)
            }
            Switch(checked = checked, onCheckedChange = onChecked, colors = orangeSwitchColors())
        }
    }
}

// ── Team settings ─────────────────────────────────────────────────────────────

@Composable
private fun TeamSettingsCard(
    settings: RegistrationSettings,
    onSettingsChange: (RegistrationSettings) -> Unit,
) {
    val minAllowed = RegistrationSettings.MIN_TEAM_SIZE
    val maxAllowed = RegistrationSettings.MAX_TEAM_SIZE

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("Team settings")
        Card(
            shape    = RoundedCornerShape(18.dp),
            colors   = CardDefaults.cardColors(containerColor = CardBg),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                StepperRow(
                    label   = "Minimum members",
                    value   = settings.minMembers,
                    canDec  = settings.minMembers > minAllowed,
                    canInc  = settings.minMembers < maxAllowed,
                    onDec   = { onSettingsChange(settings.copy(minMembers = settings.minMembers - 1)) },
                    onInc   = {
                        val newMin = settings.minMembers + 1
                        // keep max >= min
                        onSettingsChange(settings.copy(minMembers = newMin, maxMembers = maxOf(settings.maxMembers, newMin)))
                    },
                )
                StepperRow(
                    label   = "Maximum members",
                    value   = settings.maxMembers,
                    canDec  = settings.maxMembers > settings.minMembers,
                    canInc  = settings.maxMembers < maxAllowed,
                    onDec   = { onSettingsChange(settings.copy(maxMembers = settings.maxMembers - 1)) },
                    onInc   = { onSettingsChange(settings.copy(maxMembers = settings.maxMembers + 1)) },
                )
            }
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    canDec: Boolean,
    canInc: Boolean,
    onDec: () -> Unit,
    onInc: () -> Unit,
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 14.sp, color = TextPrimary)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StepperButton(Icons.Outlined.Remove, "Decrease $label", enabled = canDec, onClick = onDec)
            Text(
                "$value",
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary,
                modifier   = Modifier.widthIn(min = 20.dp),
            )
            StepperButton(Icons.Outlined.Add, "Increase $label", enabled = canInc, onClick = onInc)
        }
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val tint = if (enabled) OrangePrimary else Color(0xFFBDBDBD)
    val borderColor = if (enabled) OrangeBorder else Color(0xFFE0E0E0)

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .border(1.dp, borderColor, CircleShape)
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
    }
}
// ── Menu rows ─────────────────────────────────────────────────────────────────

@Composable
private fun SetupMenuRow(
    icon: ImageVector,
    tileColor: Color,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    badge: String? = null,
) {
    Row(
        modifier              = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconTile(icon = icon, tileColor = tileColor, iconColor = iconColor)
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        if (badge != null) {
            Text(
                badge,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Medium,
                color      = GreenText,
                modifier   = Modifier.clip(RoundedCornerShape(8.dp)).background(GreenBg).padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun IconTile(icon: ImageVector, tileColor: Color, iconColor: Color) {
    Box(
        modifier         = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(tileColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun orangeSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor   = Color.White,
    checkedTrackColor   = OrangePrimary,
    uncheckedThumbColor = Color.White,
    uncheckedTrackColor = Color(0xFFBDBDBD),
)
