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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusconnect.core.ui.theme.HintColor
import com.example.campusconnect.core.ui.theme.LabelColor
import com.example.campusconnect.core.ui.theme.OrangeLight
import com.example.campusconnect.core.ui.theme.OrangePrimary
import com.example.campusconnect.core.util.formatBackendDateTime
import com.example.campusconnect.feature.posts.models.Comment

@Composable
fun CommentCard(
    comment: Comment,
    isReply: Boolean = false,
    onReply: (Comment) -> Unit = {}
) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
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

            Text(
                text = comment.body,
                style = MaterialTheme.typography.bodyMedium,
                color = LabelColor
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isReply) {
                    Text(
                        text = "Reply",
                        color = OrangePrimary,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.clickable { onReply(comment) }
                    )
                }
                Text(
                    text = "⋮",
                    color = HintColor,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
