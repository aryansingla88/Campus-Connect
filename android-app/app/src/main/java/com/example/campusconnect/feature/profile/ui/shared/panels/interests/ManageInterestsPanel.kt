package com.example.campusconnect.feature.profile.ui.shared.panels.interests

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.profile.ui.shared.TextMuted

// Swaps in below StatsRow when the "+" add-interest button is tapped.
// Triggered by the plus button inside InterestsPanel (not the bottom bar CTA).
// Placeholder – build out interest search & selection UI here.
@Composable
fun ManageInterestsPanel() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Manage Interests – coming soon",
            fontSize = 13.sp,
            color = TextMuted
        )
    }
}
