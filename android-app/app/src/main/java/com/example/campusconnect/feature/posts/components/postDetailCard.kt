package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

import coil.compose.AsyncImage

import com.example.campusconnect.core.ui.theme.*
import com.example.campusconnect.feature.posts.models.Post

@Composable
fun PostDetailCard(

    post: Post,

    commentCount: Int
) {

    ElevatedCard(

        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),

        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        )
    ) {

        Column(

            modifier = Modifier.padding(16.dp)
        ) {

            /*
            ---------------- USER HEADER ----------------
             */

            Row(

                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(

                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            OrangeLight,
                            CircleShape
                        ),

                    contentAlignment = Alignment.Center
                ) {

                    Text(

                        text = post.username.first().uppercase(),

                        color = OrangePrimary
                    )
                }

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Column {

                    Text(

                        text = post.username,

                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(

                        text = post.createdAt,

                        style = MaterialTheme.typography.bodySmall,

                        color = HintColor
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            /*
            ---------------- TITLE + TAG ----------------
             */

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween,

                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(

                    text = post.title,

                    style = MaterialTheme.typography.titleMedium,

                    modifier = Modifier.weight(1f)
                )

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Surface(

                    shape = RoundedCornerShape(50),

                    color = OrangeLight
                ) {

                    Text(

                        post.tags.firstOrNull()?.name ?: "",

                        style = MaterialTheme.typography.bodySmall,

                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),

                        color = OrangePrimary
                    )
                }
            }

            /*
            ---------------- IMAGE ----------------
             */

            if (post.imageUrl != null) {

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                AsyncImage(

                    model = post.imageUrl,

                    contentDescription = null,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),

                    contentScale = ContentScale.Crop
                )
            }

            /*
            ---------------- BODY ----------------
             */

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(

                text = post.body,

                style = MaterialTheme.typography.bodyMedium
            )

            /*
            ---------------- ACTIONS ----------------
             */

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween,

                verticalAlignment = Alignment.CenterVertically
            ) {

                VoteSection(

                    upvotes = post.upvotes,

                    downvotes = post.downvotes,

                    userVote = post.userVote,

                    onUpvoteClick = {

                    },

                    onDownvoteClick = {

                    }
                )

                Text(
                    text = "💬 $commentCount"
                )
            }
        }
    }
}