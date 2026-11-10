package com.example.campusconnect.core.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.campusconnect.core.network.ApiConfig
import com.example.campusconnect.core.utils.toInitials


object ApiUrlResolver {

    fun resolve(path: String?): String? {
        if (path.isNullOrBlank()) return null

        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path
        }

        return ApiConfig.BASE_URL.trimEnd('/') +
                "/" +
                path.trimStart('/')
    }
}

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
    entityId    : Int?,
    displayName : String,
    imageUrl    : String?   = null,
    size        : Dp        = 40.dp,
    shape       : AvatarShape = AvatarShape.CIRCLE,
    showBorder  : Boolean   = false,
    borderWidth : Dp        = 2.dp,
    fontSize    : TextUnit  = (size.value * 0.35f).sp,
    imageModel  : Any?      = null,
) {
    val colors   = avatarColorsFor(entityId ?: displayName.hashCode())
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

    val resolvedImageUrl = remember(imageUrl) { ApiUrlResolver.resolve(imageUrl) }
    val requestModel = imageModel ?: resolvedImageUrl

    if (requestModel != null) {
        SubcomposeAsyncImage(
            model              = requestModel,
            contentDescription = displayName,
            contentScale       = ContentScale.Crop,
            modifier           = modifier,
            loading            = { AvatarFallback(initials, colors, fontSize) },
            error              = { AvatarFallback(initials, colors, fontSize) },
            success            = { SubcomposeAsyncImageContent() },
        )
    } else {
        Box(
            modifier = modifier.background(colors.background),
            contentAlignment = Alignment.Center,
        ) { Text(initials, fontSize = fontSize, fontWeight = FontWeight.Bold, color = colors.foreground) }
    }
}

@Composable
private fun AvatarFallback(initials: String, colors: AvatarColors, fontSize: TextUnit) {
    Box(
        modifier = Modifier.fillMaxSize().background(colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, fontSize = fontSize, fontWeight = FontWeight.Bold, color = colors.foreground)
    }
}
