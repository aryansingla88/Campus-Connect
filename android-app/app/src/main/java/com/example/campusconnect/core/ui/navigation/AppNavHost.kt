package com.example.campusconnect.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.campusconnect.feature.test.TestScreen
import com.example.campusconnect.feature.auth.authNav
//import com.example.campusconnect.feature.posts.postNav
//import com.example.campusconnect.feature.events.eventNav
//import com.example.campusconnect.feature.map.mapNav
//import com.example.campusconnect.feature.profile.profileNav

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "test"   //  TEMP
    ) {

        composable("test") {
            TestScreen(
                onAuth = { navController.navigate("auth") },
                onPosts = { navController.navigate("posts") },
                onEvents = { navController.navigate("events") },
                onMap = { navController.navigate("map") },
                onProfile = { navController.navigate("profile") }
            )
        }

        authNav(navController)
//        postNav(navController)
//        eventNav(navController)
//        mapNav(navController)
//        profileNav(navController)
    }
}