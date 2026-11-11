package com.example.campusconnect.feature.map.ui.components.controls

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.core.components.PanelSearchBar
import com.example.campusconnect.core.components.AppAvatar
import com.example.campusconnect.feature.map.model.MapCurrentUser
import com.example.campusconnect.feature.map.ui.OrangeLight
import com.example.campusconnect.feature.map.ui.OrangePrimary
import com.example.campusconnect.feature.map.ui.TextDark
import com.example.campusconnect.feature.profile.ui.components.TextMuted
import com.example.campusconnect.feature.map.model.EventMarkerData
import com.example.campusconnect.feature.map.model.MapMarker
import com.example.campusconnect.feature.map.model.PoiMarkerData
import com.example.campusconnect.feature.map.model.UserMarkerData

internal enum class SidePanel { NONE, PROFILE, CHAT }

// Shared sizing for the floating top controls so the avatar, search pill and
// settings button stay visually consistent.
private val TopControlSize = 44.dp
private val TopAvatarSize = 48.dp
private val TopAvatarRing = 2.5.dp
private val TopSearchShape = RoundedCornerShape(30.dp)

private fun highlightedSearchLabel(label: String, query: String): AnnotatedString {
    val matchStart = label.indexOf(query.trim(), ignoreCase = true)
    if (matchStart < 0 || query.isBlank()) return AnnotatedString(label)

    val matchEnd = matchStart + query.trim().length
    return buildAnnotatedString {
        append(label.substring(0, matchStart))
        withStyle(
            SpanStyle(
                background = Color(0xFFFFE0C2),
                color = Color(0xFFB94D00)
            )
        ) {
            append(label.substring(matchStart, matchEnd))
        }
        append(label.substring(matchEnd))
    }
}

@Composable
internal fun TopMapControls(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchSuggestions: List<MapMarker>,
    currentUser: MapCurrentUser? = null,
    onSearchSuggestionSelected: (MapMarker) -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    showAccountActions: Boolean = true,
    showSearchSuggestions: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showAccountActions) {
            // Profile avatar with a white ring and soft shadow, floating over the map.
            Box(
                modifier = Modifier
                    .size(TopAvatarSize)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(onClick = onProfileClick)
                    .padding(TopAvatarRing),
                contentAlignment = Alignment.Center,
            ) {
                if (currentUser != null) {
                    AppAvatar(
                        entityId = currentUser.id,
                        displayName = currentUser.displayName,
                        imageUrl = currentUser.avatarUrl,
                        size = TopAvatarSize - TopAvatarRing * 2,
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = OrangePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            // Pill-shaped search bar with a soft drop shadow.
            Box(
                modifier = Modifier
                    .shadow(6.dp, TopSearchShape)
                    .clip(TopSearchShape)
            ) {
                PanelSearchBar(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = "Search campus..."
                )
            }

            if (showSearchSuggestions && searchQuery.trim().length >= 2 && searchSuggestions.isNotEmpty()) {
                val popupOffset = with(androidx.compose.ui.platform.LocalDensity.current) {
                    IntOffset(0, 54.dp.roundToPx())
                }
                Popup(
                    alignment = Alignment.TopStart,
                    offset = popupOffset,
                    onDismissRequest = { onSearchQueryChange("") },
                    properties = PopupProperties(focusable = false)
                ) {
                    Surface(
                        modifier = Modifier
                            .width(maxWidth)
                            .heightIn(max = 232.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        tonalElevation = 4.dp,
                        shadowElevation = 8.dp
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(searchSuggestions, key = { it.id }) { marker ->
                                val kind = when (marker) {
                                    is PoiMarkerData -> "Place"
                                    is UserMarkerData -> "User"
                                    is EventMarkerData -> "Event"
                                    else -> ""
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .clickable { onSearchSuggestionSelected(marker) }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = highlightedSearchLabel(marker.label, searchQuery),
                                        color = TextDark,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = kind,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAccountActions) {
            Spacer(modifier = Modifier.width(12.dp))
            RoundIconButton(
                icon = Icons.Default.Settings,
                contentDescription = "Settings",
                onClick = onSettingsClick
            )
        }
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
            .size(TopControlSize)
            .shadow(6.dp, CircleShape)
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
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
internal fun RightSideTabs(
    showFilters: Boolean,
    filtersAvailable: Boolean,
    expandedFilterRows: Int,
    selectedSidePanel: SidePanel,
    showSocialTabs: Boolean = true,
    onFiltersClick: () -> Unit,
    onBoardClick: () -> Unit,
    onPostsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val boardPostsOffset by animateDpAsState(
        targetValue = if (showFilters) {
            (24 + expandedFilterRows.coerceIn(0, 3) * 52).dp
        } else {
            0.dp
        },
        animationSpec = tween(durationMillis = 240),
        label = "filter_tabs_alignment"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (filtersAvailable) {
            // Keep the rail's original slot while the drawer is open, but hide
            // the Filters tab because the drawer itself is now attached to the
            // screen edge in that space.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (!showFilters) {
                    SideTab(
                        text = "FILTERS",
                        icon = Icons.Default.FilterList,
                        selected = false,
                        onClick = onFiltersClick
                    )
                }
            }
            if (showFilters) Spacer(Modifier.height(boardPostsOffset))
        }

        if (showSocialTabs) {
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
    // Solid orange pill with a white calendar-edit icon and bold white label,
    // floating over the map in Event mode.
    val pillShape = RoundedCornerShape(percent = 50)
    Row(
        modifier = modifier
            .height(36.dp)
            .shadow(6.dp, pillShape)
            .clip(pillShape)
            .background(OrangePrimary)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Manage Events",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}