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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.campusconnect.core.ui.theme.*
import com.example.campusconnect.feature.posts.components.FeedTopBar
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.models.PostTag
import com.example.campusconnect.feature.posts.models.PostTagCount
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
    refreshTrigger: Int = 0,
    viewModel: FeedViewModel = FeedViewModel()
) {
    var posts by remember {
    mutableStateOf<List<Post>>(emptyList())
}

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
    }


    suspend fun handleUpvote(post: Post) {

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
    }


    suspend fun handleDownvote(post: Post) {

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
    }


    var selectedTab by remember {
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
                .background(OrangeBg)
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
                                    }

                                )
                            }
                        }
                    }
                }

                1 -> {

                    Text(

                        text = "Events Feed Coming Soon",

                        modifier = Modifier.padding(padding)
                    )
                }

                2 -> {

                    Text(

                        text = "Custom Rooms Coming Soon",

                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}
