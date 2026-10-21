package com.example.campusconnect.feature.registrations.userflow.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.ui.components.OrangeLight
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.TextPrimary
import com.example.campusconnect.feature.registrations.ui.components.TextSecondary

/** Shared animated loading/status presentation for participant registration steps. */
@Composable
internal fun RegistrationLoadingPresentation(
    title: String,
    message: String,
    icon: ImageVector,
    isLoading: Boolean = true,
    modifier: Modifier = Modifier,
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    val transition = rememberInfiniteTransition(label = "registrationStatusPulse")
    val scale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "registrationStatusScale",
    )

    Column(
        modifier = modifier.fillMaxSize().background(CardBg).padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(140.dp).scale(scale).background(OrangeLight.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(108.dp),
                    strokeWidth = 5.dp,
                    color = OrangePrimary,
                    trackColor = Color.White.copy(alpha = 0.65f),
                )
            }
            Icon(icon, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.height(28.dp))
        Text(title, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = TextPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(message, fontSize = 14.sp, lineHeight = 20.sp, color = TextSecondary, textAlign = TextAlign.Center)
        actions()
    }
}
