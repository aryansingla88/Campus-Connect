package com.example.campusconnect.feature.registrations.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Orange used by the header's primary action (sampled from the design reference). */
private val HeaderActionOrange = Color(0xFFFA4200)

/**
 * Shared top bar for every registration screen: back arrow, the event name as
 * the heading with Published / Draft as its subtitle, and an optional action
 * slot pinned to the right edge.
 *
 * Screens without an action (setup, team questions) simply leave [action] null.
 */
@Composable
fun RegistrationHeader(
    eventName: String,
    isPublished: Boolean,
    onBack: () -> Unit,
    action: (@Composable RowScope.() -> Unit)? = null,
) {
    Surface(
        color = CardBg,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp, end = 8.dp)
            ) {
                Text(
                    text = eventName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            if (isPublished) {
                                Color(0xFFDFF3E3)
                            } else {
                                Color(0xFFFFE4D6)
                            }
                        )
                        .padding(
                            horizontal = 9.dp,
                            vertical = 3.dp
                        )
                ) {
                    Text(
                        text = if (isPublished) "Published" else "Draft",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPublished) {
                            Color(0xFF237A35)
                        } else {
                            Color(0xFFE64A00)
                        },
                        maxLines = 1
                    )
                }
            }

            if (action != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    content = action
                )
            }
        }
    }
}

/** Filled orange "Publish" pill-rect with a paper-plane icon, for the header's action slot. */
@Composable
fun HeaderActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HeaderActionOrange,
            contentColor = Color.White,
        ),
        elevation = null,
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun RegistrationBottomBar(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = CardBg,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            content = content
        )
    }
}
