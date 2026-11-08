package com.example.campusconnect.feature.registrations.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.registrations.ui.components.OrangeBorder
import com.example.campusconnect.feature.registrations.ui.components.OrangeLight
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.TextPrimary
import com.example.campusconnect.feature.registrations.ui.components.TextSecondary

private val SkeletonBase = Color(0xFFF1E6DD)
private val SkeletonHighlight = Color(0xFFFFF8F3)

/** Animated shimmer brush that sweeps across whatever it is applied to. */
@Composable
internal fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = -600f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerOffset",
    )
    return Brush.linearGradient(
        colors = listOf(SkeletonBase, SkeletonHighlight, SkeletonBase),
        start = androidx.compose.ui.geometry.Offset(offset, 0f),
        end = androidx.compose.ui.geometry.Offset(offset + 500f, 220f),
    )
}

@Composable
internal fun SkeletonBlock(
    brush: Brush,
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    corner: Dp = 8.dp,
) {
    Box(modifier.height(height).clip(RoundedCornerShape(corner)).background(brush))
}

/** Placeholder that mirrors [StepHeader] (icon tile + title + subtitle). */
@Composable
internal fun StepHeaderSkeleton(brush: Brush) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        SkeletonBlock(brush, Modifier.size(56.dp), height = 56.dp, corner = 16.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SkeletonBlock(brush, Modifier.fillMaxWidth(0.6f), height = 22.dp)
            SkeletonBlock(brush, Modifier.fillMaxWidth(0.9f), height = 14.dp)
        }
    }
}

/** Placeholder that mirrors a label + input field. */
@Composable
internal fun FieldSkeleton(brush: Brush, tall: Boolean = false, labelWidth: Float = 0.45f) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SkeletonBlock(brush, Modifier.fillMaxWidth(labelWidth), height = 16.dp)
        SkeletonBlock(brush, Modifier.fillMaxWidth(), height = if (tall) 120.dp else 56.dp, corner = 14.dp)
    }
}

/**
 * Full-page loading state that uses the real step chrome (top bar, progress, header shape) and
 * shimmering placeholders where the content will appear — so the layout doesn't jump when data
 * arrives. If [error] is set it turns into an inline error card with a retry button.
 */
@Composable
internal fun FlowLoadingScreen(
    step: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Event Registration",
    error: String? = null,
    errorTitle: String = "Something went wrong",
    onRetry: (() -> Unit)? = null,
    loadingLabel: String = "Loading registration form",
) {
    Scaffold(
        modifier = modifier.flowPageBackground(),
        containerColor = Color.Transparent,
        topBar = { FlowTopBar(title = title, onBack = onBack, step = step) },
    ) { padding ->
        if (error == null) {
            val brush = shimmerBrush()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState(), enabled = false)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .semantics { contentDescription = loadingLabel },
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                StepHeaderSkeleton(brush)
                FieldSkeleton(brush)
                FieldSkeleton(brush, tall = true, labelWidth = 0.6f)
                FieldSkeleton(brush, labelWidth = 0.35f)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    Modifier.size(88.dp).clip(RoundedCornerShape(28.dp)).background(OrangeLight),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(44.dp))
                }
                Spacer(Modifier.height(24.dp))
                Text(errorTitle, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(error, fontSize = 15.sp, lineHeight = 21.sp, color = TextSecondary, textAlign = TextAlign.Center)
                if (onRetry != null) {
                    Spacer(Modifier.height(24.dp))
                    FlowPrimaryButton(
                        label = "Try again",
                        icon = Icons.Outlined.Refresh,
                        onClick = onRetry,
                        modifier = Modifier.width(200.dp),
                    )
                }
            }
        }
    }
}

/** Skeleton rows for the team-portal list (replaces the lone spinner). */
@Composable
internal fun TeamListSkeleton(modifier: Modifier = Modifier, rows: Int = 4) {
    val brush = shimmerBrush()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(PaddingValues(horizontal = 16.dp, vertical = 4.dp))
            .semantics { contentDescription = "Loading teams" },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(rows) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SkeletonBlock(brush, Modifier.size(48.dp), height = 48.dp, corner = 24.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonBlock(brush, Modifier.fillMaxWidth(0.55f), height = 16.dp)
                    SkeletonBlock(brush, Modifier.fillMaxWidth(0.8f), height = 12.dp)
                }
                SkeletonBlock(brush, Modifier.width(72.dp), height = 34.dp, corner = 17.dp)
            }
        }
    }
}

/** Shown while the final submit call is in flight: real indeterminate progress, no back action. */
@Composable
fun SubmittingRegistrationScreen() {
    Scaffold(
        modifier = Modifier.flowPageBackground(),
        containerColor = Color.Transparent,
        topBar = { FlowTopBar(title = "Event Registration", onBack = {}, step = 3, backEnabled = false) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp)
                .semantics { contentDescription = "Submitting your registration" },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier.size(96.dp).clip(RoundedCornerShape(30.dp)).background(OrangeLight),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(52.dp),
                    strokeWidth = 5.dp,
                    color = OrangePrimary,
                    trackColor = OrangeBorder.copy(alpha = 0.5f),
                )
            }
            Spacer(Modifier.height(28.dp))
            Text(
                "Submitting your registration",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Please keep this screen open while we finalize everything.",
                fontSize = 15.sp,
                lineHeight = 21.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(0.7f).height(6.dp).clip(RoundedCornerShape(50)),
                color = OrangePrimary,
                trackColor = OrangeBorder.copy(alpha = 0.45f),
            )
        }
    }
}
