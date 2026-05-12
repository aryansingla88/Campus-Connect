package com.example.campusconnect.feature.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.example.campusconnect.R

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    /*
    _destination.value = SplashDestination.Login
↓
destination updates
↓
collectAsState notices change
↓
Compose recomposes
↓
LaunchedEffect(destination) re-runs
↓
when(destination) matches Login
↓
onNavigateToLogin()
↓
navController.navigate(...)
     */
    val destination by viewModel.destination.collectAsState()


    // Lottie animation composition
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.campus)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    var showText by remember { mutableStateOf(false) }

    // Trigger text after animation ends
    LaunchedEffect(progress) {
        if (progress == 1f) {
            showText = true
            viewModel.startSplash()
        }
    }

    // Navigation trigger
    LaunchedEffect(destination) {
        when (destination) {
            SplashDestination.Main -> onNavigateToMain()
            SplashDestination.Login -> onNavigateToLogin()
            null -> {}
        }
    }

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(300.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            AnimatedVisibility(
                visible = showText,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                Text(
                    text = "Campus Connect",
                    fontSize = 24.sp
                )
            }
        }
    }
}