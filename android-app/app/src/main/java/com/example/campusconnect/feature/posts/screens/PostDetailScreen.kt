package com.example.campusconnect.feature.posts.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.core.ui.theme.HintColor
import com.example.campusconnect.feature.posts.components.CommentCard
import com.example.campusconnect.feature.posts.components.CommentComposer
import com.example.campusconnect.feature.posts.components.PostDetailCard
import com.example.campusconnect.feature.posts.components.PostDetailTopBar
import com.example.campusconnect.feature.posts.models.Comment
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.viewmodel.PostDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun PostDetailScreen(
    postId: Int,
    onBackClick: () -> Unit
) {
    key(postId) {
        PostDetailContent(postId, onBackClick)
    }
}

@Composable
private fun PostDetailContent(
    postId: Int,
    onBackClick: () -> Unit
) {
    val viewModel: PostDetailViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    var post by remember(postId) { mutableStateOf<Post?>(null) }
    var commentsForPost by remember(postId) { mutableStateOf<List<Comment>>(emptyList()) }
    var draft by remember(postId) { mutableStateOf("") }
    var replyTarget by remember(postId) { mutableStateOf<Comment?>(null) }
    var isSubmitting by remember(postId) { mutableStateOf(false) }
    var submissionError by remember(postId) { mutableStateOf<String?>(null) }
    var isLoadingComments by remember(postId) { mutableStateOf(true) }

    LaunchedEffect(postId) {
        post = null
        commentsForPost = emptyList()
        draft = ""
        replyTarget = null
        isSubmitting = false
        submissionError = null
        isLoadingComments = true

        viewModel.getPost(postId).onSuccess {
            post = it
        }

        commentsForPost = viewModel.getComments(postId).getOrDefault(emptyList())
        isLoadingComments = false
    }

    val topLevelComments = commentsForPost.filter { it.parentCommentId == null }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding()
    ) {
        PostDetailTopBar(onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            post?.let { currentPost ->
                item(key = "post-card") {
                    PostDetailCard(
                        post = currentPost,
                        commentCount = commentsForPost.size
                    )
                }
            }

            item(key = "comments-header") {
                Text(
                    text = "Comments (${commentsForPost.size})",
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (isLoadingComments) {
                item(key = "comments-loading") {
                    Text(
                        text = "Loading comments…",
                        color = HintColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            } else if (commentsForPost.isEmpty()) {
                item(key = "comments-empty") {
                    Text(
                        text = "No comments yet. Start the conversation.",
                        color = HintColor,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            } else {
                items(
                    items = topLevelComments,
                    key = { comment -> "comment-${comment.id}" }
                ) { comment ->
                    CommentCard(
                        comment = comment,
                        onReply = {
                            if (!isSubmitting) {
                                replyTarget = it
                                submissionError = null
                            }
                        }
                    )

                    commentsForPost
                        .filter { it.parentCommentId == comment.id }
                        .forEach { reply ->
                            CommentCard(
                                comment = reply,
                                isReply = true
                            )
                        }
                }
            }
        }

        CommentComposer(
            draft = draft,
            onDraftChange = { draft = it },
            replyTarget = replyTarget,
            onCancelReply = { replyTarget = null },
            isSubmitting = isSubmitting,
            errorMessage = submissionError,
            onSend = {
                val body = draft.trim()
                val target = replyTarget
                if (!isSubmitting && body.isNotBlank()) {
                    isSubmitting = true
                    submissionError = null
                    coroutineScope.launch {
                        try {
                            val result = if (target == null) {
                                viewModel.createComment(postId, body)
                            } else {
                                viewModel.createReply(postId, target.id, body)
                            }

                            result
                                .onSuccess { newComment ->
                                    commentsForPost = commentsForPost + newComment
                                    draft = ""
                                    replyTarget = null
                                }
                                .onFailure { error ->
                                    submissionError = error.message
                                        ?: "Couldn't send comment. Please try again."
                                }
                        } finally {
                            isSubmitting = false
                        }
                    }
                }
            }
        )
    }
}
