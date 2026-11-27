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
import com.example.campusconnect.feature.posts.models.PostEvent

@Composable
fun EventPostsContent(
    events: List<PostEvent>, searchResults: List<PostEvent>, isLoading: Boolean, error: String?,
    searchVisible: Boolean, searchQuery: String, isSearching: Boolean, searchError: String?,
    votingPostIds: Set<Int>, voteError: String?, profileUserId: Int?,
    onRegister: (PostEvent) -> Unit, onOpen: (PostEvent) -> Unit, onEdit: (PostEvent) -> Unit,
    onDelete: (PostEvent) -> Unit, onUpvote: (PostEvent) -> Unit, onDownvote: (PostEvent) -> Unit,
    onOrganizerClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        voteError?.let { message -> item(key = "event-vote-error") { Text(message, color = ErrorRed) } }
        if (searchVisible) {
            item(key = "event-search-result-summary") {
                val query = searchQuery.trim()
                when {
                    query.length < 2 -> Text("Enter at least 2 characters to search.", color = HintColor)
                    isSearching -> Text("Searching…", color = HintColor)
                    searchError != null -> Text(searchError, color = ErrorRed)
                    else -> Text("${searchResults.size} ${if (searchResults.size == 1) "result" else "results"} for “$query”", color = HintColor)
                }
            }
            if (searchQuery.trim().length >= 2 && !isSearching && searchError == null && searchResults.isEmpty()) {
                item(key = "event-search-no-results") { Text("No matching event posts found.", color = HintColor) }
            }
            items(searchResults, key = { "search-event-${it.id}" }) { event ->
                EventPostCard(event = event, onRegisterClick = { onRegister(event) },
                    isVoting = event.postId?.let(votingPostIds::contains) == true, searchHighlightQuery = searchQuery,
                    onClick = { onOpen(event) }, onEditClick = { onEdit(event) }, onDeleteClick = { onDelete(event) },
                    onUpvoteClick = { onUpvote(event) }, onDownvoteClick = { onDownvote(event) }, onOrganizerClick = onOrganizerClick)
            }
        } else if (isLoading && events.isEmpty()) {
            items(3, key = { "event-post-ghost-$it" }) { index -> PostCardGhost(showImage = index != 0) }
        } else if (error != null && events.isEmpty()) {
            item { Text(error, color = ErrorRed) }
        } else if (events.isEmpty()) {
            item { Text(if (profileUserId == null) "No event posts yet." else "No event posts for this user.", color = HintColor) }
        } else {
            items(events, key = { it.id }) { event ->
                EventPostCard(event = event, onRegisterClick = { onRegister(event) },
                    isVoting = event.postId?.let(votingPostIds::contains) == true,
                    onClick = { onOpen(event) }, onEditClick = { onEdit(event) }, onDeleteClick = { onDelete(event) },
                    onUpvoteClick = { onUpvote(event) }, onDownvoteClick = { onDownvote(event) }, onOrganizerClick = onOrganizerClick)
            }
        }
    }
}
