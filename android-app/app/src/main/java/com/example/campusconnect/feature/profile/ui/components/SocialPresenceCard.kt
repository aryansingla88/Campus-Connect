package com.example.campusconnect.feature.profile.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.R

private val SocialCardBg = Color.White
private val SocialDivider = Color(0xFFE5E5E5)
private val SocialText = Color(0xFF555555)
private val SocialAdd = Color(0xFFFF6D00)

@Composable
fun SocialPresenceCard(
    github: String?,
    linkedin: String?,
    instagram: String?,
    other: String?,
    onGithubClick: () -> Unit = {},
    onLinkedInClick: () -> Unit = {},
    onInstagramClick: () -> Unit = {},
    onOtherClick: () -> Unit = {}
) {
    val items = listOf(
    SocialPresenceItem(
        title = "GitHub",
        value = github,
        icon = {
            Image(
                painter = painterResource(R.drawable.github),
                contentDescription = "GitHub",
                modifier = Modifier.size(26.dp)
            )
        },
        onClick = onGithubClick
    ),
    SocialPresenceItem(
        title = "LinkedIn",
        value = linkedin,
        icon = {
            Image(
                painter = painterResource(R.drawable.linkedin),
                contentDescription = "LinkedIn",
                modifier = Modifier.size(26.dp)
            )
        },
        onClick = onLinkedInClick
    ),
    SocialPresenceItem(
        title = "Instagram",
        value = instagram,
        icon = {
            Image(
                painter = painterResource(R.drawable.instagram),
                contentDescription = "Instagram",
                modifier = Modifier.size(26.dp)
            )
        },
        onClick = onInstagramClick
    ),
    SocialPresenceItem(
        title = "Other",
        value = other,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Link,
                contentDescription = "Other",
                modifier = Modifier.size(26.dp),
                tint = Color(0xFF222222)
            )
        },
        onClick = onOtherClick
    )
)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(
                SocialCardBg,
                RoundedCornerShape(18.dp)
            )
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->

            if (index > 0) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(58.dp)
                        .background(SocialDivider)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { item.onClick() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                item.icon()

                Spacer(
                    modifier = Modifier.height(1.dp)
                )

                Text(
                    text = item.title,
                    fontSize = 11.sp,
                    color = SocialText
                )

                Text(
                    text = if (item.value.isNullOrBlank()) "Add" else "View",
                    fontSize = 9.sp,
                    color = SocialAdd
                )
            }
        }
    }
}

private data class SocialPresenceItem(
    val title: String,
    val value: String?,
    val icon: @Composable () -> Unit,
    val onClick: () -> Unit
)