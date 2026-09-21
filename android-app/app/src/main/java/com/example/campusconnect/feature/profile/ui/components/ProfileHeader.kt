package com.example.campusconnect.feature.profile.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.campusconnect.R
import com.example.campusconnect.core.components.AppAvatar

@Composable
fun ProfileHeader(
    entityId: Int,
    avatarUrl: String?,
    avatarUri: Uri? = null,
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
    onBack: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null
) {
    var bioEditing by remember(isEditMode) {
        mutableStateOf(false)
    }

    var bioDraft by remember(bio) {
        mutableStateOf(bio)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    bottomStart = 30.dp,
                    bottomEnd = 30.dp
                )
            )
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /*
         * =========================
         * BANNER
         * =========================
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
        ) {

            AsyncImage(
                model = R.drawable.bg_campus_header,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Back
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(
                            start = 8.dp,
                            top = 6.dp
                        )
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // Settings
            if (onSettings != null) {
                IconButton(
                    onClick = onSettings,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            end = 8.dp,
                            top = 6.dp
                        )
                ) {
                    Icon(
                        imageVector =
                            Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = TextPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            /*
             * View-profile relationship action.
             * Keep it in the banner, below the settings button.
             */
            if (headerAction != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            end = 12.dp,
                            top = 52.dp
                        )
                ) {
                    headerAction()
                }
            }

            /*
             * =========================
             * AVATAR
             * =========================
             *
             * Important:
             * Avatar is anchored to the BOTTOM of the banner.
             */
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 25.dp)
                    .size(90.dp),
                contentAlignment = Alignment.Center
            ) {

                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                color = Color(0xFF07558A),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUri != null) {
                            AsyncImage(
                                model = avatarUri,
                                contentDescription = displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            AppAvatar(
                                entityId = entityId,
                                displayName = displayName,
                                imageUrl = avatarUrl,
                                size = 90.dp
                            )
                        }

                        avatarOverlay?.invoke(this)
                    }

                    // Camera stays outside the clipped avatar
                    if (isEditMode && onEditAvatar != null) {
                        IconButton(
                            onClick = onEditAvatar,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 4.dp, y = 4.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF111111))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = "Change photo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        /*
         * Reserve space for the avatar overlap.
         *
         * Banner = 115
         * Avatar overlap = 45
         */
        Spacer(
            modifier = Modifier.height(25.dp)
        )

        /*
         * =========================
         * NAME
         * =========================
         */
        Text(
            text = displayName,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        /*
         * USERNAME
         */
        Text(
            text = username,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Orange
        )

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        /*
         * =========================
         * BIO
         * =========================
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center
        ) {

            if (isEditMode && bioEditing) {

                OutlinedTextField(
                    value = bioDraft,
                    onValueChange = {
                        bioDraft = it
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 13.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    ),
                    placeholder = {
                        Text(
                            text = "Write a short bio…",
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                onBioChange?.invoke(bioDraft)
                                bioEditing = false
                            }
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Outlined.Check,
                                contentDescription =
                                    "Confirm bio",
                                tint = Orange
                            )
                        }
                    },
                    singleLine = false,
                    maxLines = 4,
                    shape = RoundedCornerShape(8.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Orange,
                            unfocusedBorderColor =
                                DividerColor,
                            cursorColor = Orange
                        ),
                    modifier = Modifier.fillMaxWidth()
                )

            } else {

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = bio,
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )

                    if (isEditMode) {
                        IconButton(
                            onClick = { bioEditing = true },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit bio",
                                tint = Orange,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        /*
         * =========================
         * BADGES / MEDALS
         * =========================
         */
        if (
            badgeColors.isNotEmpty() ||
            medalColors.isNotEmpty()
        ) {

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                thickness = 1.dp,
                color = DividerColor
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.Center
            ) {

                if (badgeColors.isNotEmpty()) {

                    Text(
                        text = "BADGES",
                        fontSize = 11.sp,
                        color = TextMuted,
                        letterSpacing = 0.7.sp
                    )

                    Spacer(
                        modifier = Modifier.width(7.dp)
                    )

                    badgeColors.forEachIndexed { index, color ->

                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(color)
                        )

                        if (
                            index < badgeColors.lastIndex
                        ) {
                            Spacer(
                                modifier =
                                    Modifier.width(4.dp)
                            )
                        }
                    }
                }

                if (
                    badgeColors.isNotEmpty() &&
                    medalColors.isNotEmpty()
                ) {

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(18.dp)
                            .background(DividerColor)
                    )

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )
                }

                if (medalColors.isNotEmpty()) {

                    Text(
                        text = "MEDALS",
                        fontSize = 11.sp,
                        color = TextMuted,
                        letterSpacing = 0.7.sp
                    )

                    Spacer(
                        modifier = Modifier.width(7.dp)
                    )

                    medalColors.forEachIndexed { index, color ->

                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(color)
                        )

                        if (
                            index < medalColors.lastIndex
                        ) {
                            Spacer(
                                modifier =
                                    Modifier.width(4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(6.dp)
            )
        } else {
            Spacer(
                modifier = Modifier.height(6.dp)
            )
        }
    }
}