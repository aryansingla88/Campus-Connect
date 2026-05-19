package com.example.campusconnect.feature.profile.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared profile header.
 *
 * @param initials       Two-letter avatar initials.
 * @param displayName    Full display name.
 * @param username       Handle, e.g. "@aryan.sharma".
 * @param bio            Short bio shown below the handle.
 * @param badgeColors    List of badge dot colors (shown left of divider).
 * @param medalColors    List of medal dot colors (shown right of divider).
 * @param avatarOverlay  Optional composable overlaid on top-right of the avatar
 *                       (e.g. an edit-photo button for MyProfile).
 * @param headerAction   Optional composable placed at the top-right of the whole
 *                       header box (e.g. a connect/message button for ViewProfile).
 */
@Composable
fun ProfileHeader(
    initials: String,
    displayName: String,
    username: String,
    bio: String,
    badgeColors: List<Color> = emptyList(),
    medalColors: List<Color> = emptyList(),
    avatarOverlay: (@Composable BoxScope.() -> Unit)? = null,
    headerAction: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Color.White)
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp)
    ) {
        // Optional top-right action (e.g. Connect button on ViewProfile)
        headerAction?.let {
            Box(modifier = Modifier.align(Alignment.TopEnd)) { it() }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar with optional overlay
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(OrangeLight)
                    .border(3.dp, Orange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initials,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeDark
                )
                avatarOverlay?.invoke(this)
            }

            Spacer(Modifier.height(12.dp))
            Text(displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(username, fontSize = 13.sp, color = Orange)
            Spacer(Modifier.height(6.dp))
            Text(
                text = bio,
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.widthIn(max = 260.dp)
            )

            if (badgeColors.isNotEmpty() || medalColors.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (badgeColors.isNotEmpty()) {
                        Text("BADGES", fontSize = 10.sp, color = TextMuted, letterSpacing = 0.6.sp)
                        Spacer(Modifier.width(4.dp))
                        badgeColors.forEachIndexed { i, color ->
                            BadgeDot(color)
                            if (i < badgeColors.lastIndex) Spacer(Modifier.width(3.dp))
                        }
                    }

                    if (badgeColors.isNotEmpty() && medalColors.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.width(1.dp).height(14.dp).background(DividerColor))
                        Spacer(Modifier.width(8.dp))
                    }

                    if (medalColors.isNotEmpty()) {
                        Text("MEDALS", fontSize = 10.sp, color = TextMuted, letterSpacing = 0.6.sp)
                        Spacer(Modifier.width(4.dp))
                        medalColors.forEachIndexed { i, color ->
                            BadgeDot(color)
                            if (i < medalColors.lastIndex) Spacer(Modifier.width(3.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeDot(color: Color) {
    Box(Modifier.size(15.dp).clip(CircleShape).background(color))
}
