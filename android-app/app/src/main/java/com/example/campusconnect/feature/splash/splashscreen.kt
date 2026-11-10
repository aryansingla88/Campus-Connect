package com.example.campusconnect.feature.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Existing splash entry point. Keeps the current session-routing callbacks,
 * while delegating presentation and animation to CampusConnectSplashScreen.
 */
@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToOfflineFallback: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val destination by viewModel.destination.collectAsState()

    // Begin auth/session validation immediately; do not wait for the animation.
    LaunchedEffect(Unit) {
        viewModel.startSplash()
    }

    CampusConnectSplashScreen(
        appReady = destination != null,
        onFinished = {
            when (destination) {
                SplashDestination.Home -> onNavigateToHome()
                SplashDestination.Login -> onNavigateToLogin()
                SplashDestination.OfflineFallback -> onNavigateToOfflineFallback()
                null -> Unit
            }
        }
    )
}
