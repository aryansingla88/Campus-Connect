package com.example.campusconnect.feature.splash.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.campusconnect.core.session.AppAccessPolicy
import com.example.campusconnect.core.session.SessionManager
import com.example.campusconnect.feature.splash.SplashScreen

const val SPLASH_ROUTE = "splash"

fun NavGraphBuilder.splashNav(
    navController: NavController
) {
    composable(SPLASH_ROUTE) {

        SplashScreen(
            onNavigateToOfflineFallback = {
                navController.navigate("offline_fallback") {
                    popUpTo(SPLASH_ROUTE) { inclusive = true }
                    launchSingleTop = true
                }
            },

            onNavigateToHome = {

                val role = SessionManager.accountRole.value
                if (role == null) {
                    SessionManager.clearSession()
                    navController.navigate("login") {
                        popUpTo(SPLASH_ROUTE) { inclusive = true }
                    }
                    return@SplashScreen
                }
                navController.navigate(AppAccessPolicy.startRoute(role)) {
                    popUpTo(SPLASH_ROUTE) {
                        inclusive = true
                    }
                }
            },

            onNavigateToLogin = {

                navController.navigate("login") {
                    popUpTo(SPLASH_ROUTE) {
                        inclusive = true
                    }
                }
            }
        )
    }
}
