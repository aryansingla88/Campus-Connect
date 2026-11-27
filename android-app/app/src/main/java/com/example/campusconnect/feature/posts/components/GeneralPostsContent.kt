package com.example.campusconnect.feature.posts.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campusconnect.core.ui.theme.ErrorRed
import com.example.campusconnect.core.ui.theme.HintColor
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.models.PostTag
import com.example.campusconnect.feature.posts.models.PostTagCount

@Composable
fun GeneralPostsContent(
    posts: List<Post>, searchResults: List<Post>, tags: List<PostTag>, tagCounts: List<PostTagCount>?,
    isLoading: Boolean, error: String?, isLoadingTags: Boolean, tagsError: String?,
    isLoadingTagCounts: Boolean, tagCountsError: String?, isSearching: Boolean, searchError: String?,
    searchVisible: Boolean, searchQuery: String, selectedTopicId: Int?, topicRowVisible: Boolean,
    listState: LazyListState, profileUserId: Int?, voteError: String?,
    onTopicSelected: (Int?) -> Unit, onPostClick: (Post) -> Unit, onUpvote: (Post) -> Unit,
    onDownvote: (Post) -> Unit, onEdit: (Post) -> Unit, onDelete: (Post) -> Unit,
    onAuthorClick: (Int) -> Unit
) {
    AnimatedVisibility(visible = topicRowVisible, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
        Column {
            TopicChipsRow(tags, tagCounts, isLoadingTagCounts, isLoadingTags, tagCountsError, selectedTopicId, onTopicSelected)
            tagsError?.let { Text("Topics unavailable: $it") }
            Spacer(Modifier.fillMaxWidth().height(6.dp))
        }
    }
    val filteredPosts = posts.filter { selectedTopicId == null || it.tags.any { tag -> tag.id == selectedTopicId } }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), state = listState,
        verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(top = 6.dp, bottom = 12.dp)
    ) {
        voteError?.let { message -> item(key = "vote-error") { Text(message, color = ErrorRed) } }
        if (searchVisible) {
            item(key = "search-result-summary") {
                val query = searchQuery.trim()
                when {
                    query.length < 2 -> Text("Enter at least 2 characters to search.", color = HintColor)
                    isSearching -> Text("Searching…", color = HintColor)
                    searchError != null -> Text(searchError, color = ErrorRed)
                    else -> Text("${searchResults.size} ${if (searchResults.size == 1) "result" else "results"} for “$query”", color = HintColor)
                }
            }
            if (searchQuery.trim().length >= 2 && !isSearching && searchError == null && searchResults.isEmpty()) {
                item(key = "search-no-results") { Text("No matching posts found.", color = HintColor) }
            }
            items(searchResults, key = { "search-post-${it.id}" }) { post ->
                PostCard(post = post, commentCount = post.commentCount, searchHighlightQuery = searchQuery,
                    onClick = { onPostClick(post) }, onUpvoteClick = { onUpvote(post) }, onDownvoteClick = { onDownvote(post) },
                    onDeleteClick = { onDelete(post) }, onEditClick = { onEdit(post) }, onAuthorClick = onAuthorClick)
            }
        } else if (isLoading && posts.isEmpty()) {
            items(3, key = { "post-ghost-$it" }) { index -> PostCardGhost(showImage = index != 0) }
        } else if (error != null && posts.isEmpty()) {
            item { Text(error, color = ErrorRed) }
        } else if (filteredPosts.isEmpty()) {
            item { Text(if (selectedTopicId == null) if (profileUserId == null) "No posts yet." else "No posts for this user." else "No posts for this topic.", color = HintColor) }
        } else {
            items(filteredPosts) { post ->
                PostCard(post = post, commentCount = post.commentCount, onClick = { onPostClick(post) },
                    onUpvoteClick = { onUpvote(post) }, onDownvoteClick = { onDownvote(post) },
                    onDeleteClick = { onDelete(post) }, onEditClick = { onEdit(post) }, onAuthorClick = onAuthorClick)
            }
        }
    }
}
