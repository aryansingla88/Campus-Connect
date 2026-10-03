package com.example.campusconnect.feature.settings.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.profile.ui.components.TextMuted
import com.example.campusconnect.feature.profile.ui.components.TextPrimary

// --- Shared header for the Settings screen and every full-screen sub-page --------------
// (Profile Visibility, Manage Social Links, Notifications) so the back-arrow / title /
// subtitle treatment stays identical everywhere instead of being rebuilt per screen.
@Composable
fun SettingsTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(start = 40.dp, top = 2.dp)
            )
        }
    }
}
