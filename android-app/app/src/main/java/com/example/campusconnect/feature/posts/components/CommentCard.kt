package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.core.ui.theme.HintColor
import com.example.campusconnect.core.ui.theme.LabelColor
import com.example.campusconnect.core.ui.theme.OrangeBorder
import com.example.campusconnect.core.ui.theme.OrangePrimary
import com.example.campusconnect.core.ui.theme.ErrorRed
import com.example.campusconnect.core.ui.theme.White
import com.example.campusconnect.feature.posts.models.Comment
import com.example.campusconnect.feature.posts.utils.formatPostTimestamp
import com.example.campusconnect.core.components.AppAvatar

private val CommentAvatarSize = 34.dp
private val CommentHorizontalInset = 16.dp
private val CommentVerticalInset = 6.dp
private val CommentContentSpacing = 4.dp
private val CommentAuthorGap = 10.dp
private val CommentMenuTouchSize = 48.dp
private val CommentConnectorStroke = 1.dp
private val CommentConnectorColor = Color(0xFFD8D8D8)

@Composable
fun CommentCard(
    comment: Comment,
    isReply: Boolean = false,
    isLastReply: Boolean = false,
    hasReplies: Boolean = false,
    onReply: (Comment) -> Unit = {},
    onDelete: (Comment) -> Unit = {},
    onEdit: (Comment) -> Unit = {},
    isEditing: Boolean = false,
    editDraft: String = comment.body,
    onEditDraftChange: (String) -> Unit = {},
    isSavingEdit: Boolean = false,
    editError: String? = null,
    onSaveEdit: () -> Unit = {},
    onCancelEdit: () -> Unit = {},
    onAuthorClick: (Int) -> Unit = {}
) {
    var menuExpanded by remember(comment.id) { mutableStateOf(false) }
    val canShowMenu = (comment.canEdit || comment.canDelete) && !comment.isDeleted
    val menuVisualOffset = (CommentAvatarSize - CommentMenuTouchSize) / 2f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (isReply || hasReplies) {
                    val avatarSizePx = CommentAvatarSize.toPx()
                    val horizontalInsetPx = CommentHorizontalInset.toPx()
                    val verticalInsetPx = CommentVerticalInset.toPx()
                    val railX = horizontalInsetPx + avatarSizePx / 2f
                    val avatarCenterY = verticalInsetPx + avatarSizePx / 2f
                    val branchStartY = avatarCenterY - avatarSizePx / 4f
                    val replyAvatarLeftX = horizontalInsetPx + avatarSizePx
                    val strokeWidth = CommentConnectorStroke.toPx()
                    if (isReply) {
                        drawLine(
                            color = CommentConnectorColor,
                            start = Offset(railX, 0f),
                            end = Offset(
                                railX,
                                if (isLastReply) branchStartY else size.height
                            ),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                    if (hasReplies) {
                        drawLine(
                            color = CommentConnectorColor,
                            // Start below the parent avatar so the guide never crosses it.
                            start = Offset(railX, verticalInsetPx + avatarSizePx),
                            end = Offset(railX, size.height),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                    if (isReply) {
                        val branchWidth = replyAvatarLeftX - railX
                        val path = Path().apply {
                            moveTo(railX, branchStartY)
                            cubicTo(
                                railX,
                                avatarCenterY,
                                railX + branchWidth / 2f,
                                avatarCenterY,
                                replyAvatarLeftX,
                                avatarCenterY
                            )
                        }
                        drawPath(
                            path = path,
                            color = CommentConnectorColor,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }
            }
            .padding(
                start = if (isReply) CommentHorizontalInset + CommentAvatarSize else CommentHorizontalInset,
                end = CommentHorizontalInset,
                top = CommentVerticalInset,
                bottom = CommentVerticalInset
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            AppAvatar(
                entityId = comment.authorId,
                displayName = comment.authorDisplayName?.takeIf(String::isNotBlank) ?: comment.username,
                imageUrl = comment.authorAvatarUrl,
                size = CommentAvatarSize,
                fontSize = 13.sp,
                modifier = Modifier.clickable(enabled = comment.authorId != null) {
                    comment.authorId?.let(onAuthorClick)
                }
            )

            Spacer(modifier = Modifier.width(CommentAuthorGap))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(CommentContentSpacing)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = CommentAvatarSize)
                        .padding(end = if (canShowMenu) CommentMenuTouchSize else 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.authorDisplayName?.takeIf(String::isNotBlank) ?: comment.username,
                        modifier = Modifier.weight(1f, fill = false),
                        style = MaterialTheme.typography.titleSmall,
                        color = LabelColor,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatPostTimestamp(comment.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = HintColor,
                        maxLines = 1
                    )
                }

                if (isEditing) {
                    OutlinedTextField(
                        value = editDraft,
                        onValueChange = onEditDraftChange,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .align(Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = OrangeBorder,
                            cursorColor = OrangePrimary,
                            focusedContainerColor = White,
                            unfocusedContainerColor = White
                        ),
                        enabled = !isSavingEdit,
                        minLines = 2,
                        maxLines = 5
                    )
                    editError?.let { message ->
                        Text(
                            text = message,
                            color = ErrorRed,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = onSaveEdit,
                            enabled = !isSavingEdit && editDraft.isNotBlank()
                        ) {
                            Text(if (isSavingEdit) "Saving…" else "Save", color = OrangePrimary)
                        }
                        TextButton(onClick = onCancelEdit, enabled = !isSavingEdit) {
                            Text("Cancel", color = HintColor)
                        }
                    }
                } else if (comment.isDeleted) {
                    Text(
                        text = "Comment deleted",
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        color = HintColor
                    )
                } else {
                    Text(
                        text = comment.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LabelColor
                    )
                }

                if (!isReply && !comment.isDeleted && !isEditing) {
                    Text(
                        text = "Reply",
                        color = OrangePrimary,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.clickable { onReply(comment) }
                    )
                }
            }
        }

        if (canShowMenu) {
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Comment actions",
                        tint = HintColor,
                        modifier = Modifier
                            .size(24.dp)
                            .offset(y = menuVisualOffset)
                    )
                }
                MaterialTheme(
                    colorScheme = MaterialTheme.colorScheme.copy(
                        surface = White,
                        surfaceTint = White,
                        onSurface = Color.Black,
                        primary = OrangePrimary
                    )
                ) {
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        if (comment.canEdit) {
                            DropdownMenuItem(
                                text = { Text("Edit comment", color = Color.Black) },
                                onClick = {
                                    menuExpanded = false
                                    onEdit(comment)
                                }
                            )
                        }
                        if (comment.canEdit && comment.canDelete) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                color = Color(0xFFE2E2E2),
                                thickness = 1.dp
                            )
                        }
                        if (comment.canDelete) {
                            DropdownMenuItem(
                                text = { Text("Delete comment", color = Color.Black) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete(comment)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
