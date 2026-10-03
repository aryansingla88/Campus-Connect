package com.example.campusconnect.feature.settings.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.example.campusconnect.feature.profile.ui.components.TextMuted
import com.example.campusconnect.feature.settings.model.SelectableClub
import com.example.campusconnect.feature.settings.ui.components.SettingsBottomSheet
import com.example.campusconnect.feature.settings.ui.components.SettingsCheckboxRow
import com.example.campusconnect.feature.settings.ui.components.SettingsSearchField
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectClubsSheet(
    clubs: List<SelectableClub>,
    query: String,
    onQueryChange: (String) -> Unit,
    onToggleClub: (Int) -> Unit,
    onDismiss: () -> Unit,
    onDone: () -> Unit
) {
    SettingsBottomSheet(
        title = "Select Clubs",
        subtitle = "Choose the clubs that can see your location",
        onDismiss = onDismiss,
        primaryButtonText = "Done",
        onPrimaryClick = onDone
    ) {
        SettingsSearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Search clubs",
            modifier = Modifier.padding(bottom = 10.dp)
        )

        if (clubs.isEmpty()) {
            Text(
                text = "No clubs match \"$query\"",
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                clubs.forEach { club ->
                    SettingsCheckboxRow(
                        label = club.name,
                        checked = club.isSelected,
                        onToggle = { onToggleClub(club.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
