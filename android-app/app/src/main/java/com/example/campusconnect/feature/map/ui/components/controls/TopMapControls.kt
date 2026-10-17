package com.example.campusconnect.feature.map.ui.components.controls

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.core.components.PanelSearchBar
import com.example.campusconnect.feature.map.ui.OrangeLight
import com.example.campusconnect.feature.map.ui.OrangePrimary
import com.example.campusconnect.feature.map.ui.TextDark

internal enum class SidePanel { NONE, PROFILE, CHAT }
@Composable
internal fun TopMapControls(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundIconButton(
            icon = Icons.Default.Person,
            contentDescription = "Profile",
            onClick = onProfileClick
        )

        Spacer(modifier = Modifier.width(14.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(30.dp))
        ) {
            PanelSearchBar(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = "Search"
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        RoundIconButton(
            icon = Icons.Default.Settings,
            contentDescription = "Settings",
            onClick = onSettingsClick
        )
    }
}

@Composable
internal fun RoundIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = OrangePrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
internal fun RightSideTabs(
    showFilters: Boolean,
    filtersAvailable: Boolean,
    selectedSidePanel: SidePanel,
    onFiltersClick: () -> Unit,
    onBoardClick: () -> Unit,
    onPostsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (filtersAvailable) {
            SideTab(
                text = "FILTERS",
                icon = Icons.Default.FilterList,
                selected = showFilters,
                onClick = onFiltersClick
            )
        }

        SideTab(
            text = "BOARD",
            icon = Icons.Default.Chat,
            selected = selectedSidePanel == SidePanel.CHAT,
            onClick = onBoardClick
        )

        SideTab(
            text = "POSTS",
            icon = Icons.Default.Article,
            selected = selectedSidePanel == SidePanel.PROFILE,
            onClick = onPostsClick
        )
    }
}

@Composable
internal fun SideTab(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabShape = RoundedCornerShape(
        topStart = 16.dp,
        bottomStart = 16.dp,
        topEnd = 0.dp,
        bottomEnd = 0.dp
    )

    val singleTabColor = Color.White

    Surface(
        modifier = modifier
            .width(40.dp)
            .height(108.dp)
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null
            ) {
                onClick()
            },
        shape = tabShape,
        color = singleTabColor,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(singleTabColor)
                .border(
                    width = if (selected) 1.5.dp else 0.dp,
                    color = if (selected) OrangePrimary else Color.Transparent,
                    shape = tabShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .requiredWidth(108.dp)
                    .graphicsLayer {
                        rotationZ = -90f
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = if (selected) OrangePrimary else TextDark,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = text,
                    color = if (selected) OrangePrimary else TextDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable

internal fun EventShortcutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Event,
            contentDescription = "Events",
            tint = OrangePrimary,
            modifier = Modifier.size(21.dp)
        )
    }
}
