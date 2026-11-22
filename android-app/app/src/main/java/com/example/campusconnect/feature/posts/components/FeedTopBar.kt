package com.example.campusconnect.feature.posts.components
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search

import androidx.compose.material3.*

import androidx.compose.runtime.Composable
import androidx.compose.foundation.text.BasicTextField

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.campusconnect.core.ui.theme.*

@Composable
fun FeedTopBar(
    currentTitle: String,
    onSearchClick: () -> Unit = {},
    showSearchButton: Boolean = true,
    onNotificationsClick: () -> Unit = {},
    showBrand: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (showBrand) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = OrangePrimary)) { append("CC") }
                        withStyle(SpanStyle(color = LabelColor)) { append(" Discussions") }
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 4.dp
                ) {
                    Box {
                        IconButton(onClick = onNotificationsClick) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsNone,
                                contentDescription = "Notifications",
                                tint = LabelColor
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 8.dp, end = 8.dp)
                                .size(8.dp)
                                .background(OrangePrimary, CircleShape)
                        )
                    }
                }
                if (showSearchButton) {
                    Spacer(Modifier.width(12.dp))
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 4.dp
                    ) {
                        IconButton(onClick = onSearchClick) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = OrangePrimary
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(color = OrangePrimary, shape = RoundedCornerShape(50)) {
                Text(
                    text = currentTitle,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall
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
