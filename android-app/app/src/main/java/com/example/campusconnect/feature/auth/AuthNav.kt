package com.example.campusconnect.feature.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.campusconnect.feature.test.TestScreen

fun NavGraphBuilder.authNav(
    navController: NavController
) {

    // Login Screen Route
    composable("login") {

        LoginScreen(

            onLoginSuccess = {

                // TEMP: navigate to test/main screen
                navController.navigate("test") {

                    // remove login from backstack
                    popUpTo("login") {
                        inclusive = true
                    }
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