package com.example.campusconnect.feature.events.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.model.Event

// ─── Colour helpers ───────────────────────────────────────────────────────────

/** Maps an event category to a marker tint colour. Extend as needed. */
fun categoryColor(category: String): Color = when (category.lowercase()) {
    "art", "arts"        -> Color(0xFF7B3FC4)
    "music"              -> Color(0xFF1D9E75)
    "food", "carnival"   -> Color(0xFF1A7FC1)
    "sports", "sport"    -> Color(0xFFE89B10)
    "technology", "tech" -> Color(0xFFFF6F00)
    else                 -> Color(0xFFFF6F00)
}

// ─── EventMarker ─────────────────────────────────────────────────────────────

/**
 * A map marker for a single event.
 *
 * When [isActive] is true the marker scales up ~1.45× with an orange drop-shadow
 * ring underneath, matching the design reference.
 *
 * Place this inside a [Box] using [Modifier.offset] to position it at the correct
 * (xRatio * containerWidth, yRatio * containerHeight) pixel coordinates.
 */
@Composable
fun EventMarker(
    event: Event,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Smooth scale animation
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.45f else 1f,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "markerScale"
    )

    // Pulse ring alpha
    val pulseAlpha by animateFloatAsState(
        targetValue = if (isActive) 0.4f else 0.2f,
        animationSpec = tween(durationMillis = 250),
        label = "pulseAlpha"
    )

    val markerColor = categoryColor(event.category)

    Column(
        modifier = modifier
            .scale(scale)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── Pin icon ──────────────────────────────────────────────────────────
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = event.title,
                tint = markerColor,
                modifier = Modifier.size(40.dp)
            )
        }

        // ── Label chip ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Text(
                text = event.title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2A2A2A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // ── Pulse ring (active glow) ───────────────────────────────────────
        Spacer(Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .width(if (isActive) 34.dp else 22.dp)
                .height(if (isActive) 13.dp else 9.dp)
                .background(
                    markerColor.copy(alpha = pulseAlpha),
                    RoundedCornerShape(50)
                )
        )
    }
}