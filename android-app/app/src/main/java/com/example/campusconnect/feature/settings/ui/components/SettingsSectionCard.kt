package com.example.campusconnect.feature.settings.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.profile.ui.components.CardBg
import com.example.campusconnect.feature.profile.ui.components.DividerColor
import com.example.campusconnect.feature.profile.ui.components.TextPrimary
import com.example.campusconnect.feature.settings.model.SettingsItem
import com.example.campusconnect.feature.settings.model.SettingsSection

// --- Section heading + the card that groups its rows ------------------------------------
@Composable
fun SettingsSectionCard(
    section: SettingsSection,
    onItemClick: (SettingsItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = section.title,
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
                section.items.forEachIndexed { index, item ->
                    SettingsListRow(
                        item = item,
                        onClick = { onItemClick(item) }
                    )

                    if (index != section.items.lastIndex) {
                        HorizontalDivider(
                            color = DividerColor,
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}

// --- Standalone destructive row (Delete Account) ----------------------------------------
// Rendered outside any titled section, on its own tinted card — matches the design where
// this row is visually separated from the rest of the settings list.
@Composable
fun SettingsDangerCard(
    item: SettingsItem,
    onClick: (SettingsItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DangerRowBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        SettingsListRow(
            item = item,
            onClick = { onClick(item) }
        )
    }
}
