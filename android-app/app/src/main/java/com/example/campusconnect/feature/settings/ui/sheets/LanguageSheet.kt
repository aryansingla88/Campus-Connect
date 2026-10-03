package com.example.campusconnect.feature.settings.ui.sheets

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.campusconnect.feature.profile.ui.components.DividerColor
import com.example.campusconnect.feature.profile.ui.components.TextMuted
import com.example.campusconnect.feature.settings.model.LanguageOption
import com.example.campusconnect.feature.settings.model.LanguageOptionId
import com.example.campusconnect.feature.settings.ui.components.PrimaryOrange
import com.example.campusconnect.feature.settings.ui.components.PrimaryOrangeLight
import com.example.campusconnect.feature.settings.ui.components.SettingsBottomSheet
import com.example.campusconnect.feature.settings.ui.components.SettingsGlyphChip
import com.example.campusconnect.feature.settings.ui.components.SettingsRadioRow
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSheet(
    options: List<LanguageOption>,
    selected: LanguageOptionId,
    onSelect: (LanguageOptionId) -> Unit,
    onDismiss: () -> Unit,
    onDone: () -> Unit
) {
    SettingsBottomSheet(
        title = "Language",
        subtitle = "Choose your preferred language",
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
                    SettingsGlyphChip(
                        glyph = option.glyph,
                        tint = if (isSelected) PrimaryOrange else TextMuted,
                        background = if (isSelected) PrimaryOrangeLight else DividerColor,
                        size = 34.dp,
                        circular = true
                    )
                }
            )
        }
    }
}
