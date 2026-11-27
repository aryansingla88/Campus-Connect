package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campusconnect.core.ui.theme.ErrorRed
import com.example.campusconnect.core.ui.theme.HintColor
import com.example.campusconnect.feature.posts.models.Post

@Composable
fun CustomPostsContent(
    posts: List<Post>,
    searchResults: List<Post>,
    isLoading: Boolean,
    error: String?,
    searchVisible: Boolean,
    searchQuery: String,
    isSearching: Boolean,
    searchError: String?,
    profileUserId: Int?,
    voteError: String?,
    onPostClick: (Post) -> Unit,
    onUpvote: (Post) -> Unit,
    onDownvote: (Post) -> Unit,
    onEdit: (Post) -> Unit,
    onDelete: (Post) -> Unit,
    onAuthorClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        voteError?.let { message -> item(key = "custom-vote-error") { Text(message, color = ErrorRed) } }
        if (searchVisible) {
            item(key = "custom-search-summary") {
                val query = searchQuery.trim()
                when {
                    query.length < 2 -> Text("Enter at least 2 characters to search.", color = HintColor)
                    isSearching -> Text("Searching…", color = HintColor)
                    searchError != null -> Text(searchError, color = ErrorRed)
                    else -> Text("${searchResults.size} ${if (searchResults.size == 1) "result" else "results"} for “$query”", color = HintColor)
                }
            }
            if (searchQuery.trim().length >= 2 && !isSearching && searchError == null && searchResults.isEmpty()) {
                item(key = "custom-search-no-results") { Text("No matching custom posts found.", color = HintColor) }
            }
            items(searchResults, key = { "custom-search-${it.id}" }) { post ->
                PostCard(
                    post = post, commentCount = post.commentCount, searchHighlightQuery = searchQuery,
                    onClick = { onPostClick(post) }, onUpvoteClick = { onUpvote(post) },
                    onDownvoteClick = { onDownvote(post) }, onEditClick = { onEdit(post) },
                    onDeleteClick = { onDelete(post) }, onAuthorClick = onAuthorClick
                )
            }
        } else if (isLoading && posts.isEmpty()) {
            items(3, key = { "custom-post-ghost-$it" }) { index -> PostCardGhost(showImage = index != 0) }
        } else if (error != null && posts.isEmpty()) {
            item(key = "custom-post-error") { Text(error, color = ErrorRed) }
        } else if (posts.isEmpty()) {
            item(key = "custom-post-empty") {
                Text(
                    if (profileUserId == null) "No custom posts are available to you yet."
                    else "No custom posts from this user are available to you.",
                    color = HintColor
                )
            }
        } else {
            items(posts, key = { "custom-${it.id}" }) { post ->
                PostCard(
                    post = post, commentCount = post.commentCount,
                    onClick = { onPostClick(post) }, onUpvoteClick = { onUpvote(post) },
                    onDownvoteClick = { onDownvote(post) }, onEditClick = { onEdit(post) },
                    onDeleteClick = { onDelete(post) }, onAuthorClick = onAuthorClick
                )
            }
        }
    }
}
