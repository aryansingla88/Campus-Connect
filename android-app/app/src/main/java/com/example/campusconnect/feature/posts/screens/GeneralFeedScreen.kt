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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.campusconnect.core.ui.theme.*
import com.example.campusconnect.feature.posts.components.FeedTopBar
import com.example.campusconnect.feature.posts.models.dummyPosts
import com.example.campusconnect.feature.posts.components.TopicChipsRow
import com.example.campusconnect.feature.posts.models.dummyTags
import com.example.campusconnect.feature.posts.models.dummyComments
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.viewmodel.FeedViewModel

//@OptIn means ->"I know I'm using an experimental Material 3 API, and I accept that it may change in future versions."
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralFeedScreen(onPostClick: (Int) -> Unit) {
    val viewModel: FeedViewModel = viewModel()

    var posts by remember {

        mutableStateOf<List<Post>>(emptyList())
    }

    LaunchedEffect(Unit) {

        posts = viewModel
            .getPosts()
            .getOrDefault(emptyList())
    }


    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    var selectedTopic by remember {

        mutableStateOf<String?>(null)
    }

    val filteredPosts =

        if (selectedTopic == null) {

            posts
        }
        else {

            posts.filter {

                it.tags.any { tag ->

                    tag.name == selectedTopic
                }
            }
        }

    Scaffold(
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

                        // When a topic chip is clicked, update selectedTopic with that topic's name.
                        // This triggers recomposition, causing filteredPosts to be recalculated and
                        // only posts matching the selected topic to be displayed.
                        TopicChipsRow(

                            tags = dummyTags,

                            selectedTopic = selectedTopic,

                            onTopicSelected = {

                                selectedTopic = it
                            }
                        )

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

                            items(filteredPosts) { post ->

                                val commentCount =

                                    dummyComments.count {

                                        it.postId == post.id
                                    }

                                com.example.campusconnect.feature.posts.components.PostCard(

                                    post = post,

                                    commentCount = commentCount,

                                    onClick = {

                                        onPostClick(post.id)
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