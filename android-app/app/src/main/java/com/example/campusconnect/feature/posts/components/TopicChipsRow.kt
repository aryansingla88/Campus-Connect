package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp

import com.example.campusconnect.core.ui.theme.*

import com.example.campusconnect.feature.posts.models.Topic
import com.example.campusconnect.feature.posts.models.dummyPosts

@Composable
fun TopicChipsRow(

    topics: List<Topic>,

    selectedTopic: String?,

    onTopicSelected: (String?) -> Unit
){
    Row(
        verticalAlignment=Alignment.CenterVertically
    ) {
        Box(

            modifier = Modifier
                .size(34.dp)
                .background(
                    color = OrangeLight,
                    shape = CircleShape
                )
                .clickable {

                    onTopicSelected(null)
                },

            contentAlignment = Alignment.Center
        ) {

            Text("🔥")
        }

        LazyRow(

            horizontalArrangement = Arrangement.spacedBy(10.dp),

            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            /*
        TOPIC CHIPS
         */

            items(topics) { topic ->

                val postCount =

                    dummyPosts.count {

                        topic.name in it.tags
                    }

                TopicChip(

                    topic = topic,

                    postCount = postCount,

                    isSelected = selectedTopic == topic.name,

                    onClick = {

                        onTopicSelected(topic.name)
                    }
                )
            }
        }
    }
}