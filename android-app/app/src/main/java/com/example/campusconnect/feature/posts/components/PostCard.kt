package com.example.campusconnect.feature.posts.components

/*This line imports the clickable modifier from Compose Foundation library.*/
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

/*below import helps us use-
Row,Column,Box,Spacer,padding(),fillMaxWidth(),fillMaxSize(),height(),width(),Arrangement,Alignment*/
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*

/*below line helps us import the @composable annotation*/
import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

/* This imports different ways of fitting/scaling content (mostly images) inside a given space-*/
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import coil.compose.AsyncImage

import com.example.campusconnect.core.ui.theme.*

import com.example.campusconnect.feature.posts.models.Post

@Composable
fun PostCard(

    post: Post,

    commentCount: Int,

    onClick: () -> Unit
) {

    ElevatedCard(

        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },

        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            /*
            ---------------- HEADER ----------------
             */

            Row(

                modifier = Modifier.fillMaxWidth(),

                /*
                In a Row() in Jetpack Compose, this line:
                'horizontalArrangement = Arrangement.SpaceBetween'
                controls how child items are spaced horizontally inside the row.

                SpaceBetween means:
                First item goes to the start
                Last item goes to the end
                Remaining space is distributed between the items
                 */
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                /*
                LEFT SECTION
                 */

                Row {

                    /*
                    DP CIRCLE
                     */

                    Box(

                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                OrangeLight,
                                shape = CircleShape
                            ),

                        contentAlignment = Alignment.Center
                    ) {

                        Text(

                            text = post.username.first().uppercase(),

                            color = OrangePrimary,

                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    /*
                    Spacer(modifier=Modifier.height(10.dp)) <- this is used for height in column
                    Spacer(modifier=Modifier.width(10.dp)) <- this is used for width in row
                     */

                    Spacer(modifier = Modifier.width(12.dp))

                    /*
                    USERNAME + TIME
                     */

                    Column {

                        Text(

                            text = post.username,

                            style = MaterialTheme.typography.labelMedium,

                            color = LabelColor
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(

                            text = post.createdAt,

                            style = MaterialTheme.typography.bodySmall,

                            color = HintColor
                        )
                    }
                }

                /*
                RIGHT SECTION
                 */

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    /*
                    TAG CHIP
                     */

                    Surface(

                        color = OrangeLight,

                        shape = RoundedCornerShape(50)
                    ) {

                        Text(

                            post.tags.firstOrNull()?.name ?: "",

                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            ),

                            color = OrangePrimary,

                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    /*
                    THREE DOT MENU
                     */

                    Text(

                        text = "⋮",

                        color = HintColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            /*
            ---------------- TITLE ----------------
             */

            Text(

                text = post.title,

                style = MaterialTheme.typography.titleMedium,

                color = LabelColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            /*
            ---------------- BODY ----------------
             */

            Text(

                text = post.body,

                style = MaterialTheme.typography.bodyMedium,

                color = LabelColor.copy(alpha = 0.8f),

                lineHeight = 24.sp
            )

            /*
            ---------------- IMAGE ----------------
             */

            if (post.imageUrl != null) {

                Spacer(modifier = Modifier.height(16.dp))

                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(20.dp)),

                    /*
                    contentScale = ContentScale.Crop tells the image to:
                    Fill the entire available space
                    Maintain the image's aspect ratio
                    Crop extra parts if needed
                     */
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            /*
            ---------------- ACTION ROW ----------------
             */

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
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

                    text = "💬 $commentCount",
                    color = HintColor
                )
            }
        }
    }
}