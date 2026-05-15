package com.example.campusconnect.feature.profile

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

fun NavGraphBuilder.ProfileNav(
    navController: NavController
) {

    composable("profile") {
        ProfileScreen()
    }
}