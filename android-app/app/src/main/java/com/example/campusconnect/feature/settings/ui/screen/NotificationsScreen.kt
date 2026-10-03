package com.example.campusconnect.feature.settings.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.campusconnect.feature.profile.ui.components.CardBg
import com.example.campusconnect.feature.profile.ui.components.DividerColor
import com.example.campusconnect.feature.profile.ui.components.PageBg
import com.example.campusconnect.feature.profile.ui.components.TextPrimary
import com.example.campusconnect.feature.settings.model.NotificationDefaults
import com.example.campusconnect.feature.settings.model.NotificationId
import com.example.campusconnect.feature.settings.model.NotificationToggle
import com.example.campusconnect.feature.settings.ui.components.IconPurple
import com.example.campusconnect.feature.settings.ui.components.IconPurpleBg
import com.example.campusconnect.feature.settings.ui.components.SettingsIconChip
import com.example.campusconnect.feature.settings.ui.components.SettingsListRow
import com.example.campusconnect.feature.settings.ui.components.SettingsSwitchRow
import com.example.campusconnect.feature.settings.ui.components.SettingsTopBar
import com.example.campusconnect.feature.settings.model.SettingsItem
import com.example.campusconnect.feature.settings.model.SettingsItemId
import com.example.campusconnect.feature.settings.viewmodel.SettingsViewModel

// Quiet Hours isn't one of the screens we were given, so tapping it is a no-op stub
// for now (`onQuietHoursClick`) — same "leave it for later" treatment as the main
// list's unimplemented rows.
@Composable
fun NotificationsScreen(
    onBack: () -> Unit = {},
    onQuietHoursClick: () -> Unit = {},
    vm: SettingsViewModel = viewModel()
) {
    Scaffold(
        containerColor = PageBg,
        topBar = {
            SettingsTopBar(
                title = "Notifications",
                subtitle = "Manage what you want to be notified about",
                onBack = onBack
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            NotificationToggleSection(
                title = "Activity",
                items = vm.activityNotifications,
                onToggle = { id, isOn -> vm.setNotificationToggle(vm.activityNotifications, id, isOn) }
            )

            NotificationToggleSection(
                title = "System",
                items = vm.systemNotifications,
                onToggle = { id, isOn -> vm.setNotificationToggle(vm.systemNotifications, id, isOn) }
            )

            Column {
                Text(
                    "Preferences",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    // Reuses the same SettingsListRow the main Settings list uses —
                    // icon / title / subtitle / chevron is exactly what this row needs.
                    SettingsListRow(
                        item = SettingsItem(
                            id = SettingsItemId.QUIET_HOURS,
                            title = NotificationDefaults.quietHoursLabel,
                            subtitle = NotificationDefaults.quietHoursSubtitle,
                            icon = Icons.Outlined.Bedtime,
                            iconTint = IconPurple,
                            iconBackground = IconPurpleBg
                        ),
                        onClick = onQuietHoursClick
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun NotificationToggleSection(
    title: String,
    items: List<NotificationToggle>,
    onToggle: (NotificationId, Boolean) -> Unit
) {
    Column {
        Text(
            title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column {
                items.forEachIndexed { index, toggle ->
                    SettingsSwitchRow(
                        title = toggle.label,
                        subtitle = toggle.subtitle,
                        checked = toggle.isOn,
                        onCheckedChange = { isOn -> onToggle(toggle.id, isOn) },
                        leading = {
                            SettingsIconChip(
                                icon = toggle.icon,
                                tint = toggle.iconTint,
                                background = toggle.iconBackground
                            )
                        }
                    )
                    if (index != items.lastIndex) {
                        HorizontalDivider(color = DividerColor, thickness = 1.dp)
                    }
                }
            }
        }
    }
}
