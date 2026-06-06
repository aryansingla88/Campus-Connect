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
import com.example.campusconnect.feature.posts.models.dummyComments
import androidx.compose.foundation.lazy.items
import com.example.campusconnect.feature.posts.components.CommentCard
import com.example.campusconnect.feature.posts.components.PostDetailTopBar

import com.example.campusconnect.feature.posts.models.Post


@Composable
fun PostDetailScreen(

    post: Post,

    onBackClick: () -> Unit
) {

    val commentsForPost =

        dummyComments.filter {

            it.postId == post.id
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

        PostDetailCard(
            post = post
        )

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