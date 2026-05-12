package com.example.campusconnect.feature.map

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.mapNav(
    navController: NavController
) {
    composable("map") {
        MapScreen()
    }
}