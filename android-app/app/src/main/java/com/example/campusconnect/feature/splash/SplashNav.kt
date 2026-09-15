package com.example.campusconnect.feature.splash.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.campusconnect.feature.splash.SplashScreen

const val SPLASH_ROUTE = "splash"

fun NavGraphBuilder.splashNav(
    navController: NavController
) {
    composable(SPLASH_ROUTE) {

        SplashScreen(
            onNavigateToHome = {

                // TEMP: test2 is standing in for Home
                navController.navigate("test2") {
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