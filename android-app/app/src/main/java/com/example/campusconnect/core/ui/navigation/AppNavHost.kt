package com.example.campusconnect.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.campusconnect.core.session.SessionManager
import com.example.campusconnect.feature.auth.authNav
import com.example.campusconnect.feature.events.eventNav
import com.example.campusconnect.feature.events.registrations.RegisterationNav
import com.example.campusconnect.feature.events.registrations.navigateToFormBuilder
import com.example.campusconnect.feature.map.mapNav
import com.example.campusconnect.feature.posts.navigation.POSTS_FEED_ROUTE
import com.example.campusconnect.feature.posts.navigation.postNav
import com.example.campusconnect.feature.profile.ProfileNav
import com.example.campusconnect.feature.profile.ProfileRoutes
import com.example.campusconnect.feature.splash.navigation.SPLASH_ROUTE
import com.example.campusconnect.feature.splash.navigation.splashNav
import com.example.campusconnect.feature.test.TestScreen

@Composable
fun AppNavHost() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "test"
    ) {

        // ---------------------------------------------------------
        // TESTING SCREEN 1
        // ---------------------------------------------------------

        composable("test") {

            TestScreen(
                onPosts = {
                    navController.navigate(POSTS_FEED_ROUTE)
                },

                onEvents = {
                    navController.navigate("events_root")
                },

                onMap = {
                    navController.navigate("map")
                },

                onProfile = {
                    navController.navigate(ProfileRoutes.MY_PROFILE)
                },

                onSplash = {
                    navController.navigate(SPLASH_ROUTE)
                },

                onFormBuilder = {
                    navController.navigateToFormBuilder(1)
                }
            )
        }

        // ---------------------------------------------------------
        // TESTING SCREEN 2
        // ---------------------------------------------------------

        composable("test2") {

            TestScreen(
                onPosts = {
                    navController.navigate(POSTS_FEED_ROUTE)
                },

                onEvents = {
                    navController.navigate("events_root")
                },

                onMap = {
                    navController.navigate("map")
                },

                onProfile = {
                    navController.navigate(ProfileRoutes.MY_PROFILE)
                },

                onFormBuilder = {
                    navController.navigateToFormBuilder(1)
                },

                onLogout = {

                    SessionManager.clearSession()

                    navController.navigate("login") {
                        popUpTo("test2") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // ---------------------------------------------------------
        // FEATURE NAV GRAPHS
        // ---------------------------------------------------------

        splashNav(navController)

        authNav(navController)

        postNav(navController)

        eventNav(navController)

        mapNav(navController)

        ProfileNav(navController)

        RegisterationNav(navController)
    }
}