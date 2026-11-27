package com.example.campusconnect.feature.posts.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
// this "scaffold" function is responsible for the top ribbon
// and the bottom navigation tabs-> general, evens, custom
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
//LaunchedEffect in androidx.compose.runtime is used to perform side effects inside a Composable function using a coroutine.
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.core.session.SessionManager
import com.example.campusconnect.core.visibility.VisibilityApiConfiguration
import com.example.campusconnect.core.visibility.VisibilityScope
import com.example.campusconnect.core.ui.theme.HintColor
import com.example.campusconnect.core.ui.theme.OrangeLight
import com.example.campusconnect.core.ui.theme.OrangePrimary
import com.example.campusconnect.core.ui.theme.PostsBackground
import com.example.campusconnect.feature.posts.components.EventPostsContent
import com.example.campusconnect.feature.posts.components.FeedDeleteDialogs
import com.example.campusconnect.feature.posts.components.FeedSearchBar
import com.example.campusconnect.feature.posts.components.FeedTopBar
import com.example.campusconnect.feature.posts.components.FeedWarmSurfaceColor
import com.example.campusconnect.feature.posts.components.FeedWaveBackground
import com.example.campusconnect.feature.posts.components.GeneralPostsContent
import com.example.campusconnect.feature.posts.components.CustomPostsContent
import com.example.campusconnect.feature.posts.components.CustomPostVisibilityDialog
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.models.PostEvent
import com.example.campusconnect.feature.posts.models.VoteType
import com.example.campusconnect.feature.posts.viewmodel.FeedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GeneralFeedScreen(
    initialTab: Int = 0,
    profileUserId: Int? = null,
    onPostClick: (Int, String) -> Unit,
    onCreatePostClick: (postType: String, visibility: VisibilityApiConfiguration) -> Unit,
    onRegisterEventClick: (Int) -> Unit,
    onEditPostClick: (Int) -> Unit = {},
    onUserProfileClick: (Int) -> Unit = {},
    onBackClick: () -> Unit = {},
    refreshTrigger: Int = 0,
    viewModel: FeedViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUserId = remember { SessionManager.getAuthenticatedUserId() }
    val scope = rememberCoroutineScope()

    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var showCustomVisibilityDialog by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedTopicId by rememberSaveable { mutableStateOf<Int?>(null) }
    var topicRowVisible by rememberSaveable { mutableStateOf(true) }
    var selectedPostForDelete by remember { mutableStateOf<Post?>(null) }
    var selectedEventForDelete by remember { mutableStateOf<PostEvent?>(null) }
    val generalListState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(refreshTrigger, profileUserId) { viewModel.loadFeed(profileUserId) }
    LaunchedEffect(currentUserId) { if (currentUserId != null) viewModel.loadCurrentUserProfile() }
    LaunchedEffect(generalListState) {
        var previousIndex = generalListState.firstVisibleItemIndex
        var previousOffset = generalListState.firstVisibleItemScrollOffset
        snapshotFlow { Triple(generalListState.firstVisibleItemIndex, generalListState.firstVisibleItemScrollOffset, generalListState.isScrollInProgress) }
            .collect { (index, offset, scrolling) ->
                if (scrolling) when {
                    index > previousIndex || (index == previousIndex && offset > previousOffset) -> topicRowVisible = false
                    index < previousIndex || (index == previousIndex && offset < previousOffset) -> topicRowVisible = true
                }
                previousIndex = index
                previousOffset = offset
            }
    }
    LaunchedEffect(showSearch, searchQuery, selectedTab, profileUserId) {
        viewModel.prepareSearch(searchQuery)
        val query = searchQuery.trim()
        if (!showSearch || query.length < 2 || selectedTab !in 0..2) return@LaunchedEffect
        delay(300)
        viewModel.search(query, tab = selectedTab, profileUserId = profileUserId)
    }

    fun closeSearch() {
        showSearch = false
        searchQuery = ""
        viewModel.clearSearch()
    }

    Scaffold(
        floatingActionButton = {
            if (profileUserId == null) {
                FloatingActionButton(onClick = {
                    when (selectedTab) {
                        2 -> showCustomVisibilityDialog = true
                        1 -> onCreatePostClick("EVENTS", VisibilityApiConfiguration(scope = null, value = null))
                        else -> onCreatePostClick("GENERAL", VisibilityApiConfiguration(VisibilityScope.PUBLIC))
                    }
                }, containerColor = OrangePrimary) {
                    Icon(Icons.Outlined.Add, contentDescription = "Create post", tint = Color.White)
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                listOf("General", "Events", "Custom").forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { closeSearch(); selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(selectedTextColor = OrangePrimary, selectedIconColor = OrangePrimary,
                            indicatorColor = OrangeLight, unselectedTextColor = HintColor),
                        label = { Text(label) }, icon = {}
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().background(if (selectedTab in 0..1) FeedWarmSurfaceColor else PostsBackground)) {
            if (selectedTab in 0..1) FeedWaveBackground()
            when (selectedTab) {
                0 -> androidx.compose.foundation.layout.Column(Modifier.fillMaxSize().padding(padding)) {
                    if (showSearch) {
                        FeedSearchBar(query = searchQuery, onQueryChange = { searchQuery = it }, onBack = ::closeSearch)
                        FeedTopBar(currentTitle = if (profileUserId == null) "General" else "Profile Posts", showSearchButton = false, showBrand = false)
                    } else {
                        FeedTopBar(currentTitle = if (profileUserId == null) "General" else "Profile Posts",
                            onSearchClick = { showSearch = true }, onBackClick = onBackClick.takeIf { profileUserId != null },
                            currentUserId = currentUserId, currentUserDisplayName = uiState.currentUserDisplayName,
                            currentUserAvatarUrl = uiState.currentUserAvatarUrl,
                            onCurrentUserClick = { currentUserId?.let(onUserProfileClick) })
                    }
                    GeneralPostsContent(
                        posts = uiState.posts, searchResults = uiState.generalSearchResults, tags = uiState.tags, tagCounts = uiState.tagCounts,
                        isLoading = uiState.isLoadingPosts, error = uiState.postsError, isLoadingTags = uiState.isLoadingTags,
                        tagsError = uiState.tagsError, isLoadingTagCounts = uiState.isLoadingTagCounts, tagCountsError = uiState.tagCountsError,
                        isSearching = uiState.isSearching, searchError = uiState.searchError, searchVisible = showSearch, searchQuery = searchQuery,
                        selectedTopicId = selectedTopicId, topicRowVisible = topicRowVisible, listState = generalListState,
                        profileUserId = profileUserId, voteError = uiState.voteError,
                        onTopicSelected = { selectedTopicId = it },
                        onPostClick = { post -> if (showSearch) closeSearch(); onPostClick(post.id, "GENERAL") },
                        onUpvote = { post -> scope.launch { viewModel.voteGeneralPost(post, VoteType.UPVOTE) } },
                        onDownvote = { post -> scope.launch { viewModel.voteGeneralPost(post, VoteType.DOWNVOTE) } },
                        onEdit = { onEditPostClick(it.id) }, onDelete = { selectedPostForDelete = it; viewModel.clearDeleteErrors() },
                        onAuthorClick = onUserProfileClick
                    )
                }
                1 -> androidx.compose.foundation.layout.Column(Modifier.fillMaxSize().padding(padding)) {
                    if (showSearch) {
                        FeedSearchBar(query = searchQuery, onQueryChange = { searchQuery = it }, onBack = ::closeSearch)
                        FeedTopBar(currentTitle = if (profileUserId == null) "Events" else "Profile Events", showSearchButton = false, showBrand = false)
                    } else {
                        FeedTopBar(currentTitle = if (profileUserId == null) "Events" else "Profile Events",
                            onSearchClick = { showSearch = true }, onBackClick = onBackClick.takeIf { profileUserId != null },
                            currentUserId = currentUserId, currentUserDisplayName = uiState.currentUserDisplayName,
                            currentUserAvatarUrl = uiState.currentUserAvatarUrl,
                            onCurrentUserClick = { currentUserId?.let(onUserProfileClick) })
                    }
                    EventPostsContent(
                        events = uiState.eventPosts, searchResults = uiState.eventSearchResults, isLoading = uiState.isLoadingEvents,
                        error = uiState.eventsError, searchVisible = showSearch, searchQuery = searchQuery, isSearching = uiState.isSearching,
                        searchError = uiState.searchError, votingPostIds = uiState.votingEventPostIds, voteError = uiState.voteError,
                        profileUserId = profileUserId, onRegister = { onRegisterEventClick(it.id) },
                        onOpen = { event -> event.postId?.let { if (showSearch) closeSearch(); onPostClick(it, "EVENTS") } },
                        onEdit = { it.postId?.let(onEditPostClick) }, onDelete = { selectedEventForDelete = it; viewModel.clearDeleteErrors() },
                        onUpvote = { event -> scope.launch { viewModel.voteEventPost(event, VoteType.UPVOTE) } },
                        onDownvote = { event -> scope.launch { viewModel.voteEventPost(event, VoteType.DOWNVOTE) } },
                        onOrganizerClick = onUserProfileClick
                    )
                }
                else -> androidx.compose.foundation.layout.Column(Modifier.fillMaxSize().padding(padding)) {
                    if (showSearch) {
                        FeedSearchBar(query = searchQuery, onQueryChange = { searchQuery = it }, onBack = ::closeSearch)
                        FeedTopBar(currentTitle = if (profileUserId == null) "Custom" else "Profile Custom Posts", showSearchButton = false, showBrand = false)
                    } else {
                        FeedTopBar(currentTitle = if (profileUserId == null) "Custom" else "Profile Custom Posts",
                            onSearchClick = { showSearch = true }, onBackClick = onBackClick.takeIf { profileUserId != null },
                            currentUserId = currentUserId, currentUserDisplayName = uiState.currentUserDisplayName,
                            currentUserAvatarUrl = uiState.currentUserAvatarUrl,
                            onCurrentUserClick = { currentUserId?.let(onUserProfileClick) })
                    }
                    CustomPostsContent(
                        posts = uiState.customPosts, searchResults = uiState.customSearchResults,
                        isLoading = uiState.isLoadingCustomPosts, error = uiState.customPostsError,
                        searchVisible = showSearch, searchQuery = searchQuery, isSearching = uiState.isSearching,
                        searchError = uiState.searchError, profileUserId = profileUserId, voteError = uiState.voteError,
                        onPostClick = { post -> if (showSearch) closeSearch(); onPostClick(post.id, "CUSTOM") },
                        onUpvote = { post -> scope.launch { viewModel.voteGeneralPost(post, VoteType.UPVOTE, custom = true) } },
                        onDownvote = { post -> scope.launch { viewModel.voteGeneralPost(post, VoteType.DOWNVOTE, custom = true) } },
                        onEdit = { onEditPostClick(it.id) },
                        onDelete = { selectedPostForDelete = it; viewModel.clearDeleteErrors() },
                        onAuthorClick = onUserProfileClick
                    )
                }
            }
        }
    }

    FeedDeleteDialogs(
        post = selectedPostForDelete, eventPost = selectedEventForDelete,
        isDeletingPost = uiState.isDeletingPost, postError = uiState.deletePostError,
        isDeletingEventPost = uiState.isDeletingEventPost, eventPostError = uiState.deleteEventPostError,
        onDismissPost = { selectedPostForDelete = null },
        onConfirmPost = { post -> scope.launch {
            if (viewModel.deleteGeneralPost(post.id, custom = post.postType.equals("CUSTOM", ignoreCase = true)).isSuccess) {
                selectedPostForDelete = null
            }
        } },
        onDismissEventPost = { selectedEventForDelete = null },
        onConfirmEventPost = { event -> event.postId?.let { postId -> scope.launch { if (viewModel.deleteEventPost(postId).isSuccess) selectedEventForDelete = null } } },
        onDismissErrors = { viewModel.clearDeleteErrors(); selectedPostForDelete = null; selectedEventForDelete = null }
    )

    if (showCustomVisibilityDialog) {
        CustomPostVisibilityDialog(
            onDismiss = { showCustomVisibilityDialog = false },
            onConfirm = { visibility ->
                showCustomVisibilityDialog = false
                onCreatePostClick("CUSTOM", visibility)
            }
        )
    }
}
