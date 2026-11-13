package com.example.campusconnect.feature.posts.components
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search

import androidx.compose.material3.*

import androidx.compose.runtime.Composable
import androidx.compose.foundation.text.BasicTextField

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.example.campusconnect.core.ui.theme.*

@Composable
fun FeedTopBar(
    currentTitle: String,
    onSearchClick: () -> Unit = {},
    showSearchButton: Boolean = true
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
        if (showSearchButton) Surface(
            color=Color.White,
            shape=RoundedCornerShape(16.dp),
            shadowElevation=4.dp
        ){
            IconButton(
                onClick = onSearchClick
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

@Composable
fun FeedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Close search",
                tint = OrangePrimary
            )
        }
        Surface(
            modifier = Modifier.weight(1f),
            color = Color.White,
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = OrangePrimary)
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF202124)),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text("Search posts", color = HintColor, style = MaterialTheme.typography.bodyLarge)
                        }
                        inner()
                    }
                )
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear search",
                            tint = HintColor
                        )
                    }
                }
            }
        }
    }
}
