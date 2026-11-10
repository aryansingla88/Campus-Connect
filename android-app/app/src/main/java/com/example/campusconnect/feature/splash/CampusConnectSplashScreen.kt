package com.example.campusconnect.feature.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.campusconnect.R

private val SplashCanvas = Color(0xFFF7F8F5)
private val Ink = Color(0xFF18343A)
private val MutedInk = Color(0xFF718187)
private val Teal = Color(0xFF4C9296)
private val Track = Color(0xFFE5ECE9)

/**
 * Branded launch screen. The progress cue remains visible until both startup
 * routing and the Lottie animation have completed.
 */
@Composable
fun CampusConnectSplashScreen(
    appReady: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    minimumVisibleMillis: Long = 900L
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.campus_splash)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = 1.0f,
        restartOnPlay = false
    )

    var animationFinished by remember { mutableStateOf(false) }
    var minimumTimePassed by remember { mutableStateOf(false) }
    var handoffStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(minimumVisibleMillis)
        minimumTimePassed = true
    }

    LaunchedEffect(progress, composition) {
        if (composition != null && progress >= 0.999f) {
            animationFinished = true
        }
    }

    LaunchedEffect(appReady, animationFinished, minimumTimePassed) {
        if (appReady && animationFinished && minimumTimePassed && !handoffStarted) {
            handoffStarted = true
            onFinished()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SplashCanvas),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(0.72f))

            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(320.dp)
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = "CampusConnect",
                color = Ink,
                fontSize = 29.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.8).sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Your campus. Your community.",
                color = MutedInk,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.15.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(22.dp))

            if (!appReady || !animationFinished) {
                SlidingSplashLoader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 104.dp)
                )
            } else {
                Spacer(Modifier.height(3.dp))
            }

            Spacer(Modifier.weight(0.68f))
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun SlidingSplashLoader(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "splash_loader")
    val travel by transition.animateFloat(
        initialValue = -0.34f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1050, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "splash_loader_travel"
    )

    Canvas(
        modifier = modifier
            .height(3.dp)
            .clip(RoundedCornerShape(50))
    ) {
        drawRoundRect(
            color = Track,
            cornerRadius = CornerRadius(size.height / 2f)
        )

        val segmentWidth = size.width * 0.34f
        val left = travel * (size.width + segmentWidth) - segmentWidth
        drawRoundRect(
            color = Teal,
            topLeft = Offset(left, 0f),
            size = Size(segmentWidth, size.height),
            cornerRadius = CornerRadius(size.height / 2f)
        )
    }
}
