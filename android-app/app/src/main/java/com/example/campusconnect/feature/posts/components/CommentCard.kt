package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusconnect.core.ui.theme.HintColor
import com.example.campusconnect.core.ui.theme.LabelColor
import com.example.campusconnect.core.ui.theme.OrangeBorder
import com.example.campusconnect.core.ui.theme.OrangeLight
import com.example.campusconnect.core.ui.theme.OrangePrimary
import com.example.campusconnect.core.ui.theme.ErrorRed
import com.example.campusconnect.core.ui.theme.White
import com.example.campusconnect.core.util.formatBackendDateTime
import com.example.campusconnect.feature.posts.models.Comment

@Composable
fun CommentCard(
    comment: Comment,
    isReply: Boolean = false,
    onReply: (Comment) -> Unit = {},
    onDelete: (Comment) -> Unit = {},
    onEdit: (Comment) -> Unit = {},
    isEditing: Boolean = false,
    editDraft: String = comment.body,
    onEditDraftChange: (String) -> Unit = {},
    isSavingEdit: Boolean = false,
    editError: String? = null,
    onSaveEdit: () -> Unit = {},
    onCancelEdit: () -> Unit = {}
) {
    var menuExpanded by remember(comment.id) { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isReply) 32.dp else 16.dp,
                end = 16.dp,
                top = 6.dp,
                bottom = 6.dp
            ),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(OrangeLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = comment.username.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.labelLarge,
                color = LabelColor,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.username,
                        style = MaterialTheme.typography.titleSmall,
                        color = LabelColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatBackendDateTime(comment.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = HintColor
                    )
                }

                if ((comment.canEdit || comment.canDelete) && !comment.isDeleted) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Text(
                                text = "⋮",
                                color = HintColor,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        MaterialTheme(
                            colorScheme = MaterialTheme.colorScheme.copy(
                                surface = OrangeLight,
                                primary = OrangePrimary
                            )
                        ) {
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                if (comment.canEdit) {
                                    DropdownMenuItem(
                                        text = { Text("Edit comment") },
                                        onClick = {
                                            menuExpanded = false
                                            onEdit(comment)
                                        }
                                    )
                                }
                                if (comment.canDelete) {
                                    DropdownMenuItem(
                                        text = { Text("Delete comment") },
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Reply",
                        color = OrangePrimary,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.clickable { onReply(comment) }
                    )
                }
            }
        }
    }
}
