package com.example.campusconnect.feature.profile

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.campusconnect.feature.profile.ui.screen.MyProfileScreen
import com.example.campusconnect.feature.profile.ui.screen.ViewProfileScreen

// --- Route constants -----------------------------------------------------------------
object ProfileRoutes {
    const val GRAPH        = "profile"
    const val MY_PROFILE   = "profile/me"
    const val VIEW_PROFILE = "profile/{userId}"

    fun viewProfile(userId: Int) = "profile/$userId"

    fun myProfile(edit: Boolean = false): String =
        if (edit) "profile/me?edit=true" else MY_PROFILE
}

// - Nav graph -----------------------------------------------------------------
fun NavGraphBuilder.ProfileNav(
    navController: NavController
) {
    navigation(
        startDestination = ProfileRoutes.MY_PROFILE,
        route            = ProfileRoutes.GRAPH
    ) {

        // -- My Profile -----------------------------------------------------------------
        composable(
            route = "profile/me?edit={edit}",
            arguments = listOf(
                navArgument("edit") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            ),
            enterTransition = {
                fadeIn(tween(240))
            },
            exitTransition = {
                fadeOut(tween(200))
            },
            popEnterTransition = {
                fadeIn(tween(200))
            },
            popExitTransition = {
                fadeOut(tween(240))
            }
        ) { backStackEntry ->

            val editMode =
                backStackEntry.arguments?.getBoolean("edit") ?: false

            MyProfileScreen(
                initialEditMode = editMode,
                onBack = { navController.popBackStack() },
                onSettings = { navController.navigate("settings") },
                onNavigateToProfile = { userId ->
                    navController.navigate(ProfileRoutes.viewProfile(userId))
                }
            )
        }

        // -- View Another User's Profile -------------------------------------------
        composable(
            route     = ProfileRoutes.VIEW_PROFILE,
            arguments = listOf(navArgument("userId") {
                    type = NavType.IntType
                }),
            enterTransition = {
                fadeIn(tween(240))
            },
            exitTransition = {
                fadeOut(tween(200))
            },
            popEnterTransition = {
                fadeIn(tween(200))
            },
            popExitTransition = {
                fadeOut(tween(240))
            }
        ) { backStackEntry ->
            val userId =
                backStackEntry.arguments?.getInt("userId")
                    ?: return@composable

            ViewProfileScreen(
                userId = userId,
                onBack = { navController.popBackStack() },
                onNavigateToProfile = { memberId ->
                    navController.navigate(ProfileRoutes.viewProfile(memberId))
                }
            )
        }
    }
}
