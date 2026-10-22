package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.campusconnect.core.ui.theme.HintColor
import com.example.campusconnect.core.ui.theme.OrangeBg
import com.example.campusconnect.core.ui.theme.OrangeLight
import com.example.campusconnect.core.ui.theme.OrangePrimary
import com.example.campusconnect.core.util.formatBackendDateTime
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.utils.formatEventClockTime
import com.example.campusconnect.feature.posts.utils.formatEventDate
import com.example.campusconnect.feature.posts.utils.resolvePostImageUrl

@Composable
fun PostDetailCard(
    post: Post,
    commentCount: Int,
    onUpvoteClick: () -> Unit = {},
    onDownvoteClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    var showMenu by remember(post.id) { mutableStateOf(false) }
    val isEventPost = post.postType.equals("EVENTS", ignoreCase = true)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            if (isEventPost) {
                EventHeader(post)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).background(OrangeLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(post.username.firstOrNull()?.uppercase() ?: "?", color = OrangePrimary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(post.username, style = MaterialTheme.typography.bodyMedium)
                        Text(formatBackendDateTime(post.createdAt), style = MaterialTheme.typography.bodySmall, color = HintColor)
                        if (post.isEdited) Text("Edited", style = MaterialTheme.typography.labelSmall, color = HintColor)
                    }
                    Spacer(Modifier.weight(1f))
                    if (post.canDelete) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Outlined.MoreVert, contentDescription = "Post options", tint = HintColor)
                            }
                            MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(surface = OrangeLight, primary = OrangePrimary)) {
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(text = { Text("Edit post") }, onClick = { showMenu = false; onEditClick() })
                                    DropdownMenuItem(text = { Text("Delete post") }, onClick = { showMenu = false; onDeleteClick() })
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(post.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    post.tags.firstOrNull()?.name?.let { tagName ->
                        Surface(shape = RoundedCornerShape(50), color = OrangeLight) {
                            Text(tagName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = OrangePrimary)
                        }
                    }
                }
            }

            if (isEventPost) {
                post.eventCategoryName?.takeIf(String::isNotBlank)?.let { category ->
                    Surface(shape = RoundedCornerShape(50), color = OrangeLight, modifier = Modifier.padding(top = 10.dp)) {
                        Text(category, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = OrangePrimary)
                    }
                }
            }

            resolvePostImageUrl(post.imageUrl)?.let { imageUrl ->
                Spacer(Modifier.height(12.dp))
                AsyncImage(
                    model = imageUrl,
                    contentDescription = if (isEventPost) "${post.title} poster" else null,
                    modifier = Modifier.fillMaxWidth().height(if (isEventPost) 220.dp else 220.dp),
                    contentScale = ContentScale.Crop
                )
            }

            if (isEventPost) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    EventInfoCell(
                        icon = { Icon(Icons.Outlined.CalendarMonth, null, tint = OrangePrimary, modifier = Modifier.size(18.dp)) },
                        label = "DATE",
                        value = post.eventStartTime?.let(::formatEventDate).orEmpty(),
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    EventInfoCell(
                        icon = { Icon(Icons.Outlined.Schedule, null, tint = OrangePrimary, modifier = Modifier.size(18.dp)) },
                        label = "TIME",
                        value = post.eventStartTime?.let(::formatEventClockTime).orEmpty(),
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    EventInfoCell(
                        icon = { Icon(Icons.Outlined.LocationOn, null, tint = OrangePrimary, modifier = Modifier.size(18.dp)) },
                        label = "MAP",
                        value = post.eventVenue?.takeIf(String::isNotBlank)
                            ?: listOfNotNull(post.eventLatitude, post.eventLongitude).joinToString(", ").ifBlank { "Location TBD" },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }

            if (post.body.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(post.body, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VoteSection(
                    upvotes = post.upvotes,
                    downvotes = post.downvotes,
                    userVote = post.userVote,
                    onUpvoteClick = onUpvoteClick,
                    onDownvoteClick = onDownvoteClick
                )
                Text("💬 $commentCount")
            }
        }
    }
}

@Composable
private fun EventHeader(post: Post) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).background(OrangeLight, CircleShape), contentAlignment = Alignment.Center) {
            Text(post.eventOrganizerName?.firstOrNull()?.uppercase() ?: post.title.firstOrNull()?.uppercase() ?: "E", color = OrangePrimary)
        }
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Text(post.title, style = MaterialTheme.typography.titleMedium)
            Text(post.eventOrganizerName.orEmpty(), style = MaterialTheme.typography.bodySmall, color = HintColor)
        }
    }
}

@Composable
private fun EventInfoCell(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(modifier.background(OrangeBg, RoundedCornerShape(12.dp)).padding(9.dp), verticalAlignment = Alignment.Top) {
        icon()
        Column(Modifier.padding(start = 7.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = HintColor)
            Text(value, style = MaterialTheme.typography.bodySmall, color = Color.Black, maxLines = 2)
        }
    }
}
