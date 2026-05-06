package com.example.campusconnect.feature.events

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.eventNav(
    navController: NavController
) {

    composable("events") {
        EventScreen()
    }
}