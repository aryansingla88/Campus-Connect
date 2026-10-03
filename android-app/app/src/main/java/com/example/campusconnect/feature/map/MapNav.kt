package com.example.campusconnect.feature.map

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.campusconnect.feature.events.EVENTS_POSTER_VIEW
import com.example.campusconnect.feature.posts.navigation.EVENTS_FEED_ROUTE
import com.example.campusconnect.feature.posts.navigation.POSTS_FEED_ROUTE
import com.example.campusconnect.feature.profile.ProfileRoutes

fun NavGraphBuilder.mapNav(
    navController: NavController
) {
    composable("map") {

        MapScreen(

            // -------------------------------
            // Top-left profile
            // -------------------------------
            onProfileClick = {
                navController.navigate(
                    ProfileRoutes.MY_PROFILE
                )
            },

            // -------------------------------
            // Top-right settings
            // -------------------------------
            onSettingsClick = {
                navController.navigate(
                    "settings_graph"
                )
            },

            // -------------------------------
            // Home → General Posts
            // -------------------------------
            onHomePostsClick = {
                navController.navigate(
                    POSTS_FEED_ROUTE
                )
            },

            // -------------------------------
            // Events → Event Posts
            // -------------------------------
            onEventPostsClick = {
                navController.navigate(
                    EVENTS_FEED_ROUTE
                )
            },

            // -------------------------------
            // Events mode → Events feature
            // -------------------------------
            onEventFeatureClick = {
                navController.navigate(
                    "events_root"
                )
            },

            // -------------------------------
            // Poster → Events View Mode
            // -------------------------------
            onPosterClick = {
                navController.navigate(
                    EVENTS_POSTER_VIEW
                )
            }
        )
    }
}