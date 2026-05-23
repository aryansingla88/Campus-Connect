package com.example.campusconnect.feature.posts.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.campusconnect.core.ui.theme.*
import com.example.campusconnect.feature.posts.components.FeedTopBar
import com.example.campusconnect.feature.posts.models.dummyPosts
import com.example.campusconnect.feature.posts.components.TopicChipsRow
import com.example.campusconnect.feature.posts.models.dummyTopics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralFeedScreen() {

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    Scaffold(
        bottomBar = {

            NavigationBar(

                containerColor = Color.White
            ) {

                NavigationBarItem(

                    selected = selectedTab == 0,

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
                    /*
                    In Jetpack Compose, LazyColumn is a vertically scrolling list that only renders
                     the items currently visible on the screen. It is the modern, declarative equivalent
                      of the RecyclerView used in the older Android View system
                     */

                    LazyColumn(

                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 12.dp),

                        verticalArrangement = Arrangement.spacedBy(12.dp),

                        contentPadding = PaddingValues(vertical = 12.dp)

                    ) {
                        item {

                            FeedTopBar(

                                currentTitle = when (selectedTab) {

                                    0 -> "General"
                                    1 -> "Events"
                                    else -> "Custom"
                                }
                            )
                        }
                        item {

                            TopicChipsRow(
                                topics = dummyTopics
                            )
                        }

                        items(dummyPosts) { post ->

                            com.example.campusconnect.feature.posts.components.PostCard(
                                post = post,
                                onClick = {}
                            )
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