package com.example.campusconnect.feature.events

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.composable
import androidx.navigation.NavController


fun NavGraphBuilder.eventNav(navController: NavController) {

    navigation(
        startDestination = "events_main",
        route = "events_root"
    ) {

        composable("events_main") {
            EventScreen()
        }

   //     composable("event_list") {
   //         EventListScreen()
    //    }
    }
}