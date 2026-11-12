
package com.example.campusconnect.feature.posts.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

import com.example.campusconnect.core.components.AppAvatar
import com.example.campusconnect.core.ui.theme.*
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.utils.resolvePostImageUrl
import com.example.campusconnect.feature.posts.utils.formatPostTimestamp

@Composable
fun PostCard(
    post: Post,
    commentCount: Int,
    onClick: () -> Unit,
    onUpvoteClick: () -> Unit,
    onDownvoteClick: () -> Unit,
    onDeleteClick: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    var showMenu by remember(post.id) { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE6E7EA), RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 3.dp,
            pressedElevation = 5.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 4.dp
            )
        ) {

            /*
            ---------------- HEADER ----------------
            */

            val topicName = post.tags.firstOrNull()?.name?.takeIf(String::isNotBlank)

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Updated avatar: uses shared AppAvatar component
                    val authorDisplayName = post.authorDisplayName
                        ?.takeIf(String::isNotBlank)
                        ?: post.username

                    AppAvatar(
                        entityId = post.authorId,
                        displayName = authorDisplayName,
                        imageUrl = post.authorAvatarUrl,
                        size = 42.dp
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp, end = 4.dp)
                    ) {
                        Text(
                            text = post.username,
                            style = MaterialTheme.typography.labelLarge,
                            color = LabelColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = formatPostTimestamp(post.createdAt),
                            modifier = Modifier.padding(top = 2.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = HintColor,
                            maxLines = 1
                        )

                        if (post.isEdited) {
                            Text(
                                text = "Edited",
                                style = MaterialTheme.typography.labelSmall,
                                color = HintColor
                            )
                        }
                    }

                    if (topicName != null) {
                        Surface(
                            color = OrangeLight,
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(
                                text = topicName,
                                modifier = Modifier
                                    .widthIn(max = 120.dp)
                                    .padding(horizontal = 9.dp, vertical = 5.dp),
                                color = OrangePrimary,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (post.canDelete) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    Icons.Outlined.MoreVert,
                                    contentDescription = "Post options",
                                    tint = HintColor
                                )
                            }

                            MaterialTheme(
                                colorScheme = MaterialTheme.colorScheme.copy(
                                    surface = OrangeLight,
                                    primary = OrangePrimary
                                )
                            ) {
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit post") },
                                        onClick = {
                                            showMenu = false
                                            onEditClick()
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text("Delete post") },
                                        onClick = {
                                            showMenu = false
                                            onDeleteClick()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            /*
            ---------------- TITLE ----------------
            */

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                color = LabelColor
            )

            /*
            ---------------- IMAGE ----------------
            */

            val resolvedImageUrl = resolvePostImageUrl(post.imageUrl)

            if (resolvedImageUrl != null) {
                Log.d("POST_IMAGE", "Image URL = ${post.imageUrl}")

                Spacer(modifier = Modifier.height(16.dp))

                AsyncImage(
                    model = resolvedImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            /*
            ---------------- BODY ----------------
            */

            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium,
                color = LabelColor.copy(alpha = 0.8f),
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            /*
            ---------------- ACTION ROW ----------------
            */

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                VoteSection(
                    upvotes = post.upvotes,
                    downvotes = post.downvotes,
                    userVote = post.userVote,
                    onUpvoteClick = onUpvoteClick,
                    onDownvoteClick = onDownvoteClick
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = OrangePrimary,
                        modifier = Modifier.size(19.dp)
                    )

                    Text(" $commentCount", color = HintColor)
                }
            }
        }
    }
}
