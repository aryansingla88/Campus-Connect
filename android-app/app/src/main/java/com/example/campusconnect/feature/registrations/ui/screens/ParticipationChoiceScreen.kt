package com.example.campusconnect.feature.registrations.ui.screens

import com.example.campusconnect.feature.registrations.ui.components.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.ui.components.OrangeBorder
import com.example.campusconnect.feature.registrations.ui.components.OrangeLight
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.OrangeSurface
import com.example.campusconnect.feature.registrations.ui.components.TextPrimary
import com.example.campusconnect.feature.registrations.ui.components.TextSecondary
import com.example.campusconnect.feature.registrations.model.ParticipationMode
import com.example.campusconnect.feature.registrations.model.UserRegistrationStep

private val JoinBg = Color(0xFFEDEBFC)
private val JoinFg = Color(0xFF6C4CF1)
private val SoloBg = Color(0xFFDDF6EA)
private val SoloFg = Color(0xFF12A05C)
private val CardOutline = Color(0xFFE6E8EC)

/** "How would you like to participate?" — create a team, join one, or go solo. */
@Composable
fun ParticipationChoiceScreen(
    allowSolo: Boolean,
    selected: ParticipationMode?,
    teamName: String,
    teamNameAvailable: Boolean?,
    checkingTeamName: Boolean,
    creatingTeam: Boolean,
    transientError: String?,
    onTeamNameChange: (String) -> Unit,
    onCheckTeamName: () -> Unit,
    onSelect: (ParticipationMode) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.flowPageBackground(),
        containerColor = Color.Transparent,
        topBar = {
            FlowTopBar(
                title = "Event Registration",
                onBack = onBack,
                step = UserRegistrationStep.PARTICIPATION.progress,
            )
        },
        bottomBar = {
            NextBottomBar(
                onClick = onNext,
                enabled = selected != null && (selected != ParticipationMode.CREATE_TEAM || teamNameAvailable == true)
                        && !checkingTeamName && !creatingTeam,
                loading = creatingTeam,
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            StepHeader(
                icon = Icons.Outlined.Groups,
                title = "Team Registration",
                subtitle = "How would you like to participate?",
            )
            Spacer(Modifier.height(2.dp))

            ParticipationOptionCard(
                icon = Icons.Outlined.GroupAdd,
                iconBackground = OrangeLight,
                iconTint = OrangePrimary,
                title = "Create a new team",
                description = "Become a team leader and invite others later.",
                selected = selected == ParticipationMode.CREATE_TEAM,
                onClick = { onSelect(ParticipationMode.CREATE_TEAM) },
            )
            ParticipationOptionCard(
                icon = Icons.Outlined.PersonSearch,
                iconBackground = JoinBg,
                iconTint = JoinFg,
                title = "Join an existing team",
                description = "Browse and request to join available teams.",
                selected = selected == ParticipationMode.JOIN_TEAM,
                onClick = { onSelect(ParticipationMode.JOIN_TEAM) },
            )
            if (allowSolo) {
                ParticipationOptionCard(
                    icon = Icons.Outlined.Person,
                    iconBackground = SoloBg,
                    iconTint = SoloFg,
                    title = "Continue solo",
                    description = "Participate individually in this event.",
                    note = "(Solo registration is allowed (min 1 member).)",
                    selected = selected == ParticipationMode.SOLO,
                    onClick = { onSelect(ParticipationMode.SOLO) },
                )
            }

            AnimatedVisibility(visible = selected == ParticipationMode.CREATE_TEAM) {
                TeamNameSection(
                    teamName = teamName,
                    teamNameAvailable = teamNameAvailable,
                    checking = checkingTeamName,
                    creating = creatingTeam,
                    error = transientError,
                    onTeamNameChange = onTeamNameChange,
                    onCheck = onCheckTeamName,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TeamNameSection(
    teamName: String,
    teamNameAvailable: Boolean?,
    checking: Boolean,
    creating: Boolean,
    error: String?,
    onTeamNameChange: (String) -> Unit,
    onCheck: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalDivider(color = CardOutline, modifier = Modifier.padding(vertical = 4.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(OrangeLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.EditNote, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(24.dp))
            }
            Column {
                Text("Team Setup", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Set a unique name for your team.", fontSize = 14.sp, color = TextSecondary)
            }
        }

        FlowTextInput(
            value = teamName,
            onValueChange = onTeamNameChange,
            placeholder = "Team name",
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
            enabled = !creating,
            isError = teamNameAvailable == false,
            leadingIcon = Icons.Outlined.Groups,
        )

        val checkEnabled = !checking && !creating && teamName.isNotBlank()
        Surface(
            onClick = onCheck,
            enabled = checkEnabled,
            shape = RoundedCornerShape(14.dp),
            color = OrangeSurface,
            border = BorderStroke(1.dp, OrangeBorder),
            modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            Row(
                Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                if (checking) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = OrangePrimary)
                } else {
                    Icon(
                        Icons.Outlined.CheckCircle, contentDescription = null,
                        tint = if (checkEnabled) OrangePrimary else TextSecondary, modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    if (checking) "Checking..." else if (teamNameAvailable == true) "Check again" else "Check name availability",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (checkEnabled || checking) TextPrimary else TextSecondary,
                )
            }
        }

        when {
            error != null -> Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            teamNameAvailable == true -> Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = SoloFg, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("This team name is available. Press Next to create the team.", color = SoloFg, fontSize = 13.sp)
            }
            teamNameAvailable == false -> Text(
                "This team name is unavailable. Choose another name.",
                color = MaterialTheme.colorScheme.error, fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun ParticipationOptionCard(
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    note: String? = null,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) OrangeSurface else CardBg,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) OrangePrimary else CardOutline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(description, fontSize = 14.sp, lineHeight = 20.sp, color = TextSecondary)
                if (note != null) Text(note, fontSize = 12.sp, lineHeight = 16.sp, color = TextSecondary)
            }
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = OrangePrimary,
                    unselectedColor = TextSecondary,
                ),
            )
        }
    }
}
