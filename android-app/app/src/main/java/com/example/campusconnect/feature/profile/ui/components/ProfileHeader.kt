package com.example.campusconnect.feature.profile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.campusconnect.core.components.AppAvatar

@Composable
fun ProfileHeader(
    entityId: Int,
    avatarUrl: String?,
    displayName: String,
    username: String,
    bio: String,
    badgeColors: List<Color> = emptyList(),
    medalColors: List<Color> = emptyList(),
    isEditMode: Boolean = false,
    onEditAvatar: (() -> Unit)? = null,
    onBioChange: ((String) -> Unit)? = null,
    avatarOverlay: (@Composable BoxScope.() -> Unit)? = null,
    headerAction: (@Composable () -> Unit)? = null,
) {
    // biostate
    var bioEditing by remember(isEditMode) { mutableStateOf(false) }
    // localdraft
    var bioDraft   by remember(bio) { mutableStateOf(bio) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Color.White)
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp)
    ) {
        headerAction?.let {
            Box(modifier = Modifier.align(Alignment.TopEnd)) { it() }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Avatar ────────────────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center
            ) {

                AppAvatar(
                    entityId = entityId,
                    displayName = displayName,
                    imageUrl = avatarUrl,
                    size = 82.dp,
                    showBorder = true,
                    borderWidth = 3.dp
                )

                avatarOverlay?.invoke(this)

                if (isEditMode && onEditAvatar != null) {
                    Box(
                        modifier = Modifier
                            .size(82.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onEditAvatar
                        ) {
                            Icon(
                                Icons.Outlined.CameraAlt,
                                contentDescription = "Change photo",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(username,    fontSize = 13.sp, color = Orange)
            Spacer(Modifier.height(6.dp))

            // ── Bio ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier.widthIn(max = 280.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isEditMode && bioEditing) {

                    OutlinedTextField(
                        value         = bioDraft,
                        onValueChange = { bioDraft = it },
                        textStyle     = LocalTextStyle.current.copy(
                            fontSize  = 12.sp,
                            color     = TextPrimary,
                            textAlign = TextAlign.Center
                        ),
                        placeholder = {
                            Text(
                                "Write a short bio…",
                                fontSize  = 12.sp,
                                color     = TextMuted,
                                textAlign = TextAlign.Center,
                                modifier  = Modifier.fillMaxWidth()
                            )
                        },
                        trailingIcon = {

                            IconButton(onClick = {
                                onBioChange?.invoke(bioDraft)
                                bioEditing = false
                            }) {
                                Icon(
                                    Icons.Outlined.Check,
                                    contentDescription = "Confirm bio",
                                    tint   = Orange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        singleLine    = false,
                        maxLines      = 4,
                        shape         = RoundedCornerShape(12.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Orange,
                            unfocusedBorderColor = DividerColor,
                            cursorColor          = Orange
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    )
                } else {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text      = bio,
                            fontSize  = 12.sp,
                            color     = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier  = Modifier.weight(1f, fill = false)
                        )
                        if (isEditMode) {
                            Spacer(Modifier.width(4.dp))
                            IconButton(
                                onClick  = { bioEditing = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = "Edit bio",
                                    tint     = Orange,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── Badges / Medals row ──────────────────────────
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
