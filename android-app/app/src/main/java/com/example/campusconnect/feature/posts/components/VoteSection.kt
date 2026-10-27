package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        VoteButton(
            count = upvotes,
            isUpvote = true,
            contentDescription = "Upvote",
            selected = userVote == VoteType.UPVOTE,
            enabled = enabled,
            onClick = onUpvoteClick
        )
        VoteButton(
            count = downvotes,
            isUpvote = false,
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
    isUpvote: Boolean,
    contentDescription: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(15.dp)
    val activeColor = if (enabled && selected) OrangePrimary else HintColor
    val borderColor = if (enabled && selected) OrangePrimary else HintColor.copy(alpha = 0.28f)

    Box(
        modifier = Modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(role = Role.Button, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .widthIn(min = 48.dp)
                .height(29.dp)
                .clip(shape)
                .background(if (enabled && selected) OrangeLight else Color.White)
                .border(1.dp, borderColor, shape)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .size(17.dp)
                    .semantics { this.contentDescription = contentDescription }
            ) {
                val width = size.width
                val height = size.height
                fun point(x: Float, y: Float) = Offset(
                    x = width * x / 24f,
                    y = height * (if (isUpvote) y else 24f - y) / 24f
                )
                val arrow = Path()
                with(arrow) {
                    var currentX = 12f
                    var currentY = 1.5f
                    fun line(x: Float, y: Float) {
                        point(x, y).let { arrow.lineTo(it.x, it.y) }
                        currentX = x
                        currentY = y
                    }
                    fun curve(controlX: Float, controlY: Float, x: Float, y: Float) {
                        val firstControl = point(
                            currentX + (controlX - currentX) * (2f / 3f),
                            currentY + (controlY - currentY) * (2f / 3f)
                        )
                        val secondControl = point(
                            x + (controlX - x) * (2f / 3f),
                            y + (controlY - y) * (2f / 3f)
                        )
                        val end = point(x, y)
                        arrow.cubicTo(
                            firstControl.x, firstControl.y,
                            secondControl.x, secondControl.y,
                            end.x, end.y
                        )
                        currentX = x
                        currentY = y
                    }

                    point(12f, 1.5f).let { arrow.moveTo(it.x, it.y) }
                    curve(11f, 1.5f, 10.5f, 2f)
                    line(2.5f, 10.3f)
                    curve(1.5f, 11.4f, 2.7f, 12.5f)
                    line(7.3f, 12.5f)
                    line(7.3f, 21f)
                    curve(6.8f, 22f, 7.8f, 22f)
                    line(16.2f, 22f)
                    curve(17.2f, 22f, 16.7f, 21f)
                    line(16.7f, 12.5f)
                    line(21.3f, 12.5f)
                    curve(22.5f, 11.4f, 21.3f, 10.3f)
                    line(13.5f, 2f)
                    curve(13f, 1.5f, 12f, 1.5f)
                    arrow.close()
                }
                drawPath(path = arrow, color = activeColor)
            }
            Text(
                text = count.toString(),
                color = if (enabled && selected) OrangePrimary else HintColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
