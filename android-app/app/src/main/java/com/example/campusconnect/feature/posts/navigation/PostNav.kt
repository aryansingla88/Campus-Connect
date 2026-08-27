package com.example.campusconnect.feature.posts.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.campusconnect.feature.posts.screens.GeneralFeedScreen
import com.example.campusconnect.feature.posts.screens.PostDetailScreen

const val POSTS_FEED_ROUTE = "posts"

const val POST_DETAIL_ROUTE = "post_detail/{postId}"

fun NavGraphBuilder.postNav(
    navController: NavController
){
    composable(POSTS_FEED_ROUTE) {

        GeneralFeedScreen(
            onPostClick = { postId ->

                navController.navigate("post_detail/$postId")
            }


        )
    }
    composable(
        route = POST_DETAIL_ROUTE
    ) { backStackEntry ->

        val postId =
            backStackEntry.arguments
                ?.getString("postId")
                ?.toIntOrNull()

        postId?.let {

            PostDetailScreen(
                postId = it,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}