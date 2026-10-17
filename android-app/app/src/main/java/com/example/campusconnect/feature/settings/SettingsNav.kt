package com.example.campusconnect.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.campusconnect.feature.profile.ProfileRoutes
import com.example.campusconnect.feature.settings.model.SettingsItemId
import com.example.campusconnect.feature.settings.ui.screen.ManageSocialLinksScreen
import com.example.campusconnect.feature.settings.ui.screen.NotificationsScreen
import com.example.campusconnect.feature.settings.ui.screen.ProfileVisibilityScreen
import com.example.campusconnect.feature.settings.ui.screen.SettingsScreen
import com.example.campusconnect.feature.settings.viewmodel.SettingsViewModel

// --- Route constants -----------------------------------------------------------------
object SettingsRoutes {
    const val GRAPH                 = "settings_graph"
    const val SETTINGS              = "settings"
    const val PROFILE_VISIBILITY    = "settings/profile_visibility"
    const val MANAGE_SOCIAL_LINKS   = "settings/manage_social_links"
    const val NOTIFICATIONS         = "settings/notifications"

    const val CHANGE_PASSWORD       = "settings/change_password"


    fun fullScreenRouteFor(id: SettingsItemId): String? {
        return when (id) {

            SettingsItemId.PROFILE_VISIBILITY ->
                PROFILE_VISIBILITY

            SettingsItemId.MANAGE_SOCIAL_LINKS ->
                MANAGE_SOCIAL_LINKS

            SettingsItemId.NOTIFICATIONS ->
                NOTIFICATIONS

            SettingsItemId.CHANGE_PASSWORD ->
                CHANGE_PASSWORD

            else ->
                null
        }
    }
}

// --- Shared Settings ViewModel
@Composable
private fun sharedSettingsViewModel(
    navController: NavController,
    backStackEntry: NavBackStackEntry
): SettingsViewModel {

    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(
            SettingsRoutes.GRAPH
        )
    }

    return viewModel(parentEntry)
}


// - Nav graph -----------------------------------------------------------------
// Sub-destinations that AREN'T built yet (Change Password, Manage Data, Privacy &
// Security, Help & FAQ, Contact Us, Terms & Policies, About) still resolve to a route
// string via SettingsDestinations so they can be registered elsewhere later — same
// deferral pattern ProfileNav uses for its own "settings" route.
fun NavGraphBuilder.SettingsNav(
    navController: NavController
) {
    navigation(
        startDestination = SettingsRoutes.SETTINGS,
        route            = SettingsRoutes.GRAPH
    ) {


        composable(SettingsRoutes.SETTINGS) { backStackEntry ->
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateFullScreen = { itemId ->

                    if (itemId == SettingsItemId.EDIT_PROFILE) {
                        navController.navigate(
                            ProfileRoutes.myProfile(edit = true)
                        )
                    } else {
                        SettingsRoutes.fullScreenRouteFor(itemId)?.let { route ->
                            navController.navigate(route)
                        }
                    }
                },
                onNavigate = { route -> navController.navigate(route) },
                onConfirmDeleteAccount = {
                    // TODO
                    // Actual delete flow (API call, sign-out, navigation) isn't one of
                    // the screens we were given — left for the app to wire in.
                },
                vm = sharedSettingsViewModel(navController, backStackEntry)
            )
        }

        composable(SettingsRoutes.PROFILE_VISIBILITY) { backStackEntry ->
            ProfileVisibilityScreen(
                onBack = { navController.popBackStack() },
                onSaveChanges = { navController.popBackStack() },
                vm = sharedSettingsViewModel(navController, backStackEntry)
            )
        }

        composable(SettingsRoutes.MANAGE_SOCIAL_LINKS) { backStackEntry ->
            ManageSocialLinksScreen(
                onBack = { navController.popBackStack() },
                onSaveChanges = { navController.popBackStack() },
                vm = sharedSettingsViewModel(navController, backStackEntry)
            )
        }

        composable(SettingsRoutes.NOTIFICATIONS) { backStackEntry ->
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                onQuietHoursClick = {
                    // Not one of the screens we were given — left as a no-op for now.
                },
                vm = sharedSettingsViewModel(navController, backStackEntry)
            )
        }

//        composable(
//            SettingsRoutes.CHANGE_PASSWORD
//        ) {
//
//            ChangePasswordScreen(
//                onBack = {
//                    navController.popBackStack()
//                }
//            )
//        }

    }
}
