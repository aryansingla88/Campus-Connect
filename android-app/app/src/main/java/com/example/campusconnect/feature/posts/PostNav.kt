package com.example.campusconnect.feature.posts.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.campusconnect.feature.posts.screens.CreatePostScreen
import com.example.campusconnect.feature.posts.screens.GeneralFeedScreen
import com.example.campusconnect.feature.posts.screens.PostDetailScreen

const val POSTS_FEED_ROUTE = "posts"
const val EVENTS_FEED_ROUTE = "posts/events"
const val POST_DETAIL_ROUTE = "post_detail/{postId}"
const val CREATE_POST_ROUTE = "create_post/{postType}"
private const val EDIT_POST_ROUTE = "edit_post/{postId}"

private const val CREATE_POST_ROUTE_BASE = "create_post"
private const val POST_REFRESH_KEY = "post_refresh"

fun NavGraphBuilder.postNav(navController: NavController) {
    feedDestination(
        route = POSTS_FEED_ROUTE,
        initialTab = 0,
        postType = "GENERAL",
        navController = navController
    )

    feedDestination(
        route = EVENTS_FEED_ROUTE,
        initialTab = 1,
        postType = "EVENTS",
        navController = navController
    )

    composable(
        route = CREATE_POST_ROUTE,
        arguments = listOf(
            navArgument("postType") {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val initialPostType =
            backStackEntry.arguments?.getString("postType") ?: "GENERAL"

        CreatePostScreen(
            initialPostType = initialPostType,
            onBackClick = { navController.popBackStack() },
            onPostCreated = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.let { state ->
                        state[POST_REFRESH_KEY] =
                            (state.get<Int>(POST_REFRESH_KEY) ?: 0) + 1
                    }

                navController.popBackStack()
            }
        )
    }

    composable(
        route = EDIT_POST_ROUTE,
        arguments = listOf(navArgument("postId") { type = NavType.IntType })
    ) { backStackEntry ->
        val postId = backStackEntry.arguments?.getInt("postId")
        if (postId != null) {
            CreatePostScreen(
                editPostId = postId,
                onBackClick = { navController.popBackStack() },
                onPostCreated = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.let { state ->
                            state[POST_REFRESH_KEY] = (state.get<Int>(POST_REFRESH_KEY) ?: 0) + 1
                        }
                    navController.popBackStack()
                }
            )
        }
    }

    composable(POST_DETAIL_ROUTE) { backStackEntry ->
        val refreshTrigger by backStackEntry.savedStateHandle
            .getStateFlow(POST_REFRESH_KEY, 0)
            .collectAsState()
        val postId = backStackEntry.arguments
            ?.getString("postId")
            ?.toIntOrNull()

        if (postId != null) {
            PostDetailScreen(
                postId = postId,
                refreshTrigger = refreshTrigger,
                onBackClick = {
                    navController.previousBackStackEntry?.savedStateHandle?.let { state ->
                        state[POST_REFRESH_KEY] = (state.get<Int>(POST_REFRESH_KEY) ?: 0) + 1
                    }
                    navController.popBackStack()
                },
                onEditPost = { id -> navController.navigate("edit_post/$id") }
            )
        }
    }
}

private fun NavGraphBuilder.feedDestination(
    route: String,
    initialTab: Int,
    postType: String,
    navController: NavController
) {
    composable(route) { backStackEntry ->
        val refreshTrigger by backStackEntry.savedStateHandle
            .getStateFlow(POST_REFRESH_KEY, 0)
            .collectAsState()

        GeneralFeedScreen(
            initialTab = initialTab,
            refreshTrigger = refreshTrigger,
            onPostClick = { postId ->
                navController.navigate("post_detail/$postId")
            },
            onCreatePostClick = {
                navController.navigate("$CREATE_POST_ROUTE_BASE/$postType")
            },
            onEditPostClick = { postId -> navController.navigate("edit_post/$postId") }
        )
    }
}
