package com.example.campusconnect.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.campusconnect.feature.auth.LoginScreen
import com.example.campusconnect.feature.splash.SplashScreen
import com.example.campusconnect.feature.splash.SplashDestination
import com.example.campusconnect.feature.test.TestScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        // Splash Screen
        composable("splash") {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate("test") {   // TEMP: using test as main
                        popUpTo("splash") { inclusive = true }
                    //popUpTo("splash") function removes splash screen from the backstack
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("test") {   // TEMP: same for now
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // Temporary main screen
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register") // later
                }
            )
        }
    }
}