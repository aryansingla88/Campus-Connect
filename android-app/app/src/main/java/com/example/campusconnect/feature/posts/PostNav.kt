package com.example.campusconnect.feature.posts.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.campusconnect.feature.posts.screens.GeneralFeedScreen
import com.example.campusconnect.feature.posts.screens.PostDetailScreen

const val POSTS_FEED_ROUTE = "posts"
const val EVENTS_FEED_ROUTE = "posts/events"
const val POST_DETAIL_ROUTE = "post_detail/{postId}"

fun NavGraphBuilder.postNav(
    navController: NavController
) {

    // ---------------------------------------------------------
    // General Feed
    // ---------------------------------------------------------

    composable(POSTS_FEED_ROUTE) {

        GeneralFeedScreen(
            initialTab = 0,
            onPostClick = { postId ->
                navController.navigate(
                    "post_detail/$postId"
                )
            }
        )
    }

    // ---------------------------------------------------------
    // Event Feed
    // ---------------------------------------------------------

    composable(EVENTS_FEED_ROUTE) {

        GeneralFeedScreen(
            initialTab = 1,
            onPostClick = { postId ->
                navController.navigate(
                    "post_detail/$postId"
                )
            }
        )
    }

    // ---------------------------------------------------------
    // Post Detail
    // ---------------------------------------------------------

    composable(POST_DETAIL_ROUTE) { backStackEntry ->

        val postId = backStackEntry.arguments
            ?.getString("postId")
            ?.toIntOrNull()

        if (postId != null) {

            PostDetailScreen(
                postId = postId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}