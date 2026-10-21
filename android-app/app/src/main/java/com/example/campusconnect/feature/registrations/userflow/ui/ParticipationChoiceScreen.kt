package com.example.campusconnect.feature.registrations.userflow.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.ui.components.OrangeLight
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.OrangeSurface
import com.example.campusconnect.feature.registrations.ui.components.TextPrimary
import com.example.campusconnect.feature.registrations.ui.components.TextSecondary
import com.example.campusconnect.feature.registrations.userflow.model.ParticipationMode
import com.example.campusconnect.feature.registrations.userflow.model.UserRegistrationStep
import com.example.campusconnect.feature.registrations.userflow.model.TEAM_NAME_MAX_LENGTH
import com.example.campusconnect.feature.registrations.userflow.model.TEAM_NAME_MIN_LENGTH

private val JoinBg = Color(0xFFEDEBFC)
private val JoinFg = Color(0xFF6C4CF1)
private val SoloBg = Color(0xFFDDF6EA)
private val SoloFg = Color(0xFF12A05C)
private val CardOutline = Color(0xFFE0E3E8)

/** "How would you like to participate?" — create a team, join one, or go solo. */
@Composable
fun ParticipationChoiceScreen(
    allowTeam: Boolean = true,
    allowSolo: Boolean,
    selected: ParticipationMode?,
    onSelect: (ParticipationMode) -> Unit,
    teamName: String,
    teamNamePromptOpen: Boolean,
    teamNameError: String?,
    teamNameResultMessage: String?,
    teamNameResultAvailable: Boolean?,
    isCheckingTeamName: Boolean,
    teamNameConfirmed: Boolean,
    onTeamNameChange: (String) -> Unit,
    onEditTeamName: () -> Unit,
    onValidateTeamName: () -> Unit,
    onDismissTeamNameResult: () -> Unit,
    onCancelTeamName: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val showTeamNameDialog = teamNamePromptOpen && selected == ParticipationMode.CREATE_TEAM
    Scaffold(
        containerColor = CardBg,
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
                enabled = selected != null && (selected != ParticipationMode.CREATE_TEAM || teamNameConfirmed),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            StepHeader(
                icon = Icons.Outlined.Groups,
                title = "Team Registration",
                subtitle = "How would you like to participate?",
            )

            if (allowTeam) ParticipationOptionCard(
                icon = Icons.Outlined.GroupAdd,
                iconBackground = OrangeLight,
                iconTint = OrangePrimary,
                title = "Create a new team",
                description = "Become a team leader and invite others later.",
                selected = selected == ParticipationMode.CREATE_TEAM,
                onClick = { onSelect(ParticipationMode.CREATE_TEAM) },
            )
            if (teamNameConfirmed && selected == ParticipationMode.CREATE_TEAM) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Team name available: ${teamName.trim()}",
                        fontSize = 12.sp,
                        color = SoloFg,
                        modifier = Modifier.padding(start = 8.dp).weight(1f),
                    )
                    TextButton(onClick = onEditTeamName) { Text("Edit", color = OrangePrimary) }
                }
            }
            if (allowTeam) ParticipationOptionCard(
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
                    note = "(Solo registration is allowed)",
                    selected = selected == ParticipationMode.SOLO,
                    onClick = { onSelect(ParticipationMode.SOLO) },
                )
            }
        }
    }

    if (showTeamNameDialog) {
        AlertDialog(
            onDismissRequest = onCancelTeamName,
            title = { Text("Choose a team name", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "This name will identify your team in the Team Portal. Check availability here, then press Next to continue to team questions.",
                        color = TextSecondary,
                    )
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { onTeamNameChange(it.take(TEAM_NAME_MAX_LENGTH)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCheckingTeamName,
                        singleLine = true,
                        label = { Text("Team name") },
                        placeholder = { Text("e.g. Bright Sparks") },
                        isError = teamNameError != null,
                        supportingText = {
                            Text(teamNameError ?: "$TEAM_NAME_MIN_LENGTH–$TEAM_NAME_MAX_LENGTH characters")
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = CardOutline,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                        ),
                    )
                    if (isCheckingTeamName) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = OrangePrimary)
                        Text("Checking availability…", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onValidateTeamName,
                    enabled = !isCheckingTeamName && teamName.trim().length in TEAM_NAME_MIN_LENGTH..TEAM_NAME_MAX_LENGTH,
                ) { Text("Check name", color = OrangePrimary) }
            },
            dismissButton = {
                TextButton(onClick = onCancelTeamName, enabled = !isCheckingTeamName) { Text("Cancel", color = TextSecondary) }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = CardBg,
        )
    }

    if (teamNameResultMessage != null) {
        AlertDialog(
            onDismissRequest = onDismissTeamNameResult,
            title = { Text(if (teamNameResultAvailable == true) "Team name available" else "Team name check") },
            text = { Text(teamNameResultMessage, color = if (teamNameResultAvailable == true) SoloFg else TextSecondary) },
            confirmButton = { TextButton(onClick = onDismissTeamNameResult) { Text("OK", color = OrangePrimary) } },
            shape = RoundedCornerShape(20.dp),
            containerColor = CardBg,
        )
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
        shape = RoundedCornerShape(14.dp),
        color = if (selected) OrangeSurface else CardBg,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) OrangePrimary else CardOutline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(iconBackground, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(description, fontSize = 13.sp, lineHeight = 18.sp, color = TextSecondary)
                if (note != null) Text(note, fontSize = 12.sp, lineHeight = 16.sp, color = TextSecondary)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
        }
    }
}
