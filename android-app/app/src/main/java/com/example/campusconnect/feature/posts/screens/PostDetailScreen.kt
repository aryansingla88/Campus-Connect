package com.example.campusconnect.feature.posts.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campusconnect.feature.posts.components.PostDetailCard
import androidx.compose.foundation.lazy.items
import com.example.campusconnect.feature.posts.components.CommentCard
import com.example.campusconnect.feature.posts.components.PostDetailTopBar

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.campusconnect.feature.posts.models.Comment
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.viewmodel.PostDetailViewModel


@Composable
fun PostDetailScreen(

    postId: Int,

    onBackClick: () -> Unit
) {
    val viewModel: PostDetailViewModel = viewModel()

    var post by remember {
        mutableStateOf<Post?>(null)
    }

    var commentsForPost by remember {

        mutableStateOf<List<Comment>>(emptyList())
    }

    LaunchedEffect(postId) {

        viewModel
            .getPost(postId)
            .onSuccess {
                post = it
            }

        commentsForPost =
            viewModel
                .getComments(postId)
                .getOrDefault(emptyList())
    }
    val topLevelComments =

        commentsForPost.filter {

            it.parentCommentId == null
        }

    Column(

        modifier = Modifier.fillMaxSize()
    ) {
        PostDetailTopBar(

            onBackClick = onBackClick
        )

        /*
        Fixed Post
         */

        post?.let {

            PostDetailCard(
                post = it,
                commentCount = commentsForPost.size
            )
        }

        /*
        Scrollable Comments
         */

        LazyColumn(

            modifier = Modifier.weight(1f)
        ) {

            item {

                Text(

                    text = "Comments",

                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
                )
            }

            items(topLevelComments) { comment ->

                CommentCard(
                    comment = comment
                )

                val replies =

                    commentsForPost.filter {

                        it.parentCommentId == comment.id
                    }

                replies.forEach { reply ->

                    CommentCard(

                        comment = reply,

                        isReply = true
                    )
                }
            }
        }
    }
}