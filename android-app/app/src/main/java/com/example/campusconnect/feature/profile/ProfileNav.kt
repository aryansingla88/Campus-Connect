package com.example.campusconnect.feature.profile

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.campusconnect.feature.profile.ui.myprofile.MyProfileScreen
import com.example.campusconnect.feature.profile.ui.viewprofile.ViewProfileScreen

// ── Route constants ────────────────────────────────────────────────────────────
object ProfileRoutes {
    const val GRAPH        = "profile"
    const val MY_PROFILE   = "profile/me"
    const val VIEW_PROFILE = "profile/{userId}"

    fun viewProfile(userId: String) = "profile/$userId"
}

// ── Nav graph ──────────────────────────────────────────────────────────────────
fun NavGraphBuilder.ProfileNav(
    navController: NavController
) {
    navigation(
        startDestination = ProfileRoutes.MY_PROFILE,
        route            = ProfileRoutes.GRAPH
    ) {

        // ── My Profile ─────────────────────────────────────────────────────────
        composable(ProfileRoutes.MY_PROFILE) {
            MyProfileScreen(
                onBack        = { navController.popBackStack() },
                onSettings    = { navController.navigate("settings") },
                onEditProfile = { navController.navigate("edit_profile") }
            )
        }

        // ── View Another User's Profile ────────────────────────────────────────
        composable(
            route     = ProfileRoutes.VIEW_PROFILE,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
            ViewProfileScreen(
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}