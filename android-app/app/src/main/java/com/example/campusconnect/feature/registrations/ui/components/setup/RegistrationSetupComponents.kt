package com.example.campusconnect.feature.registrations.ui.components.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import com.example.campusconnect.feature.registrations.model.RegistrationSettings
import com.example.campusconnect.feature.registrations.ui.components.*
@Composable
internal fun SectionLabel(text: String) {
    Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
}

@Composable
internal fun RegistrationTypeCard(
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
internal fun TeamSettingsCard(
    settings: RegistrationSettings,
    onSettingsChange: (RegistrationSettings) -> Unit,
) {
    // A one-person team is allowed only while individual registration is enabled.
    val minAllowed = if (settings.allowIndividual) 1 else RegistrationSettings.MIN_TEAM_SIZE
    val maxAllowed = RegistrationSettings.MAX_TEAM_SIZE

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("Team settings")
        Card(
            shape    = RoundedCornerShape(18.dp),
            colors   = CardDefaults.cardColors(containerColor = CardBg),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    canDec  = settings.maxMembers > maxOf(settings.minMembers, 2),
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
        modifier              = Modifier.fillMaxWidth().heightIn(min = 38.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StepperButton(Icons.Outlined.Remove, "Decrease $label", enabled = canDec, onClick = onDec)
            Text(
                "$value",
                fontSize   = 14.sp,
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
            .size(28.dp)
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
internal fun SetupMenuRow(
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
