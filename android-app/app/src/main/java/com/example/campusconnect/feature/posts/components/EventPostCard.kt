package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.ScrollState
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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.campusconnect.core.ui.theme.HintColor
import com.example.campusconnect.core.ui.theme.OrangeBg
import com.example.campusconnect.core.ui.theme.OrangeLight
import com.example.campusconnect.core.ui.theme.OrangePrimary
import com.example.campusconnect.feature.posts.models.PostEvent
import com.example.campusconnect.feature.posts.utils.formatEventClockTime
import com.example.campusconnect.feature.posts.utils.formatEventDateWithoutYear
import com.example.campusconnect.feature.posts.utils.resolvePostImageUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

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
    var isVisible by remember(event.id) { mutableStateOf(false) }
    var marqueeEnabled by remember(event.id) { mutableStateOf(false) }
    val view = LocalView.current

    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(2_000)
            marqueeEnabled = true
        } else {
            marqueeEnabled = false
        }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                val bounds = coordinates.boundsInWindow()
                val visible = view.height > 0 && bounds.bottom > 0f && bounds.top < view.height.toFloat()
                if (isVisible != visible) isVisible = visible
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(14.dp)) {
            Column {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(42.dp)
                            .background(OrangeLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = event.hostName?.firstOrNull()?.uppercase()
                                ?: event.title.firstOrNull()?.uppercase() ?: "E",
                            color = OrangePrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp, end = 4.dp)
                    ) {
                        MarqueeValue(
                            value = event.title,
                            enabled = marqueeEnabled,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = event.organizerName?.takeIf(String::isNotBlank)
                                ?: event.hostName.orEmpty(),
                            modifier = Modifier.padding(top = 2.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = HintColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (event.isEdited) {
                            Text(
                                text = "Edited",
                                style = MaterialTheme.typography.labelSmall,
                                color = HintColor
                            )
                        }
                    }
                    if (event.canDelete) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    Icons.Outlined.MoreVert,
                                    contentDescription = "Event post options",
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

                event.categoryName?.takeIf(String::isNotBlank)?.let { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(color = OrangeLight, shape = RoundedCornerShape(18.dp)) {
                            Text(
                                text = category,
                                modifier = Modifier
                                    .widthIn(max = 180.dp)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                color = OrangePrimary,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
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
                modifier = Modifier.fillMaxWidth()
            ) {
                EventInfoCell(
                    icon = { Icon(Icons.Outlined.CalendarMonth, null, tint = OrangePrimary, modifier = Modifier.size(18.dp)) },
                    label = "DATE",
                    value = formatEventDateWithoutYear(event.startTime),
                    modifier = Modifier.weight(1f),
                    marqueeEnabled = marqueeEnabled
                )
                EventInfoCell(
                    icon = { Icon(Icons.Outlined.Schedule, null, tint = OrangePrimary, modifier = Modifier.size(18.dp)) },
                    label = "TIME",
                    value = formatEventClockTime(event.startTime),
                    modifier = Modifier.weight(1f),
                    marqueeEnabled = marqueeEnabled
                )
                EventInfoCell(
                    icon = { Icon(Icons.Outlined.LocationOn, null, tint = OrangePrimary, modifier = Modifier.size(18.dp)) },
                    label = "LOCATION",
                    value = event.venue?.takeIf(String::isNotBlank)
                        ?: listOfNotNull(event.latitude, event.longitude).joinToString(", ").ifBlank { "Location TBD" },
                    modifier = Modifier.weight(1f),
                    marqueeEnabled = marqueeEnabled
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
    modifier: Modifier = Modifier,
    marqueeEnabled: Boolean
) {
    Column(
        modifier = modifier
            .background(OrangeBg, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon()
            Text(
                text = label,
                modifier = Modifier.padding(start = 5.dp).weight(1f),
                style = MaterialTheme.typography.labelSmall,
                color = HintColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip
            )
        }
        Spacer(Modifier.height(4.dp))
        MarqueeValue(value = value, enabled = marqueeEnabled)
    }
}

@Composable
private fun MarqueeValue(
    value: String,
    enabled: Boolean,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodySmall
) {
    val scrollState = remember(value) { ScrollState(0) }
    val density = LocalDensity.current
    LaunchedEffect(enabled, value, scrollState) {
        if (!enabled) {
            scrollState.scrollTo(0)
            return@LaunchedEffect
        }

        // Wait until horizontalScroll has measured the unwrapped text and viewport.
        val maxScroll = snapshotFlow { scrollState.maxValue }
            .first { it in 1 until Int.MAX_VALUE }
        val durationMillis = with(density) {
            ((maxScroll.toDp().value / 32f) * 1_000f).roundToInt().coerceAtLeast(1)
        }

        scrollState.scrollTo(0)
        while (true) {
            scrollState.animateScrollTo(
                value = scrollState.maxValue,
                animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing)
            )
            delay(900)
            // Restart at the first character after the end has been readable.
            scrollState.scrollTo(0)
            delay(900)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
            .horizontalScroll(scrollState, enabled = false)
    ) {
        Text(
            text = value,
            style = style,
            color = Color.Black,
            maxLines = 1,
            softWrap = false
        )
    }
}
