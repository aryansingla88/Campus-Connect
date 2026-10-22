package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.core.ui.theme.HintColor
import com.example.campusconnect.core.ui.theme.OrangeLight
import com.example.campusconnect.core.ui.theme.OrangePrimary
import com.example.campusconnect.feature.posts.models.VoteType

@Composable
fun VoteSection(
    upvotes: Int,
    downvotes: Int,
    userVote: VoteType?,
    onUpvoteClick: () -> Unit,
    onDownvoteClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        VoteButton(
            count = upvotes,
            icon = Icons.Filled.ArrowUpward,
            contentDescription = "Upvote",
            selected = userVote == VoteType.UPVOTE,
            enabled = enabled,
            onClick = onUpvoteClick
        )
        VoteButton(
            count = downvotes,
            icon = Icons.Outlined.ArrowDownward,
            contentDescription = "Downvote",
            selected = userVote == VoteType.DOWNVOTE,
            enabled = enabled,
            onClick = onDownvoteClick
        )
    }
}

@Composable
private fun VoteButton(
    count: Int,
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(13.dp)
    val activeColor = if (enabled && selected) OrangePrimary else HintColor
    val borderColor = if (enabled && selected) OrangePrimary.copy(alpha = 0.55f) else HintColor.copy(alpha = 0.28f)

    Box(
        modifier = Modifier
            .sizeIn(minWidth = 60.dp, minHeight = 48.dp)
            .clickable(role = Role.Button, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .widthIn(min = 60.dp)
                .height(36.dp)
                .clip(shape)
                .background(if (enabled && selected) OrangeLight.copy(alpha = 0.55f) else Color.White)
                .border(1.dp, borderColor, shape)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = activeColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = count.toString(),
                color = if (enabled && selected) OrangePrimary else HintColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
