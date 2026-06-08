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
import androidx.compose.foundation.clickable

@Composable
fun TopicChip(

    topic: Topic,

    postCount: Int,

    isSelected: Boolean,

    onClick: () -> Unit

    /*
    isSelected: Boolean,

    onClick: () -> Unit
     */
)

{
    Row(

        modifier = Modifier

            .background(

                color =
                    if (isSelected)
                        OrangePrimary
                    else
                        Color.White,

                shape = RoundedCornerShape(50)
            )

            .clickable {

                onClick()
            }
            .padding(

                horizontal = 18.dp,

                vertical = 10.dp
            ),

        horizontalArrangement = Arrangement.spacedBy(6.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        /*
        TOPIC NAME
         */

        Text(

            text = topic.name,

            color =
                if (isSelected)
                    Color.White
                else
                    LabelColor,

            style = MaterialTheme.typography.labelMedium
        )

        /*
        POST COUNT
         */

        Text(

            text = postCount.toString(),

            color =
                if (isSelected)
                    Color.White
                else
                    OrangePrimary,

            style = MaterialTheme.typography.labelMedium
        )
    }
}