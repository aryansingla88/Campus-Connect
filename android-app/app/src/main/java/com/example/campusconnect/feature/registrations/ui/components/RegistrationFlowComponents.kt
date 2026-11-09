package com.example.campusconnect.feature.registrations.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.registrations.model.USER_REGISTRATION_TOTAL_STEPS
internal val FlowDanger = Color(0xFFD32F2F)
internal val FlowFieldBorder = Color(0xFFE2E4E8)
internal val FlowDisabledBg = Color(0xFFF1F1F3)

// ── Page chrome ───────────────────────────────────────────────────────────────

internal val FlowIconTint = Color(0xFF3C4043)
private val FlowSecondaryBg = Color(0xFFEFEFF2)
private val OrangeGlow = Color(0xFFFF8A2B)

/** Soft warm page background shared by every step. Use with a transparent Scaffold container. */
internal val FlowPageBrush = Brush.verticalGradient(
    listOf(Color(0xFFFFF1E6), Color(0xFFFFF9F5), Color(0xFFFFFFFF)),
)

internal fun Modifier.flowPageBackground(): Modifier = background(FlowPageBrush)

internal fun flowPrimaryBrush(): Brush = Brush.verticalGradient(listOf(lerp(OrangePrimary, OrangeGlow, 0.45f), OrangePrimary))

@Composable
internal fun FlowTopBar(
    title: String,
    onBack: () -> Unit,
    step: Int? = null,
    totalSteps: Int = USER_REGISTRATION_TOTAL_STEPS,
    backEnabled: Boolean = true,
) {
    Column(Modifier.statusBarsPadding().fillMaxWidth().padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, enabled = backEnabled) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        if (step != null) {
            val progress by animateFloatAsState((step / totalSteps.toFloat()).coerceIn(0f, 1f), label = "stepProgress")
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(OrangeBorder.copy(alpha = 0.45f)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(flowPrimaryBrush()),
                    )
                }
                Text(
                    "$step / $totalSteps",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OrangePrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(OrangeLight)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}

/** Icon tile + title + subtitle used at the top of each step. */
@Composable
internal fun StepHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    iconBackground: Color = OrangeLight,
    iconTint: Color = OrangePrimary,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtitle, fontSize = 15.sp, lineHeight = 21.sp, color = TextSecondary)
        }
    }
}

// ── Bottom action bar ─────────────────────────────────────────────────────────

/** Primary "Next" bar. Pass [onBack] to show the grey Back button beside it (steps 2 and 3). */
@Composable
internal fun NextBottomBar(
    onClick: () -> Unit,
    enabled: Boolean = true,
    label: String = "Next",
    loading: Boolean = false,
    onBack: (() -> Unit)? = null,
) {
    FlowActionBar(
        primaryLabel = label,
        onPrimary = onClick,
        enabled = enabled,
        loading = loading,
        onBack = onBack,
    )
}

@Composable
internal fun FlowActionBar(
    primaryLabel: String,
    onPrimary: () -> Unit,
    enabled: Boolean = true,
    loading: Boolean = false,
    primaryIcon: ImageVector = Icons.Outlined.ArrowForward,
    iconBeforeLabel: Boolean = false,
    onBack: (() -> Unit)? = null,
    backEnabled: Boolean = true,
) {
    RegistrationBottomBar {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (onBack != null) {
                FlowSecondaryButton("Back", onBack, backEnabled, Modifier.weight(0.8f))
            }
            FlowPrimaryButton(
                label = primaryLabel,
                icon = primaryIcon,
                iconBeforeLabel = iconBeforeLabel,
                onClick = onPrimary,
                enabled = enabled,
                loading = loading,
                modifier = Modifier.weight(if (onBack != null) 1.5f else 1f),
            )
        }
    }
}

@Composable
internal fun FlowPrimaryButton(
    label: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    iconBeforeLabel: Boolean = false,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(shape)
            .background(if (enabled) flowPrimaryBrush() else SolidColor(OrangePrimary.copy(alpha = 0.38f)))
            .clickable(enabled = enabled && !loading, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            } else if (icon != null && iconBeforeLabel) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Text(label, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            if (!loading && icon != null && !iconBeforeLabel) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
internal fun FlowSecondaryButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(FlowSecondaryBg)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                Icons.Outlined.ArrowBack, contentDescription = null,
                tint = if (enabled) TextPrimary else TextSecondary, modifier = Modifier.size(20.dp),
            )
            Text(
                label, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                color = if (enabled) TextPrimary else TextSecondary,
            )
        }
    }
}
