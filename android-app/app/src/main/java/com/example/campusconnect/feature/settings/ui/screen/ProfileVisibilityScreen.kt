package com.example.campusconnect.feature.settings.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.campusconnect.feature.settings.ui.components.SettingsInfoBanner
import com.example.campusconnect.feature.settings.ui.components.SettingsIconChip
import com.example.campusconnect.feature.settings.ui.components.SettingsPrimaryButton
import com.example.campusconnect.feature.settings.ui.components.SettingsStaticRow
import com.example.campusconnect.feature.settings.ui.components.SettingsSwitchRow
import com.example.campusconnect.feature.settings.ui.components.SettingsTopBar
import com.example.campusconnect.feature.settings.viewmodel.SettingsViewModel

@Composable
fun ProfileVisibilityScreen(
    onBack: () -> Unit = {},
    onSaveChanges: () -> Unit = {},
    vm: SettingsViewModel = viewModel()
) {
    Scaffold(
        containerColor = PageBg,
        topBar = {
            SettingsTopBar(
                title = "Profile Visibility",
                subtitle = "Control who can see your profile information",
                onBack = onBack
            )
        },
        bottomBar = {
            SettingsPrimaryButton(text = "Save Changes", onClick = onSaveChanges)
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

            SettingsInfoBanner(
                text = "You can choose which information is visible on Campus Connect. " +
                        "Some basic information like your name and course will always be visible."
            )

            Column {
                Text(
                    "Always Visible",
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
                        vm.alwaysVisibleFields.forEachIndexed { index, field ->
                            SettingsStaticRow(icon = field.icon, label = field.label)
                            if (index != vm.alwaysVisibleFields.lastIndex) {
                                HorizontalDivider(
                                    color = DividerColor,
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }
            }

            Column {
                Text(
                    "Control Visibility",
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
                        vm.visibilityToggles.forEachIndexed { index, toggle ->
                            SettingsSwitchRow(
                                title = toggle.label,
                                subtitle = toggle.subtitle,
                                checked = toggle.isOn,
                                onCheckedChange = { isOn ->
                                    vm.setVisibilityToggle(toggle.id, isOn)
                                },
                                leading = {
                                    SettingsIconChip(
                                        icon = toggle.icon,
                                        tint = toggle.iconTint,
                                        background = toggle.iconBackground
                                    )
                                }
                            )
                            if (index != vm.visibilityToggles.lastIndex) {
                                HorizontalDivider(
                                    color = DividerColor,
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}
