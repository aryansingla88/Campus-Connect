package com.example.campusconnect.feature.posts.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.NavigationBar
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.campusconnect.core.ui.theme.*
import com.example.campusconnect.feature.posts.components.FeedTopBar
import com.example.campusconnect.feature.posts.components.EventPostCard
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.models.PostTag
import com.example.campusconnect.feature.posts.models.PostTagCount
import com.example.campusconnect.feature.posts.models.PostEvent
import com.example.campusconnect.feature.posts.viewmodel.FeedViewModel
import com.example.campusconnect.feature.posts.components.TopicChipsRow
import com.example.campusconnect.feature.posts.models.VoteType
import kotlinx.coroutines.launch

//@OptIn means ->"I know I'm using an experimental Material 3 API, and I accept that it may change in future versions."
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralFeedScreen(

    initialTab: Int = 0,
    onPostClick: (Int) -> Unit,
    onCreatePostClick: () -> Unit,
    onEditPostClick: (Int) -> Unit = {},
    refreshTrigger: Int = 0,
    viewModel: FeedViewModel = FeedViewModel()
) {
    var posts by remember {
    mutableStateOf<List<Post>>(emptyList())
}
    var eventPosts by remember { mutableStateOf<List<PostEvent>>(emptyList()) }
    var isLoadingEventPosts by remember { mutableStateOf(true) }
    var eventPostsError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    var tags by remember {
        mutableStateOf<List<PostTag>>(emptyList())
    }

    var tagCounts by remember {
        mutableStateOf<List<PostTagCount>?>(null)
    }

    var countsLoading by remember {
        mutableStateOf(true)
    }

    var countsError by remember {
        mutableStateOf<String?>(null)
    }

    var tagsError by remember {
        mutableStateOf<String?>(null)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var voteError by remember { mutableStateOf<String?>(null) }
    var votingEventPostIds by remember { mutableStateOf<Set<Int>>(emptySet()) }

    var postToDelete by remember { mutableStateOf<Post?>(null) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    var eventPostToDelete by remember { mutableStateOf<PostEvent?>(null) }
    var eventPostDeleteError by remember { mutableStateOf<String?>(null) }
    var isDeletingEventPost by remember { mutableStateOf(false) }

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        errorMessage = null

        viewModel.getPosts()
            .onSuccess {
                posts = it
            }
            .onFailure {
                errorMessage = it.message ?: "Failed to load posts"
            }

        tagsError = null
        viewModel.getTags()
            .onSuccess {
                tags = it
            }
            .onFailure {
                tagsError = it.message ?: "Failed to load topics"
            }

        countsLoading = true
        countsError = null
        tagCounts = null
        viewModel.getGeneralTagCounts()
            .onSuccess {
                tagCounts = it
            }
            .onFailure {
                countsError = it.message ?: "Failed to load topic counts"
            }
        countsLoading = false

        isLoading = false

        isLoadingEventPosts = true
        eventPostsError = null
        viewModel.getEventPosts()
            .onSuccess { eventPosts = it }
            .onFailure { eventPostsError = it.message ?: "Failed to load event posts." }
        isLoadingEventPosts = false
    }


    suspend fun handleUpvote(post: Post) {

        voteError = null

        val result =

            if (post.userVote == VoteType.UPVOTE) {

                viewModel.removePostVote(post.id)

            } else {

                viewModel.upvotePost(post.id)
            }

        result.onSuccess {

            posts = posts.map { currentPost ->

                if (currentPost.id != post.id) {

                    currentPost

                } else {

                    when (post.userVote) {

                        VoteType.UPVOTE ->

                            currentPost.copy(

                                upvotes =
                                    (currentPost.upvotes - 1)
                                        .coerceAtLeast(0),

                                userVote = null
                            )

                        VoteType.DOWNVOTE ->

                            currentPost.copy(

                                upvotes = currentPost.upvotes + 1,

                                downvotes =
                                    (currentPost.downvotes - 1)
                                        .coerceAtLeast(0),

                                userVote = VoteType.UPVOTE
                            )

                        null ->

                            currentPost.copy(

                                upvotes = currentPost.upvotes + 1,

                                userVote = VoteType.UPVOTE
                            )
                    }
                }
            }
        }
        result.onFailure { voteError = it.message ?: "Couldn't update your vote." }
    }


    suspend fun handleDownvote(post: Post) {

        voteError = null

        val result =

            if (post.userVote == VoteType.DOWNVOTE) {

                viewModel.removePostVote(post.id)

            } else {

                viewModel.downvotePost(post.id)
            }

        result.onSuccess {

            posts = posts.map { currentPost ->

                if (currentPost.id != post.id) {

                    currentPost

                } else {

                    when (post.userVote) {

                        VoteType.DOWNVOTE ->

                            currentPost.copy(

                                downvotes =
                                    (currentPost.downvotes - 1)
                                        .coerceAtLeast(0),

                                userVote = null
                            )

                        VoteType.UPVOTE ->

                            currentPost.copy(

                                upvotes =
                                    (currentPost.upvotes - 1)
                                        .coerceAtLeast(0),

                                downvotes = currentPost.downvotes + 1,

                                userVote = VoteType.DOWNVOTE
                            )

                        null ->

                            currentPost.copy(

                                downvotes = currentPost.downvotes + 1,

                                userVote = VoteType.DOWNVOTE
                            )
                    }
                }
            }
        }
        result.onFailure { voteError = it.message ?: "Couldn't update your vote." }
    }

    fun updateEventVoteCounts(postId: Int, from: VoteType?, to: VoteType?) {
        eventPosts = eventPosts.map { currentEvent ->
            if (currentEvent.postId != postId) {
                currentEvent
            } else {
                currentEvent.copy(
                    upvotes = (currentEvent.upvotes
                        - (if (from == VoteType.UPVOTE) 1 else 0)
                        + (if (to == VoteType.UPVOTE) 1 else 0)).coerceAtLeast(0),
                    downvotes = (currentEvent.downvotes
                        - (if (from == VoteType.DOWNVOTE) 1 else 0)
                        + (if (to == VoteType.DOWNVOTE) 1 else 0)).coerceAtLeast(0),
                    userVote = to
                )
            }
        }
    }

    suspend fun handleEventVote(event: PostEvent, requestedVote: VoteType) {
        val postId = event.postId ?: return
        if (postId in votingEventPostIds) return

        val previousVote = event.userVote
        val nextVote = if (previousVote == requestedVote) null else requestedVote
        votingEventPostIds = votingEventPostIds + postId
        voteError = null
        updateEventVoteCounts(postId, previousVote, nextVote)

        try {
            val result = when {
                previousVote == requestedVote -> viewModel.removePostVote(postId)
                requestedVote == VoteType.UPVOTE -> viewModel.upvotePost(postId)
                else -> viewModel.downvotePost(postId)
            }

            result.onFailure { error ->
                updateEventVoteCounts(postId, nextVote, previousVote)
                voteError = error.message ?: "Couldn't update your vote."
            }
        } catch (error: Exception) {
            updateEventVoteCounts(postId, nextVote, previousVote)
            voteError = error.message ?: "Couldn't update your vote."
        } finally {
            votingEventPostIds = votingEventPostIds - postId
        }
    }


    var selectedTab by rememberSaveable {
        mutableIntStateOf(initialTab)
    }

    var selectedTopicId by remember {

        mutableStateOf<Int?>(null)
    }

    val filteredPosts =

        if (selectedTopicId == null) {

            posts
        }
        else {

            posts.filter {

                it.tags.any { tag ->

                    tag.id == selectedTopicId
                }
            }
        }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreatePostClick,
                containerColor = OrangePrimary
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Create post",
                    tint = Color.White
                )
            }
        },
        bottomBar = {

            NavigationBar(

                containerColor = Color.White
            ) {

                NavigationBarItem(

                    selected = selectedTab == 0,//if selectedTab is 0 then selectedTab==0 returns true and thus selected gets true

                    onClick = {
                        selectedTab = 0
                    },

                    colors = NavigationBarItemDefaults.colors(

                        selectedTextColor = OrangePrimary,

                        selectedIconColor = OrangePrimary,

                        indicatorColor = OrangeLight,

                        unselectedTextColor = HintColor
                    ),

                    label = {
                        Text("General")
                    },

                    icon = {}
                )

                NavigationBarItem(

                    selected = selectedTab == 1,

                    onClick = {
                        selectedTab = 1
                    },

                    colors = NavigationBarItemDefaults.colors(

                        selectedTextColor = OrangePrimary,

                        selectedIconColor = OrangePrimary,

                        indicatorColor = OrangeLight,

                        unselectedTextColor = HintColor
                    ),

                    label = {
                        Text("Events")
                    },

                    icon = {}
                )

                NavigationBarItem(

                    selected = selectedTab == 2,

                    onClick = {
                        selectedTab = 2
                    },

                    colors = NavigationBarItemDefaults.colors(

                        selectedTextColor = OrangePrimary,

                        selectedIconColor = OrangePrimary,

                        indicatorColor = OrangeLight,

                        unselectedTextColor = HintColor
                    ),

                    label = {
                        Text("Custom")
                    },

                    icon = {}
                )
            }
        }

    ) { padding ->

        Box(

            modifier = Modifier
                .fillMaxSize()
                .background(PostsBackground)
        ) {

            when (selectedTab) {

                0 -> {

                    Column(

                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {

                        FeedTopBar(

                            currentTitle = when (selectedTab) {

                                0 -> "General"
                                1 -> "Events"
                                else -> "Custom"
                            }
                        )

                        TopicChipsRow(

                            tags = tags,

                            tagCounts = tagCounts,

                            countsLoading = countsLoading,

                            countsError = countsError,

                            selectedTopic = selectedTopicId,

                            onTopicSelected = {

                                selectedTopicId = it
                            }
                        )

                        tagsError?.let { message ->
                            Text("Topics unavailable: $message")
                        }

                        /*
                        In Jetpack Compose, LazyColumn is a vertically scrolling list that only renders
                         the items currently visible on the screen. It is the modern, declarative equivalent
                          of the RecyclerView used in the older Android View system
                         */

                        LazyColumn(

                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),

                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            //above line is for adding space between items

                            contentPadding = PaddingValues(vertical = 12.dp)
                            /*
                            contentPadding = PaddingValues(vertical = 12.dp)

                            means:

                            contentPadding = PaddingValues(
                            top = 12.dp,
                            bottom = 12.dp
                            )
                             */

                        ) {

                            voteError?.let { message ->
                                item(key = "vote-error") { Text(message, color = ErrorRed) }
                            }

                            if (isLoading && posts.isEmpty()) {
                                item { Text("Loading posts…") }
                            } else if (errorMessage != null && posts.isEmpty()) {
                                item { Text(errorMessage!!) }
                            } else if (filteredPosts.isEmpty()) {
                                item {
                                    Text(
                                        if (selectedTopicId == null) "No posts yet."
                                        else "No posts for this topic."
                                    )
                                }
                            } else items(filteredPosts) { post ->

                                com.example.campusconnect.feature.posts.components.PostCard(

                                    post = post,

                                    commentCount = post.commentCount,

                                    onClick = {

                                        onPostClick(post.id)
                                    },
                                    onUpvoteClick = {

                                        coroutineScope.launch {

                                            handleUpvote(post)
                                        }
                                    },

                                    onDownvoteClick = {

                                        coroutineScope.launch {

                                            handleDownvote(post)
                                        }
                                    },
                                    onDeleteClick = {
                                        postToDelete = post
                                        deleteError = null
                                    },
                                    onEditClick = { onEditPostClick(post.id) }

                                )
                            }
                        }
                    }
                }

                1 -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(padding)
                    ) {
                        FeedTopBar(currentTitle = "Events")
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            voteError?.let { message ->
                                item(key = "event-vote-error") { Text(message, color = ErrorRed) }
                            }
                            if (isLoadingEventPosts && eventPosts.isEmpty()) {
                                item { Text("Loading event posts…") }
                            } else if (eventPostsError != null && eventPosts.isEmpty()) {
                                item { Text(eventPostsError!!, color = ErrorRed) }
                            } else if (eventPosts.isEmpty()) {
                                item { Text("No event posts yet.", color = HintColor) }
                            } else {
                                items(eventPosts, key = { it.id }) { event ->
                                    EventPostCard(
                                        event = event,
                                        isVoting = event.postId?.let(votingEventPostIds::contains) == true,
                                        onClick = { event.postId?.let(onPostClick) },
                                        onEditClick = { event.postId?.let(onEditPostClick) },
                                        onDeleteClick = {
                                            eventPostToDelete = event
                                            eventPostDeleteError = null
                                        },
                                        onUpvoteClick = {
                                            coroutineScope.launch { handleEventVote(event, VoteType.UPVOTE) }
                                        },
                                        onDownvoteClick = {
                                            coroutineScope.launch { handleEventVote(event, VoteType.DOWNVOTE) }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Custom Rooms Coming Soon")
                }
            }
        }
    }

    postToDelete?.let { post ->
        AlertDialog(
            onDismissRequest = { if (!isDeleting) postToDelete = null },
            title = { Text("Delete post?") },
            text = { Text("The post content and media will be removed. Its comments, replies, and votes will remain.") },
            confirmButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        isDeleting = true
                        coroutineScope.launch {
                            val deleteResult = viewModel.deletePost(post.id)
                            deleteResult
                                .onSuccess {
                                    posts = posts.filterNot { current -> current.id == post.id }
                                    postToDelete = null
                                }
                                .onFailure { deleteError = it.message ?: "Couldn't delete the post." }

                            if (deleteResult.isSuccess) {
                                countsLoading = true
                                countsError = null
                                tagCounts = null
                                viewModel.getGeneralTagCounts()
                                    .onSuccess { tagCounts = it }
                                    .onFailure { countsError = it.message ?: "Failed to load topic counts" }
                                countsLoading = false
                            }
                            isDeleting = false
                        }
                    }
                ) { Text(if (isDeleting) "Deleting…" else "Delete") }
            },
            dismissButton = {
                TextButton(enabled = !isDeleting, onClick = { postToDelete = null }) { Text("Cancel") }
            }
        )
    }

    deleteError?.let { message ->
        AlertDialog(
            onDismissRequest = { deleteError = null; postToDelete = null },
            title = { Text("Couldn't delete post") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { deleteError = null; postToDelete = null }) { Text("OK") }
            }
        )
    }

    eventPostToDelete?.let { eventPost ->
        AlertDialog(
            onDismissRequest = { if (!isDeletingEventPost) eventPostToDelete = null },
            title = { Text("Delete event post?") },
            text = {
                Text("The event post’s description and attached media will be removed. The event details, comments, replies, and votes will remain.")
            },
            containerColor = White,
            titleContentColor = LabelColor,
            textContentColor = LabelColor,
            confirmButton = {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(contentColor = OrangePrimary),
                    enabled = !isDeletingEventPost,
                    onClick = {
                        val postId = eventPost.postId ?: return@TextButton
                        isDeletingEventPost = true
                        coroutineScope.launch {
                            val deleteResult = viewModel.deletePost(postId)
                            deleteResult
                                .onSuccess {
                                    eventPosts = eventPosts.filterNot { it.postId == postId }
                                    eventPostToDelete = null
                                }
                                .onFailure {
                                    eventPostDeleteError = it.message ?: "Couldn’t delete the event post."
                                }
                            isDeletingEventPost = false
                        }
                    }
                ) { Text(if (isDeletingEventPost) "Deleting…" else "Delete") }
            },
            dismissButton = {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(contentColor = OrangePrimary),
                    enabled = !isDeletingEventPost,
                    onClick = { eventPostToDelete = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    eventPostDeleteError?.let { message ->
        AlertDialog(
            onDismissRequest = { eventPostDeleteError = null; eventPostToDelete = null },
            title = { Text("Couldn’t delete event post") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { eventPostDeleteError = null; eventPostToDelete = null }) {
                    Text("OK")
                }
            }
        )
    }
}
