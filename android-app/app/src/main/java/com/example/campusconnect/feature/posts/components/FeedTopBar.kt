package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search

import androidx.compose.material3.*

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.example.campusconnect.core.ui.theme.*

class FeedTopBar {

}
@Composable
fun FeedTopBar(

    currentTitle: String
) {
    Row(

        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),

        horizontalArrangement = Arrangement.SpaceBetween,

        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(

            color = OrangePrimary,

            shape = RoundedCornerShape(50)
        ) {

            Text(

                text = currentTitle,

                modifier = Modifier.padding(
                    horizontal = 20.dp,
                    vertical = 10.dp
                ),

                color = Color.White,

                style = MaterialTheme.typography.titleMedium
            )
        }
        Surface(
            color=Color.White,
            shape=RoundedCornerShape(16.dp),
            shadowElevation=4.dp
        ){
            IconButton(
                onClick={

                }
            ){
                Icon(
                    imageVector=Icons.Outlined.Search,
                    contentDescription="Search",
                    tint=OrangePrimary
                )
            }
        }

    }

}