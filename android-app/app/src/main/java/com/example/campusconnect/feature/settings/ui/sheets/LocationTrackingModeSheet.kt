package com.example.campusconnect.feature.settings.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusconnect.core.location.TrackingMode

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LocationTrackingModeSheet(
    selectedMode: TrackingMode,
    onSelectMode: (TrackingMode) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Location tracking",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Tracking runs only while the map is active.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            TrackingMode.entries.forEach { mode ->
                val title = when (mode) {
                    TrackingMode.BALANCED -> "Balanced"
                    TrackingMode.HIGH_PRECISION -> "High Precision"
                }
                val description = when (mode) {
                    TrackingMode.BALANCED -> "Lower battery use with useful campus-level accuracy."
                    TrackingMode.HIGH_PRECISION -> "More frequent, more accurate fixes with higher battery use."
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectMode(mode) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    RadioButton(
                        selected = selectedMode == mode,
                        onClick = { onSelectMode(mode) }
                    )
                }
            }
        }
    }
}
