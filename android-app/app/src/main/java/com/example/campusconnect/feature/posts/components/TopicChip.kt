package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.dp

import com.example.campusconnect.core.ui.theme.*

import com.example.campusconnect.feature.posts.models.Topic

@Composable
fun TopicChip(

    topic: Topic
) {

    Row(

        modifier = Modifier
            .background(

                color = Color.White,

                shape = RoundedCornerShape(50)
            )
            .padding(
                horizontal = 14.dp,
                vertical = 8.dp
            ),

        horizontalArrangement = Arrangement.spacedBy(6.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        /*
        TOPIC NAME
         */

        Text(

            text = topic.name,

            color = LabelColor,

            style = MaterialTheme.typography.labelMedium
        )

        /*
        POST COUNT
         */

        Text(

            text = topic.postCount.toString(),

            color = OrangePrimary,

            style = MaterialTheme.typography.labelMedium
        )
    }
}