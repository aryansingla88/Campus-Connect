package com.example.campusconnect.feature.posts.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.campusconnect.core.ui.theme.LabelColor
import com.example.campusconnect.core.ui.theme.OrangePrimary
import com.example.campusconnect.core.ui.theme.White
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.models.PostEvent

@Composable
fun FeedDeleteDialogs(
    post: Post?, eventPost: PostEvent?, isDeletingPost: Boolean, postError: String?,
    isDeletingEventPost: Boolean, eventPostError: String?, onDismissPost: () -> Unit,
    onConfirmPost: (Post) -> Unit, onDismissEventPost: () -> Unit, onConfirmEventPost: (PostEvent) -> Unit,
    onDismissErrors: () -> Unit
) {
    post?.let {
        AlertDialog(
            onDismissRequest = { if (!isDeletingPost) onDismissPost() },
            title = { Text("Delete post?") },
            text = { Text("The post content and media will be removed. Its comments, replies, and votes will remain.") },
            confirmButton = { TextButton(enabled = !isDeletingPost, onClick = { onConfirmPost(it) }) { Text(if (isDeletingPost) "Deleting…" else "Delete") } },
            dismissButton = { TextButton(enabled = !isDeletingPost, onClick = onDismissPost) { Text("Cancel") } }
        )
    }
    postError?.let { message ->
        AlertDialog(onDismissRequest = onDismissErrors, title = { Text("Couldn't delete post") }, text = { Text(message) },
            confirmButton = { TextButton(onClick = onDismissErrors) { Text("OK") } })
    }
    eventPost?.let { event ->
        AlertDialog(
            onDismissRequest = { if (!isDeletingEventPost) onDismissEventPost() },
            title = { Text("Delete event post?") },
            text = { Text("The event post’s description and attached media will be removed. The event details, comments, replies, and votes will remain.") },
            containerColor = White, titleContentColor = LabelColor, textContentColor = LabelColor,
            confirmButton = {
                TextButton(colors = ButtonDefaults.textButtonColors(contentColor = OrangePrimary), enabled = !isDeletingEventPost,
                    onClick = { if (event.postId != null) onConfirmEventPost(event) }) { Text(if (isDeletingEventPost) "Deleting…" else "Delete") }
            },
            dismissButton = { TextButton(colors = ButtonDefaults.textButtonColors(contentColor = OrangePrimary), enabled = !isDeletingEventPost, onClick = onDismissEventPost) { Text("Cancel") } }
        )
    }
    eventPostError?.let { message ->
        AlertDialog(onDismissRequest = onDismissErrors, title = { Text("Couldn’t delete event post") }, text = { Text(message) },
            confirmButton = { TextButton(onClick = onDismissErrors) { Text("OK") } })
    }
}
