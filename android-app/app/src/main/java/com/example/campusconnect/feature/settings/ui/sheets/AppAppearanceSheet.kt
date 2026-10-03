package com.example.campusconnect.feature.settings.ui.sheets

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.campusconnect.feature.profile.ui.components.DividerColor
import com.example.campusconnect.feature.profile.ui.components.TextMuted
import com.example.campusconnect.feature.settings.model.ThemeOption
import com.example.campusconnect.feature.settings.model.ThemeOptionId
import com.example.campusconnect.feature.settings.ui.components.PrimaryOrange
import com.example.campusconnect.feature.settings.ui.components.PrimaryOrangeLight
import com.example.campusconnect.feature.settings.ui.components.SettingsBottomSheet
import com.example.campusconnect.feature.settings.ui.components.SettingsIconChip
import com.example.campusconnect.feature.settings.ui.components.SettingsRadioRow
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppAppearanceSheet(
    options: List<ThemeOption>,
    selected: ThemeOptionId,
    onSelect: (ThemeOptionId) -> Unit,
    onDismiss: () -> Unit,
    onDone: () -> Unit
) {
    SettingsBottomSheet(
        title = "App Appearance",
        subtitle = "Choose how you want the app to look",
        onDismiss = onDismiss,
        primaryButtonText = "Done",
        onPrimaryClick = onDone
    ) {
        options.forEach { option ->
            val isSelected = selected == option.id
            SettingsRadioRow(
                title = option.label,
                subtitle = option.subtitle,
                selected = isSelected,
                onClick = { onSelect(option.id) },
                leading = {
                    SettingsIconChip(
                        icon = option.icon,
                        tint = if (isSelected) PrimaryOrange else TextMuted,
                        background = if (isSelected) PrimaryOrangeLight else DividerColor,
                        size = 34.dp
                    )
                }
            )
        }
    }
}
