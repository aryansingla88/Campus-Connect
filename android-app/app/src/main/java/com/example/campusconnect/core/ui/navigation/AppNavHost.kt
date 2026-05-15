package com.example.campusconnect.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.campusconnect.feature.auth.LoginScreen
import com.example.campusconnect.feature.splash.SplashScreen
import com.example.campusconnect.feature.splash.SplashDestination
import com.example.campusconnect.feature.test.TestScreen
import com.example.campusconnect.feature.auth.authNav
//import com.example.campusconnect.feature.posts.postNav
import com.example.campusconnect.feature.events.eventNav
import com.example.campusconnect.feature.map.mapNav
import com.example.campusconnect.feature.profile.ProfileNav

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "test"
    ) {

        // Splash Screen
        composable("splash") {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate("main")
                },
                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }

        composable("test") {
            TestScreen(
                onAuth = { navController.navigate("auth") },
                onPosts = { navController.navigate("posts") },
                onEvents = { navController.navigate("events_root") },
                onMap = { navController.navigate("map") },
                onProfile = { navController.navigate("profile")},
                onSplash={navController.navigate("splash")}
            )
        }

        authNav(navController)
//        postNav(navController)
        eventNav(navController)
        mapNav(navController)
        ProfileNav(navController)
    }
}