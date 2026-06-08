package com.example.campusconnect.feature.posts.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@Composable
fun PostDetailTopBar(

    onBackClick: () -> Unit
) {

    Surface(

        color = Color.Transparent
    ) {

        IconButton(

            onClick = onBackClick
        ) {

            Icon(

                imageVector = Icons.Default.ArrowBack,

                contentDescription = "Back"
            )
        }
    }
}