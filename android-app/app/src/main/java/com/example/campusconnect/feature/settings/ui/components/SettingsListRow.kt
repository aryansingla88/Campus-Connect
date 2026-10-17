package com.example.campusconnect.feature.settings.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.profile.ui.components.TextMuted
import com.example.campusconnect.feature.profile.ui.components.TextPrimary
import com.example.campusconnect.feature.settings.model.SettingsItem

// --- One row inside a SettingsSectionCard -----------------------------------------------
// Mirrors ProfileListCard's row shape (icon / title+subtitle / trailing) but is always
// full-width inside its parent Card rather than being a Card itself, so section items
// can share one continuous surface with dividers between them.
@Composable
fun SettingsListRow(
    item: SettingsItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsIconChip(
            icon = item.icon,
            tint = item.iconTint,
            background = item.iconBackground
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (item.isDestructive) DangerRed else TextPrimary
            )

            if (item.subtitle.isNotBlank()) {
                Text(
                    text = item.subtitle,
                    fontSize = 12.sp,
                    color = if (item.isDestructive) DangerRed.copy(alpha = 0.7f) else TextMuted
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = if (item.isDestructive) DangerRed else TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

// --- Static, non-interactive row used for "Always Visible" fields in Profile ---------
// Visibility — same silhouette as SettingsListRow so the two read as one family, but
// no click target and a muted trailing tag instead of a chevron.
@Composable
fun SettingsStaticRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tag: String = "Always visible",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AlwaysVisibleIcon,
            modifier = Modifier.size(18.dp)
        )

        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = tag,
            fontSize = 11.sp,
            color = AlwaysVisibleTag
        )
    }
}
