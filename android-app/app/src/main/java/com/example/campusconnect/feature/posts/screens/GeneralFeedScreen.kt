package com.example.campusconnect.feature.posts.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
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
import com.example.campusconnect.feature.posts.components.FeedSearchBar
import com.example.campusconnect.feature.posts.components.EventPostCard
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.models.PostTag
import com.example.campusconnect.feature.posts.models.PostTagCount
import com.example.campusconnect.feature.posts.models.PostEvent
import com.example.campusconnect.feature.posts.viewmodel.FeedViewModel
import com.example.campusconnect.feature.posts.components.TopicChipsRow
import com.example.campusconnect.feature.posts.models.VoteType
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

//@OptIn means ->"I know I'm using an experimental Material 3 API, and I accept that it may change in future versions."
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralFeedScreen(

    initialTab: Int = 0,
    profileUserId: Int? = null,
    onPostClick: (Int) -> Unit,
    onCreatePostClick: (postType: String) -> Unit,
    onEditPostClick: (Int) -> Unit = {},
    onUserProfileClick: (Int) -> Unit = {},
    onBackClick: () -> Unit = {},
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
    var generalSearchResults by remember { mutableStateOf<List<Post>>(emptyList()) }
    var eventSearchResults by remember { mutableStateOf<List<PostEvent>>(emptyList()) }

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
            generalSearchResults = generalSearchResults.map { searchedPost ->
                posts.firstOrNull { it.id == searchedPost.id } ?: searchedPost
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
            generalSearchResults = generalSearchResults.map { searchedPost ->
                posts.firstOrNull { it.id == searchedPost.id } ?: searchedPost
            }
        }
        result.onFailure { voteError = it.message ?: "Couldn't update your vote." }
    }

    fun updateEventVoteCounts(postId: Int, from: VoteType?, to: VoteType?) {
        fun update(event: PostEvent): PostEvent =
            if (event.postId != postId) {
                event
            } else {
                event.copy(
                    upvotes = (event.upvotes
                        - (if (from == VoteType.UPVOTE) 1 else 0)
                        + (if (to == VoteType.UPVOTE) 1 else 0)).coerceAtLeast(0),
                    downvotes = (event.downvotes
                        - (if (from == VoteType.DOWNVOTE) 1 else 0)
                        + (if (to == VoteType.DOWNVOTE) 1 else 0)).coerceAtLeast(0),
                    userVote = to
                )
            }
        eventPosts = eventPosts.map { currentEvent -> update(currentEvent) }
        eventSearchResults = eventSearchResults.map { currentEvent -> update(currentEvent) }
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

    var showSearchPopup by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchLoading by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var searchGeneration by remember { mutableIntStateOf(0) }
    var searchRequestJob by remember { mutableStateOf<Job?>(null) }

    fun resetPostSearch() {
        searchGeneration++
        searchRequestJob?.cancel()
        searchRequestJob = null
        showSearchPopup = false
        searchQuery = ""
        generalSearchResults = emptyList()
        eventSearchResults = emptyList()
        isSearchLoading = false
        searchError = null
    }

    fun updatePostSearchQuery(query: String) {
        searchQuery = query
        searchGeneration++
        searchRequestJob?.cancel()
        generalSearchResults = emptyList()
        eventSearchResults = emptyList()
        isSearchLoading = query.trim().length >= 2
        searchError = null
    }

    fun submitPostSearch(rawQuery: String) {
        val query = rawQuery.trim()
        if (query.length < 2) return

        searchRequestJob?.cancel()
        val requestGeneration = searchGeneration
        val requestTab = selectedTab
        isSearchLoading = true
        searchError = null
        searchRequestJob = coroutineScope.launch {
            if (requestTab == 0) {
                val result = viewModel.getPosts(query)
                if (requestGeneration != searchGeneration || requestTab != selectedTab || query != searchQuery.trim()) {
                    return@launch
                }
                result.onSuccess { generalSearchResults = it }
                    .onFailure { searchError = it.message ?: "Couldn't search posts." }
            } else if (requestTab == 1) {
                val result = viewModel.getEventPosts(query)
                if (requestGeneration != searchGeneration || requestTab != selectedTab || query != searchQuery.trim()) {
                    return@launch
                }
                result.onSuccess { eventSearchResults = it }
                    .onFailure { searchError = it.message ?: "Couldn't search event posts." }
            }
            isSearchLoading = false
        }
    }

    LaunchedEffect(showSearchPopup, searchQuery, selectedTab) {
        val query = searchQuery.trim()
        if (!showSearchPopup || query.length < 2 || selectedTab !in 0..1) return@LaunchedEffect
        kotlinx.coroutines.delay(300)
        submitPostSearch(query)
    }

    var selectedTopicId by remember {

        mutableStateOf<Int?>(null)
    }

    val generalFeedListState = rememberLazyListState()
    var isTopicRowVisible by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(generalFeedListState) {
        var previousIndex = generalFeedListState.firstVisibleItemIndex
        var previousOffset = generalFeedListState.firstVisibleItemScrollOffset

        snapshotFlow {
            Triple(
                generalFeedListState.firstVisibleItemIndex,
                generalFeedListState.firstVisibleItemScrollOffset,
                generalFeedListState.isScrollInProgress
            )
        }.collect { (index, offset, isScrolling) ->
            if (isScrolling) {
                when {
                    index > previousIndex || (index == previousIndex && offset > previousOffset) ->
                        isTopicRowVisible = false
                    index < previousIndex || (index == previousIndex && offset < previousOffset) ->
                        isTopicRowVisible = true
                }
            }
            previousIndex = index
            previousOffset = offset
        }
    }

    val filteredPosts = posts.filter { post ->
        (profileUserId == null || post.authorId == profileUserId) &&
                (selectedTopicId == null || post.tags.any { it.id == selectedTopicId })
    }
    val filteredGeneralSearchResults = generalSearchResults.filter { post ->
        profileUserId == null || post.authorId == profileUserId
    }
    val filteredEventPosts = eventPosts.filter { event ->
        profileUserId == null || event.organizerUserId == profileUserId
    }
    val filteredEventSearchResults = eventSearchResults.filter { event ->
        profileUserId == null || event.organizerUserId == profileUserId
    }

    Scaffold(
        floatingActionButton = {
            if (profileUserId == null && selectedTab != 2) {
                FloatingActionButton(
                    onClick = {
                        onCreatePostClick(if (selectedTab == 1) "EVENTS" else "GENERAL")
                    },
                    containerColor = OrangePrimary
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Create post",
                        tint = Color.White
                    )
                }
            }
        },
        bottomBar = {

            NavigationBar(

                containerColor = Color.White
            ) {

                NavigationBarItem(

                    selected = selectedTab == 0,//if selectedTab is 0 then selectedTab==0 returns true and thus selected gets true

                    onClick = {
                        resetPostSearch()
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
                        resetPostSearch()
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
                        resetPostSearch()
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

                        if (showSearchPopup) {
                            FeedSearchBar(
                                query = searchQuery,
                                onQueryChange = ::updatePostSearchQuery,
                                onBack = ::resetPostSearch
                            )
                            FeedTopBar(currentTitle = if (profileUserId == null) "General" else "Profile Posts", showSearchButton = false, showBrand = false)
                        } else {
                            FeedTopBar(
                                currentTitle = if (profileUserId == null) "General" else "Profile Posts",
                                onSearchClick = { showSearchPopup = true },
                                onBackClick = onBackClick.takeIf { profileUserId != null }
                            )
                        }

                        AnimatedVisibility(
                            visible = isTopicRowVisible,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                TopicChipsRow(
                                    tags = tags,
                                    tagCounts = tagCounts,
                                    countsLoading = countsLoading,
                                    countsError = countsError,
                                    selectedTopic = selectedTopicId,
                                    onTopicSelected = { selectedTopicId = it }
                                )
                                tagsError?.let { message ->
                                    Text("Topics unavailable: $message")
                                }
                                Spacer(Modifier.fillMaxWidth().height(6.dp))
                            }
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

                            state = generalFeedListState,

                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            //above line is for adding space between items

                            contentPadding = PaddingValues(top = 6.dp, bottom = 12.dp)
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

                            if (showSearchPopup) {
                                item(key = "search-result-summary") {
                                    val trimmedQuery = searchQuery.trim()
                                    when {
                                        trimmedQuery.length < 2 -> Text("Enter at least 2 characters to search.", color = HintColor)
                                        isSearchLoading -> Text("Searching…", color = HintColor)
                                        searchError != null -> Text(searchError!!, color = ErrorRed)
                                        else -> Text(
                                            "${filteredGeneralSearchResults.size} ${if (filteredGeneralSearchResults.size == 1) "result" else "results"} for “$trimmedQuery”",
                                            color = HintColor
                                        )
                                    }
                                }
                                if (searchQuery.trim().length >= 2 && !isSearchLoading && searchError == null && filteredGeneralSearchResults.isEmpty()) {
                                    item(key = "search-no-results") { Text("No matching posts found.", color = HintColor) }
                                }
                                items(filteredGeneralSearchResults, key = { "search-post-${it.id}" }) { post ->
                                    com.example.campusconnect.feature.posts.components.PostCard(
                                        post = post,
                                        commentCount = post.commentCount,
                                        searchHighlightQuery = searchQuery,
                                        onClick = { resetPostSearch(); onPostClick(post.id) },
                                        onUpvoteClick = { coroutineScope.launch { handleUpvote(post) } },
                                        onDownvoteClick = { coroutineScope.launch { handleDownvote(post) } },
                                        onDeleteClick = { postToDelete = post; deleteError = null },
                                        onEditClick = { onEditPostClick(post.id) },
                                        onAuthorClick = onUserProfileClick
                                    )
                                }
                            } else if (isLoading && posts.isEmpty()) {
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
                                    onEditClick = { onEditPostClick(post.id) },
                                    onAuthorClick = onUserProfileClick

                                )
                            }
                        }
                    }
                }

                1 -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(padding)
                    ) {
                        if (showSearchPopup) {
                            FeedSearchBar(
                                query = searchQuery,
                                onQueryChange = ::updatePostSearchQuery,
                                onBack = ::resetPostSearch
                            )
                            FeedTopBar(currentTitle = if (profileUserId == null) "Events" else "Profile Events", showSearchButton = false, showBrand = false)
                        } else {
                            FeedTopBar(
                                currentTitle = if (profileUserId == null) "Events" else "Profile Events",
                                onSearchClick = { showSearchPopup = true },
                                onBackClick = onBackClick.takeIf { profileUserId != null }
                            )
                        }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            voteError?.let { message ->
                                item(key = "event-vote-error") { Text(message, color = ErrorRed) }
                            }
                            if (showSearchPopup) {
                                item(key = "event-search-result-summary") {
                                    val trimmedQuery = searchQuery.trim()
                                    when {
                                        trimmedQuery.length < 2 -> Text("Enter at least 2 characters to search.", color = HintColor)
                                        isSearchLoading -> Text("Searching…", color = HintColor)
                                        searchError != null -> Text(searchError!!, color = ErrorRed)
                                        else -> Text(
                                            "${filteredEventSearchResults.size} ${if (filteredEventSearchResults.size == 1) "result" else "results"} for “$trimmedQuery”",
                                            color = HintColor
                                        )
                                    }
                                }
                                if (searchQuery.trim().length >= 2 && !isSearchLoading && searchError == null && filteredEventSearchResults.isEmpty()) {
                                    item(key = "event-search-no-results") { Text("No matching event posts found.", color = HintColor) }
                                }
                                items(filteredEventSearchResults, key = { "search-event-${it.id}" }) { event ->
                                    EventPostCard(
                                        event = event,
                                        isVoting = event.postId?.let(votingEventPostIds::contains) == true,
                                        searchHighlightQuery = searchQuery,
                                        onClick = { event.postId?.let { resetPostSearch(); onPostClick(it) } },
                                        onEditClick = { event.postId?.let(onEditPostClick) },
                                        onDeleteClick = { eventPostToDelete = event; eventPostDeleteError = null },
                                        onUpvoteClick = { coroutineScope.launch { handleEventVote(event, VoteType.UPVOTE) } },
                                        onDownvoteClick = { coroutineScope.launch { handleEventVote(event, VoteType.DOWNVOTE) } },
                                        onOrganizerClick = onUserProfileClick
                                    )
                                }
                            } else if (isLoadingEventPosts && filteredEventPosts.isEmpty()) {
                                item { Text("Loading event posts…") }
                            } else if (eventPostsError != null && filteredEventPosts.isEmpty()) {
                                item { Text(eventPostsError!!, color = ErrorRed) }
                            } else if (filteredEventPosts.isEmpty()) {
                                item { Text(if (profileUserId == null) "No event posts yet." else "No event posts for this user.", color = HintColor) }
                            } else {
                                items(filteredEventPosts, key = { it.id }) { event ->
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
                                        },
                                        onOrganizerClick = onUserProfileClick
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
                                    generalSearchResults = generalSearchResults.filterNot { current -> current.id == post.id }
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
                                    eventSearchResults = eventSearchResults.filterNot { it.postId == postId }
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
