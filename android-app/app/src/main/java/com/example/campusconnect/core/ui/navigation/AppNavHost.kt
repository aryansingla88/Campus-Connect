package com.example.campusconnect.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.campusconnect.core.session.SessionManager
import com.example.campusconnect.core.session.AppAccessPolicy
import com.example.campusconnect.feature.auth.authNav
import com.example.campusconnect.feature.events.eventNav
import com.example.campusconnect.feature.registrations.RegistrationNav
import com.example.campusconnect.feature.map.mapNav
import com.example.campusconnect.feature.posts.navigation.POSTS_FEED_ROUTE
import com.example.campusconnect.feature.posts.navigation.postNav
import com.example.campusconnect.feature.profile.ProfileNav
import com.example.campusconnect.feature.profile.ProfileRoutes
import com.example.campusconnect.feature.settings.SettingsNav
import com.example.campusconnect.feature.splash.navigation.SPLASH_ROUTE
import com.example.campusconnect.feature.splash.navigation.splashNav
import com.example.campusconnect.feature.test.TestScreen2
import com.example.campusconnect.feature.profile.ui.components.PageBg
import com.example.campusconnect.feature.map.ui.components.controls.CloudTransitionOverlay
import com.example.campusconnect.feature.map.ui.components.controls.LocalCloudTransitionState
import com.example.campusconnect.feature.map.ui.components.controls.rememberCloudTransitionState

@Composable
fun AppNavHost() {

    val navController = rememberNavController()
    val cloudTransitionState = rememberCloudTransitionState()
    val role by SessionManager.accountRole.collectAsState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    var accessBlocked by remember { mutableStateOf(false) }

    LaunchedEffect(role, backStackEntry?.destination?.route) {
        val route = backStackEntry?.destination?.route ?: return@LaunchedEffect
        accessBlocked = !AppAccessPolicy.canAccessOptional(role, route)
        if (accessBlocked) {
            val safeRoute = role?.let(AppAccessPolicy::startRoute) ?: "login"
            if (navController.popBackStack(safeRoute, inclusive = false)) {
                accessBlocked = false
                return@LaunchedEffect
            }
            navController.popBackStack(route, inclusive = true)
            navController.navigate(safeRoute) { launchSingleTop = true }
        }
    }

    CompositionLocalProvider(LocalCloudTransitionState provides cloudTransitionState) {
        Box(Modifier.fillMaxSize()) {
            NavHost(
                modifier = Modifier.fillMaxSize(),
                navController = navController,
                startDestination = SPLASH_ROUTE
            ) {
        // Developer entry and the network-unavailable fallback share the same screen.
        composable("test2") { TestScreenRoute(navController, "test2") }
        composable("offline_fallback") { TestScreenRoute(navController, "offline_fallback") }

        // ---------------------------------------------------------
        // FEATURE NAV GRAPHS
        // ---------------------------------------------------------

        splashNav(navController)

        authNav(navController)

        postNav(navController)

        eventNav(navController)

        mapNav(navController)

        ProfileNav(navController)

        RegistrationNav(navController)

        SettingsNav(navController)
            }
            CloudTransitionOverlay(cloudTransitionState)
            if (accessBlocked) {
                Box(
                    Modifier.fillMaxSize().background(PageBg)
                )
            }
        }
    }
}

@Composable
private fun TestScreenRoute(navController: NavController, route: String) {
    TestScreen2(
        onPosts = { navController.navigate(POSTS_FEED_ROUTE) },
        onEvents = { navController.navigate("events_root") },
        onMap = { navController.navigate("map") },
        onProfile = { navController.navigate(ProfileRoutes.MY_PROFILE) },
        onSettings = { navController.navigate("settings_graph") },
        onLogout = {
            SessionManager.clearSession()
            navController.navigate("login") {
                popUpTo(route) { inclusive = true }
            }
        },
        onContinue = { navController.navigate("map") }
    )
}
