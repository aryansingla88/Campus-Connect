package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex

import androidx.compose.ui.unit.dp

import com.example.campusconnect.core.ui.theme.*

import com.example.campusconnect.feature.posts.models.PostTag
import com.example.campusconnect.feature.posts.models.PostTagCount

@Composable
fun TopicChipsRow(

    tags: List<PostTag>,

    tagCounts: List<PostTagCount>?,

    countsLoading: Boolean,

    countsError: String?,

    selectedTopic: Int?,

    onTopicSelected: (Int?) -> Unit
){
    Box(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .drawWithCache {
                    val fireStart = 8.dp.toPx()
                    val fireDiameter = 36.dp.toPx()
                    val fadeWidth = fireDiameter + 16.dp.toPx()
                    val solidWidthFraction = (fireStart + fireDiameter) / (fireStart + fadeWidth)
                    val fadeBrush = Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0f to PostsBackground,
                            solidWidthFraction to PostsBackground,
                            1f to Color.Transparent
                        ),
                        startX = 0f,
                        endX = fireStart + fadeWidth
                    )
                    onDrawWithContent {
                        drawContent()
                        drawRect(
                            brush = fadeBrush,
                            topLeft = Offset.Zero,
                            size = Size(fireStart + fadeWidth, size.height)
                        )
                    }
                },
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(start = 60.dp, end = 16.dp)
        ) {
            /*
        TOPIC CHIPS
         */

            items(tags) { tag ->
                val postCount = tagCounts
                    ?.firstOrNull { count -> count.id == tag.id }
                    ?.postCount

                TopicChip(

                    tag = tag,

                    postCount = postCount,

                    isSelected = selectedTopic == tag.id,

                    onClick = {

                        onTopicSelected(tag.id)
                    }
                )
            }
        }

        // Keep the fire control fixed above the scrolling row. Chips scroll under
        // its circular surface instead of being cut off at a straight edge.
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 8.dp)
                .size(36.dp)
                .zIndex(1f)
                .background(color = OrangeLight, shape = CircleShape)
                .clickable { onTopicSelected(null) },
            contentAlignment = Alignment.Center
        ) {
            Text("🔥")
        }
    }

    when {
        countsLoading -> Text("Loading topic counts…")
        countsError != null -> Text("Topic counts unavailable: $countsError")
    }
}
