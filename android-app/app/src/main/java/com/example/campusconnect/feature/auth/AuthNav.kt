package com.example.campusconnect.feature.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.campusconnect.core.session.AppAccessPolicy
import com.example.campusconnect.core.session.SessionManager
import com.example.campusconnect.feature.auth.ui.LoginScreen
import com.example.campusconnect.feature.auth.ui.RegisterScreen

fun NavGraphBuilder.authNav(
    navController: NavController
) {

    // Login Screen Route
    composable("login") {

        LoginScreen(

            onServerUnavailable = {
                navController.navigate("offline_fallback") {
                    popUpTo("login") { inclusive = true }
                    launchSingleTop = true
                }
            },

            onLoginSuccess = {
                val role = SessionManager.accountRole.value
                if (role == null) {
                    SessionManager.clearSession()
                    return@LoginScreen
                }
                navController.navigate(AppAccessPolicy.startRoute(role)) {
                    popUpTo("login") { inclusive = true }
                    launchSingleTop = true
                }
            },

            onNavigateToRegister = {

                // future register screen
                navController.navigate("register") {
                }
            }
        )
    }

    // Temporary Register Route
    composable("register") {

        RegisterScreen(

            onRegisterSuccess = {
                navController.navigate("login")
            },

            onNavigateToLogin = {
                navController.navigate("login")
            }
        )
    }
}
