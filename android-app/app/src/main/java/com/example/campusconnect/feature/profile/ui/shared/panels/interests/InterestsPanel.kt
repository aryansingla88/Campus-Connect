package com.example.campusconnect.feature.profile.ui.shared.panels.interests

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.profile.ui.shared.*

/**
 * Interests panel for both MyProfile (isOwner = true → editable) and
 * ViewProfile (isOwner = false → read-only).
 *
 * @param interests      Current list of interest tags.
 * @param isOwner        When true the remove button and the add-interest
 *                       button are shown.
 * @param onRemove       Called with the tag string when remove is tapped.
 * @param onAddClick     Called when the add-interest "+" button is tapped
 *                       (triggers ManageInterestsPanel in the parent).
 */
@Composable
fun InterestsPanel(
    interests: List<String>,
    isOwner: Boolean = false,
    onRemove: (String) -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Interests",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Add button – only for owner; triggers ManageInterestsPanel
            if (isOwner) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Orange)
                        .clickable { onAddClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Interest",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            interests.forEach { tag ->
                ProfileListCard(
                    title = tag,
                    subtitle = interestCategory(tag),
                    trailingContent = if (isOwner) {
                        {
                            IconButton(onClick = { onRemove(tag) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else null
                )
            }
        }
    }
}

private fun interestCategory(tag: String) = when (tag) {
    "AI/ML"            -> "Technology"
    "Web Development"  -> "Development"
    "UI/UX Design"     -> "Design"
    "Photography"      -> "Creative"
    "Gaming"           -> "Entertainment"
    else               -> "Interest"
}
