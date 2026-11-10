package com.example.campusconnect.feature.posts.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.campusconnect.core.ui.theme.HintColor
import com.example.campusconnect.core.ui.theme.LabelColor
import com.example.campusconnect.core.ui.theme.OrangeLight
import com.example.campusconnect.core.ui.theme.OrangePrimary
import com.example.campusconnect.core.components.AppAvatar
import com.example.campusconnect.core.util.formatBackendDateTime
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.utils.resolvePostImageUrl

// Local style tokens for the card
private val CardCream = Color(0xFFFFFBF8)
private val CardBorder = Color(0xFFF1E9E2)
private val PillBorder = Color(0xFFE6DFD8)
private val DividerColor = Color(0xFFF0EAE4)
private val OrangePillBorder = Color(0xFFFFD9BF)

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

    // Works whether userVote is an Int (1 / -1), or a String/enum ("UP" / "DOWN").
    // Adjust if your Post model uses something different.
    val vote = post.userVote.toString().uppercase()
    val isUpvoted = vote == "1" || vote == "UP" || vote == "UPVOTE"
    val isDownvoted = vote == "-1" || vote == "DOWN" || vote == "DOWNVOTE"

    val cardShape = RoundedCornerShape(28.dp)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, cardShape)
            .clickable { onClick() },
        shape = cardShape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = CardCream)
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {

            // ---------------- HEADER ----------------
            val topicName = post.tags.firstOrNull()?.name?.takeIf(String::isNotBlank)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppAvatar(
                    entityId = post.authorId,
                    displayName = post.authorDisplayName?.takeIf(String::isNotBlank) ?: post.username,
                    imageUrl = post.authorAvatarUrl,
                    size = 50.dp,
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, end = 8.dp)
                ) {
                    Text(
                        text = post.authorDisplayName?.takeIf(String::isNotBlank) ?: post.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LabelColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatBackendDateTime(post.createdAt) +
                                if (post.isEdited) "  •  Edited" else "",
                        modifier = Modifier.padding(top = 2.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = HintColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (topicName != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(OrangeLight)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = topicName,
                            modifier = Modifier.widthIn(max = 110.dp),
                            color = OrangePrimary,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
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
                                tint = LabelColor
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

            Spacer(modifier = Modifier.height(20.dp))

            // ---------------- TITLE ----------------
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LabelColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ---------------- BODY ----------------
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyLarge,
                color = LabelColor.copy(alpha = 0.85f),
                lineHeight = 26.sp
            )

            // ---------------- IMAGE ----------------
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

            Spacer(modifier = Modifier.height(16.dp))

            // ---------------- DIVIDER ----------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(DividerColor)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ---------------- ACTION ROW ----------------
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionPill(
                        icon = remember(isUpvoted) { blockArrow(up = true, filled = isUpvoted) },
                        count = post.upvotes,
                        contentDescription = "Upvote",
                        active = isUpvoted,
                        onClick = onUpvoteClick
                    )
                    ActionPill(
                        icon = remember(isDownvoted) { blockArrow(up = false, filled = isDownvoted) },
                        count = post.downvotes,
                        contentDescription = "Downvote",
                        active = isDownvoted,
                        onClick = onDownvoteClick
                    )
                }

                ActionPill(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    count = commentCount,
                    contentDescription = "Comments",
                    active = false,
                    onClick = onClick
                )
            }
        }
    }
}

/**
 * Rounded, bordered pill used for upvote / downvote / comments.
 * Active state = light orange fill + orange border + orange icon.
 */
@Composable
private fun ActionPill(
    icon: ImageVector,
    count: Int,
    contentDescription: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    val iconTint = if (active) OrangePrimary else LabelColor

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(shape)
            .background(if (active) OrangeLight else Color.White)
            .border(1.dp, if (active) OrangePillBorder else PillBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = LabelColor
        )
    }
}

/**
 * Block arrow (head + stem) like the reference: solid when [filled],
 * hollow outline otherwise. Tinted by the Icon that uses it.
 */
private fun blockArrow(up: Boolean, filled: Boolean): ImageVector {
    fun y(v: Float) = if (up) v else 24f - v

    return ImageVector.Builder(
        name = if (up) "BlockArrowUp" else "BlockArrowDown",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = if (filled) SolidColor(Color.Black) else null,
        stroke = if (filled) null else SolidColor(Color.Black),
        strokeLineWidth = 1.8f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(12f, y(3f))
        lineTo(20.5f, y(12f))
        lineTo(15.5f, y(12f))
        lineTo(15.5f, y(20.5f))
        lineTo(8.5f, y(20.5f))
        lineTo(8.5f, y(12f))
        lineTo(3.5f, y(12f))
        close()
    }.build()
}
