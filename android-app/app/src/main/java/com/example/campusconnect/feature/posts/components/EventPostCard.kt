package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MoreVert
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.campusconnect.core.ui.theme.HintColor
import com.example.campusconnect.core.ui.theme.OrangeBg
import com.example.campusconnect.core.ui.theme.OrangeLight
import com.example.campusconnect.core.ui.theme.OrangePrimary
import com.example.campusconnect.feature.posts.models.PostEvent
import com.example.campusconnect.feature.posts.utils.formatEventClockTime
import com.example.campusconnect.feature.posts.utils.formatEventDate
import com.example.campusconnect.feature.posts.utils.resolvePostImageUrl

@Composable
fun EventPostCard(
    event: PostEvent,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onUpvoteClick: () -> Unit = {},
    onDownvoteClick: () -> Unit = {},
    isVoting: Boolean = false
) {
    var showMenu by remember(event.id) { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(42.dp).background(OrangeLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = event.hostName?.firstOrNull()?.uppercase() ?: event.title.firstOrNull()?.uppercase() ?: "E",
                        color = OrangePrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(event.title, style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            event.organizerName?.takeIf(String::isNotBlank)
                                ?: event.hostName.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = HintColor
                        )
                        if (event.isEdited) {
                            Text(
                                " · Edited",
                                style = MaterialTheme.typography.labelSmall,
                                color = HintColor
                            )
                        }
                    }
                }
                event.categoryName?.takeIf(String::isNotBlank)?.let { category ->
                    Surface(color = OrangeLight, shape = RoundedCornerShape(50)) {
                        Text(category, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = OrangePrimary, style = MaterialTheme.typography.labelMedium)
                    }
                }
                if (event.canDelete) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "Event post options", tint = HintColor)
                        }
                        MaterialTheme(
                            colorScheme = MaterialTheme.colorScheme.copy(
                                surface = OrangeLight,
                                primary = OrangePrimary
                            )
                        ) {
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Edit post") },
                                    onClick = { showMenu = false; onEditClick() }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete post") },
                                    onClick = { showMenu = false; onDeleteClick() }
                                )
                            }
                        }
                    }
                }
            }

            event.description
                ?.lineSequence()
                ?.firstOrNull()
                ?.trim()
                ?.takeIf(String::isNotBlank)
                ?.let { descriptionPreview ->
                    Text(
                        text = descriptionPreview,
                        modifier = Modifier.padding(top = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2
                    )
                }

            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
            ) {
                EventInfoCell(
                    icon = { Icon(Icons.Outlined.CalendarMonth, null, tint = OrangePrimary, modifier = Modifier.size(18.dp)) },
                    label = "DATE",
                    value = formatEventDate(event.startTime),
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                EventInfoCell(
                    icon = { Icon(Icons.Outlined.Schedule, null, tint = OrangePrimary, modifier = Modifier.size(18.dp)) },
                    label = "TIME",
                    value = formatEventClockTime(event.startTime),
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                EventInfoCell(
                    icon = { Icon(Icons.Outlined.LocationOn, null, tint = OrangePrimary, modifier = Modifier.size(18.dp)) },
                    label = "LOCATION",
                    value = event.venue?.takeIf(String::isNotBlank)
                        ?: listOfNotNull(event.latitude, event.longitude).joinToString(", ").ifBlank { "Location TBD" },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }

            resolvePostImageUrl(event.posterUrl)?.let { imageUrl ->
                Spacer(Modifier.height(12.dp))
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "${event.title} poster",
                    modifier = Modifier.fillMaxWidth().height(176.dp).clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VoteSection(
                    upvotes = event.upvotes,
                    downvotes = event.downvotes,
                    userVote = event.userVote,
                    onUpvoteClick = onUpvoteClick,
                    onDownvoteClick = onDownvoteClick,
                    enabled = !isVoting && event.postId != null
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comments", tint = OrangePrimary, modifier = Modifier.size(19.dp))
                    Text(" ${event.commentCount}", color = HintColor, style = MaterialTheme.typography.bodyMedium)
                }
            }
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
    Row(
        modifier = modifier.background(OrangeBg, RoundedCornerShape(12.dp)).padding(9.dp),
        verticalAlignment = Alignment.Top
    ) {
        icon()
        Column(Modifier.padding(start = 7.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = HintColor)
            Text(value, style = MaterialTheme.typography.bodySmall, color = Color.Black, maxLines = 2)
        }
    }
}
