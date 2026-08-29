package com.example.campusconnect.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.campusconnect.core.utils.toInitials



enum class AvatarShape { CIRCLE, ROUNDED }

/**
 *
 * Usage:
 *   // User avatar
 *   AppAvatar(
 *       entityId    = connection.userId,
 *       displayName = connection.fullName,
 *       imageUrl    = connection.avatarUrl,
 *       size        = 44.dp
 *   )
 *
 *   // Club logo
 *   AppAvatar(
 *       entityId    = club.clubId,
 *       displayName = club.name,
 *       imageUrl    = club.logoUrl,
 *       size        = 40.dp,
 *       shape       = AvatarShape.ROUNDED
 *   )
 */
@Composable
fun AppAvatar(
    entityId    : Int,
    displayName : String,
    imageUrl    : String?   = null,
    size        : Dp        = 40.dp,
    shape       : AvatarShape = AvatarShape.CIRCLE,
    showBorder  : Boolean   = false,
    borderWidth : Dp        = 2.dp,
    fontSize    : TextUnit  = (size.value * 0.35f).sp,
) {
    val colors   = avatarColorsFor(entityId)
    val initials = displayName.toInitials().ifBlank { "?" }
    val clipShape: Shape = when (shape) {
        AvatarShape.CIRCLE  -> CircleShape
        AvatarShape.ROUNDED -> RoundedCornerShape((size.value * 0.25f).dp)
    }

    val modifier = Modifier
        .size(size)
        .clip(clipShape)
        .then(
            if (showBorder)
                Modifier.border(borderWidth, colors.foreground, clipShape)
            else Modifier
        )

    if (!imageUrl.isNullOrBlank()) {
        // Image avatar
        AsyncImage(
            model             = imageUrl,
            contentDescription = displayName,
            contentScale      = ContentScale.Crop,
            modifier          = modifier,
        )
    } else {
        // Initials fallback
        Box(
            modifier         = modifier.background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = initials,
                fontSize   = fontSize,
                fontWeight = FontWeight.Bold,
                color      = colors.foreground,
            )
        }
    }
}