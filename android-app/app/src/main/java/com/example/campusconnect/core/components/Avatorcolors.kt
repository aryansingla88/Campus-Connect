package com.example.campusconnect.core.components

import androidx.compose.ui.graphics.Color

/**
 * Stable avatar color pair — background + foreground.
 * Generated from entity id, never stored, never sent by backend.
 */
data class AvatarColors(
    val background : Color,
    val foreground : Color,
)

private val AVATAR_PALETTE = listOf(
    AvatarColors(Color(0xFFFEF0E6), Color(0xFFB45309)),  // warm orange
    AvatarColors(Color(0xFFE6F1FB), Color(0xFF0C447C)),  // blue
    AvatarColors(Color(0xFFE1F5EE), Color(0xFF085041)),  // green
    AvatarColors(Color(0xFFEEEDFE), Color(0xFF3C3489)),  // purple
    AvatarColors(Color(0xFFFFF4D9), Color(0xFF8C6A00)),  // yellow
    AvatarColors(Color(0xFFFDE8F0), Color(0xFF9B1B5A)),  // pink
    AvatarColors(Color(0xFFE8F5E9), Color(0xFF1B5E20)),  // dark green
    AvatarColors(Color(0xFFE3F2FD), Color(0xFF0D47A1)),  // deep blue
    AvatarColors(Color(0xFFFCE4EC), Color(0xFF880E4F)),  // deep pink
    AvatarColors(Color(0xFFF3E5F5), Color(0xFF4A148C)),  // deep purple
    AvatarColors(Color(0xFFE0F7FA), Color(0xFF006064)),  // teal
    AvatarColors(Color(0xFFFFF8E1), Color(0xFF6D4C00)),  // amber
)


fun avatarColorsFor(entityId: Int): AvatarColors {
    val index = (entityId.hashCode() and Int.MAX_VALUE) % AVATAR_PALETTE.size
    return AVATAR_PALETTE[index]
}