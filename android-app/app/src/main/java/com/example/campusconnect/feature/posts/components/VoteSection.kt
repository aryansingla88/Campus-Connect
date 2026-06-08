package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.example.campusconnect.core.ui.theme.OrangePrimary
import com.example.campusconnect.feature.posts.models.VoteType

@Composable
fun VoteSection(

    upvotes: Int,

    downvotes: Int,

    userVote: VoteType?,

    onUpvoteClick: () -> Unit,

    onDownvoteClick: () -> Unit
) {

    Row(

        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(

            text = "⬆ $upvotes",

            color =
                if (userVote == VoteType.UPVOTED)
                    OrangePrimary
                else
                    Color.Unspecified,

            modifier = Modifier.clickable {

                onUpvoteClick()
            }
        )

        Text(

            text = "⬇ $downvotes",

            color =
                if (userVote == VoteType.DOWNVOTED)
                    OrangePrimary
                else
                    Color.Unspecified,

            modifier = Modifier.clickable {

                onDownvoteClick()
            }
        )
    }
}