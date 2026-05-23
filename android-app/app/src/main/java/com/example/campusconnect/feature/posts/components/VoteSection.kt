package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VoteSection(
    upvotes: Int,
    downvotes: Int
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("⬆ $upvotes")

        Text("⬇ $downvotes")
    }
}