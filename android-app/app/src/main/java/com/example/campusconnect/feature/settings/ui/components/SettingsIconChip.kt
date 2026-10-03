package com.example.campusconnect.feature.settings.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Small rounded-square icon badge used as the leading element of a settings row -----
@Composable
fun SettingsIconChip(
    icon: ImageVector,
    tint: Color,
    background: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = background,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

// --- Text/glyph variant — used where there's no clean vector icon (language script, ---
// --- a social platform's initials) but we still want the same chip footprint. ---------
@Composable
fun SettingsGlyphChip(
    glyph: String,
    tint: Color,
    background: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    circular: Boolean = false
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = background,
                shape = if (circular) CircleShape else RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = glyph,
            color = tint,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
