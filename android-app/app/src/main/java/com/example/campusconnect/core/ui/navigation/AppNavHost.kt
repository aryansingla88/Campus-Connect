package com.example.campusconnect.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.campusconnect.feature.test.TestScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "test"
    ) {
        composable("test") {
            TestScreen()
        }
    }
}