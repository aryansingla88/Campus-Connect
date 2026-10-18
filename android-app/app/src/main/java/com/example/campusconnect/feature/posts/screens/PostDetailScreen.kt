package com.example.campusconnect.feature.posts.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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
import com.example.campusconnect.feature.posts.models.VoteType
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
    var isLoadingPost by remember(postId) { mutableStateOf(true) }
    var postLoadError by remember(postId) { mutableStateOf<String?>(null) }
    var commentsForPost by remember(postId) { mutableStateOf<List<Comment>>(emptyList()) }
    var draft by remember(postId) { mutableStateOf("") }
    var replyTarget by remember(postId) { mutableStateOf<Comment?>(null) }
    var isSubmitting by remember(postId) { mutableStateOf(false) }
    var submissionError by remember(postId) { mutableStateOf<String?>(null) }
    var isLoadingComments by remember(postId) { mutableStateOf(true) }
    var showDeleteConfirmation by remember(postId) { mutableStateOf(false) }
    var isDeleting by remember(postId) { mutableStateOf(false) }
    var deleteError by remember(postId) { mutableStateOf<String?>(null) }
    var voteError by remember(postId) { mutableStateOf<String?>(null) }
    var commentToDelete by remember(postId) { mutableStateOf<Comment?>(null) }
    var isDeletingComment by remember(postId) { mutableStateOf(false) }
    var commentDeleteError by remember(postId) { mutableStateOf<String?>(null) }
    var editingCommentId by remember(postId) { mutableStateOf<Int?>(null) }
    var editDraft by remember(postId) { mutableStateOf("") }
    var isSavingCommentEdit by remember(postId) { mutableStateOf(false) }
    var commentEditError by remember(postId) { mutableStateOf<String?>(null) }

    LaunchedEffect(postId) {
        post = null
        isLoadingPost = true
        postLoadError = null
        commentsForPost = emptyList()
        draft = ""
        replyTarget = null
        isSubmitting = false
        submissionError = null
        isLoadingComments = true
        commentToDelete = null
        isDeletingComment = false
        commentDeleteError = null
        editingCommentId = null
        editDraft = ""
        isSavingCommentEdit = false
        commentEditError = null

        val postResult = viewModel.getPost(postId)
        postResult
            .onSuccess { post = it }
            .onFailure { postLoadError = it.message ?: "Post not found." }
        if (postResult.isSuccess) {
            commentsForPost = viewModel.getComments(postId).getOrDefault(emptyList())
        }
        isLoadingPost = false
        isLoadingComments = false
    }

    val topLevelComments = commentsForPost.filter { it.parentCommentId == null }

    fun startCommentEdit(comment: Comment) {
        if (!isSavingCommentEdit && comment.canEdit && !comment.isDeleted) {
            editingCommentId = comment.id
            editDraft = comment.body
            commentEditError = null
        }
    }

    fun cancelCommentEdit() {
        if (!isSavingCommentEdit) {
            editingCommentId = null
            editDraft = ""
            commentEditError = null
        }
    }

    fun saveCommentEdit(comment: Comment) {
        val body = editDraft.trim()
        if (isSavingCommentEdit || body.isBlank()) {
            if (body.isBlank()) commentEditError = "Comment cannot be empty."
            return
        }

        isSavingCommentEdit = true
        commentEditError = null
        coroutineScope.launch {
            try {
                viewModel.updateComment(comment.id, body)
                    .onSuccess { updatedComment ->
                        commentsForPost = commentsForPost.map { current ->
                            if (current.id == updatedComment.id) updatedComment else current
                        }
                        editingCommentId = null
                        editDraft = ""
                    }
                    .onFailure { error ->
                        commentEditError = error.message ?: "Couldn't update this comment."
                    }
            } finally {
                isSavingCommentEdit = false
            }
        }
    }

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
            if (isLoadingPost) {
                item(key = "post-loading") {
                    Text("Loading post…", color = HintColor, modifier = Modifier.padding(16.dp))
                }
            } else if (postLoadError != null) {
                item(key = "post-error") {
                    Text(postLoadError ?: "Post not found.", color = HintColor, modifier = Modifier.padding(16.dp))
                }
            } else {
            post?.let { currentPost ->
                item(key = "post-card") {
                    PostDetailCard(
                        post = currentPost,
                        commentCount = commentsForPost.size,
                        onDeleteClick = { showDeleteConfirmation = true },
                        onUpvoteClick = {
                            coroutineScope.launch {
                                voteError = null
                                val result = if (currentPost.userVote == VoteType.UPVOTE) {
                                    viewModel.removePostVote(postId)
                                } else viewModel.upvotePost(postId)
                                result.onSuccess {
                                    post = post?.let { current ->
                                        when (current.userVote) {
                                            VoteType.UPVOTE -> current.copy(upvotes = (current.upvotes - 1).coerceAtLeast(0), userVote = null)
                                            VoteType.DOWNVOTE -> current.copy(upvotes = current.upvotes + 1, downvotes = (current.downvotes - 1).coerceAtLeast(0), userVote = VoteType.UPVOTE)
                                            null -> current.copy(upvotes = current.upvotes + 1, userVote = VoteType.UPVOTE)
                                        }
                                    }
                                }
                                result.onFailure { voteError = it.message ?: "Couldn't update your vote." }
                            }
                        },
                        onDownvoteClick = {
                            coroutineScope.launch {
                                voteError = null
                                val result = if (currentPost.userVote == VoteType.DOWNVOTE) {
                                    viewModel.removePostVote(postId)
                                } else viewModel.downvotePost(postId)
                                result.onSuccess {
                                    post = post?.let { current ->
                                        when (current.userVote) {
                                            VoteType.DOWNVOTE -> current.copy(downvotes = (current.downvotes - 1).coerceAtLeast(0), userVote = null)
                                            VoteType.UPVOTE -> current.copy(upvotes = (current.upvotes - 1).coerceAtLeast(0), downvotes = current.downvotes + 1, userVote = VoteType.DOWNVOTE)
                                            null -> current.copy(downvotes = current.downvotes + 1, userVote = VoteType.DOWNVOTE)
                                        }
                                    }
                                }
                                result.onFailure { voteError = it.message ?: "Couldn't update your vote." }
                            }
                        }
                    )
                }
                voteError?.let { message ->
                    item(key = "vote-error") {
                        Text(
                            text = message,
                            color = com.example.campusconnect.core.ui.theme.ErrorRed,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
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
                        isEditing = editingCommentId == comment.id,
                        editDraft = editDraft,
                        onEditDraftChange = { editDraft = it },
                        isSavingEdit = isSavingCommentEdit,
                        editError = if (editingCommentId == comment.id) commentEditError else null,
                        onEdit = ::startCommentEdit,
                        onSaveEdit = { saveCommentEdit(comment) },
                        onCancelEdit = ::cancelCommentEdit,
                        onReply = {
                            if (!isSubmitting) {
                                replyTarget = it
                                submissionError = null
                            }
                        },
                        onDelete = {
                            commentToDelete = it
                            commentDeleteError = null
                        }
                    )

                    commentsForPost
                        .filter { it.parentCommentId == comment.id }
                        .forEach { reply ->
                            CommentCard(
                                comment = reply,
                                isReply = true,
                                isEditing = editingCommentId == reply.id,
                                editDraft = editDraft,
                                onEditDraftChange = { editDraft = it },
                                isSavingEdit = isSavingCommentEdit,
                                editError = if (editingCommentId == reply.id) commentEditError else null,
                                onEdit = ::startCommentEdit,
                                onSaveEdit = { saveCommentEdit(reply) },
                                onCancelEdit = ::cancelCommentEdit,
                                onDelete = {
                                    commentToDelete = it
                                    commentDeleteError = null
                                }
                            )
                        }
                }
            }
            }
        }

        if (post != null && postLoadError == null) CommentComposer(
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

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteConfirmation = false },
            title = { Text("Delete post?") },
            text = { Text("The post content and media will be removed. Its comments, replies, and votes will remain.") },
            confirmButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        isDeleting = true
                        coroutineScope.launch {
                            viewModel.deletePost(postId)
                                .onSuccess {
                                    showDeleteConfirmation = false
                                    onBackClick()
                                }
                                .onFailure { deleteError = it.message ?: "Couldn't delete the post." }
                            isDeleting = false
                        }
                    }
                ) { Text(if (isDeleting) "Deleting…" else "Delete") }
            },
            dismissButton = {
                TextButton(enabled = !isDeleting, onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    deleteError?.let { message ->
        AlertDialog(
            onDismissRequest = { deleteError = null },
            title = { Text("Couldn't delete post") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { deleteError = null }) { Text("OK") } }
        )
    }

    commentToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { if (!isDeletingComment) commentToDelete = null },
            title = { Text("Delete comment?") },
            text = {
                Text(
                    if (target.parentCommentId == null && commentsForPost.any { it.parentCommentId == target.id }) {
                        "This comment will be marked as deleted so its replies remain visible."
                    } else {
                        "This comment will be permanently removed."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !isDeletingComment,
                    onClick = {
                        isDeletingComment = true
                        coroutineScope.launch {
                            try {
                                viewModel.deleteComment(target.id)
                                    .onSuccess { result ->
                                        val removedIds = result.removedCommentIds.ifEmpty {
                                            listOf(result.commentId)
                                        }.toSet()
                                        if (result.retainedAsTombstone) {
                                            val tombstone = result.comment
                                            if (tombstone != null) {
                                                commentsForPost = commentsForPost.map { current ->
                                                    if (current.id == result.commentId) tombstone else current
                                                }
                                            } else {
                                                commentDeleteError = "Couldn't update the deleted comment. Please reload."
                                                commentToDelete = null
                                            }
                                        } else {
                                            commentsForPost = commentsForPost.filterNot { it.id in removedIds }
                                        }
                                        if (!result.retainedAsTombstone
                                            && replyTarget?.id?.let { it in removedIds } == true
                                        ) {
                                            replyTarget = null
                                        }
                                        if (commentDeleteError == null) commentToDelete = null
                                    }
                                    .onFailure { error ->
                                        commentDeleteError = error.message ?: "Couldn't delete this comment."
                                        commentToDelete = null
                                    }
                            } finally {
                                isDeletingComment = false
                            }
                        }
                    }
                ) { Text(if (isDeletingComment) "Deleting…" else "Delete") }
            },
            dismissButton = {
                TextButton(
                    enabled = !isDeletingComment,
                    onClick = { commentToDelete = null }
                ) { Text("Cancel") }
            }
        )
    }

    commentDeleteError?.let { message ->
        AlertDialog(
            onDismissRequest = { commentDeleteError = null },
            title = { Text("Couldn't delete comment") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { commentDeleteError = null }) { Text("OK") }
            }
        )
    }
}
