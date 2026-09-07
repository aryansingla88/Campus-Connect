package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.campusconnect.core.ui.theme.*
import com.example.campusconnect.feature.posts.models.Comment

@Composable
fun CommentCard(

    comment: Comment,

    isReply: Boolean = false
) {

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isReply) 48.dp else 12.dp,
                end = 12.dp,
                top = 8.dp,
                bottom = 8.dp
            )
    ) {

        if (isReply) {

            Text(

                text = "│",

                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Column {

            Row(

                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(

                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            OrangeLight,
                            CircleShape
                        ),

                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = comment.username.first().uppercase()
                    )
                }

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                Column {

                    Text(

                        text = comment.username,

                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(

                        text = comment.createdAt,

                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = comment.body
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Row {

                if (!isReply) {

                    Text("Reply")

                    Spacer(
                        modifier = Modifier.width(16.dp)
                    )
                }

                Text("⋮")
            }
        }
    }
}