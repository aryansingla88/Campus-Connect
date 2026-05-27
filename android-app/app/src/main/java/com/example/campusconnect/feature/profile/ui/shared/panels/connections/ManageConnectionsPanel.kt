package com.example.campusconnect.feature.profile.ui.shared.panels.connections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.profile.ui.shared.TextMuted

// Swaps in below StatsRow when the "Requests" bottom-bar CTA is tapped.
// Placeholder – build out pending/sent requests UI here.
@Composable
fun ManageConnectionsPanel() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Manage Connections – coming soon",
            fontSize = 13.sp,
            color = TextMuted
        )
    }
}
