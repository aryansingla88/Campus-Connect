package com.example.campusconnect.feature.profile.ui.panels.interests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.profile.ui.components.*

@Composable
fun ManageInterestsPanel(
    interests: List<String>,
    allInterests: List<String>,
    onAddInterest: (String) -> Unit
) {

    val availableInterests =
        allInterests.filterNot {
            it in interests
        }

    if (availableInterests.isEmpty()) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "You've explored all interests",
                fontSize = 13.sp,
                color = TextMuted
            )
        }

    } else {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = 12.dp,
                vertical = 14.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(availableInterests) { interest ->

                ProfileListCard(
                    title = interest,
                    subtitle = interestCategory(interest),

                    trailingContent = {

                        IconButton(
                            onClick = {
                                onAddInterest(interest)
                            }
                        ) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = "Add Interest",
                                tint = Orange
                            )
                        }
                    }
                )
            }
        }
    }
}

private fun interestCategory(tag: String) = when (tag) {
    "AI/ML" -> "Technology"
    "Web Development" -> "Development"
    "UI/UX Design" -> "Design"
    "Photography" -> "Creative"
    "Gaming" -> "Entertainment"
    "Open Source" -> "Technology"
    "Hackathons" -> "Career"
    "App Development" -> "Development"
    "Cyber Security" -> "Technology"
    "Data Science" -> "Technology"
    "Public Speaking" -> "Career"
    "Content Creation" -> "Creative"
    "Competitive Coding" -> "Technology"
    "Football" -> "Sports"
    "Basketball" -> "Sports"
    "Music" -> "Creative"
    "Dance" -> "Creative"
    "Entrepreneurship" -> "Career"
    "Startups" -> "Career"
    "Graphic Design" -> "Design"
    else -> "Interest"
}