@file:OptIn(ExperimentalFoundationApi::class)

package com.example.campusconnect.feature.events.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.model.Event
import kotlinx.coroutines.launch

// ─── Theme ────────────────────────────────────────────────────────────────────
private val OrangePrimary = Color(0xFFFF6F00)
private val LabelColor    = Color(0xFF2A2A2A)
private val HintColor     = Color(0xFFAAAAAA)

// ─── EventPreviewSheet ────────────────────────────────────────────────────────

/**
 * Swipeable bottom-sheet event preview.
 *
 * Sync strategy — avoids the feedback loop:
 *  • Marker tap  → [activeIndex] changes → [LaunchedEffect] scrolls pager
 *                  → we set [markerTriggered] = true so the settle callback
 *                    knows NOT to call [onPageChanged] for that scroll.
 *  • Pager swipe → settle detected via [snapshotFlow] on [isScrollInProgress]
 *                  → only calls [onPageChanged] when [markerTriggered] is false.
 */
@Composable
fun EventPreviewSheet(
    events: List<Event>,
    activeIndex: Int = 0,
    onPageChanged: (Int) -> Unit,
    onClose: () -> Unit,
    onEdit: (Event) -> Unit = {},
    onRegistration: (Event) -> Unit = {},
    onChat: (Event) -> Unit = {},
    onAccess: (Event) -> Unit = {},
    onDelete: (Event) -> Unit = {},
) {
    if (events.isEmpty()) return

    val pagerState      = rememberPagerState(
        initialPage = activeIndex.coerceIn(0, events.lastIndex),
        pageCount   = { events.size }
    )
    val scope           = rememberCoroutineScope()

    // True while a marker-tap-initiated scroll is in flight.
    // Prevents the settle observer from echoing the change back to the ViewModel.
    val markerTriggered = remember { mutableStateOf(false) }

    // ── Marker tap → scroll pager ─────────────────────────────────────────────
    LaunchedEffect(activeIndex) {
        if (pagerState.currentPage != activeIndex ||
            pagerState.targetPage  != activeIndex) {
            markerTriggered.value = true
            pagerState.animateScrollToPage(
                page          = activeIndex.coerceIn(0, events.lastIndex),
                animationSpec = tween(durationMillis = 350)
            )
            // animateScrollToPage suspends until the animation finishes,
            // so we reset the flag here — after the page has settled.
            markerTriggered.value = false
        }
    }

    // ── Pager swipe → update ViewModel ────────────────────────────────────────
    // Watch isScrollInProgress: when it flips false the pager has settled.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.isScrollInProgress }
            .collect { scrolling ->
                if (!scrolling && !markerTriggered.value) {
                    onPageChanged(pagerState.currentPage)
                }
            }
    }

    Surface(
        modifier        = Modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color           = Color.White,
        tonalElevation  = 0.dp,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Handle + close ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp).height(4.dp)
                        .background(Color(0xFFE0E0E0), CircleShape)
                        .align(Alignment.TopCenter)
                )
                IconButton(
                    onClick  = onClose,
                    modifier = Modifier.align(Alignment.TopEnd).padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = "Close",
                        tint               = HintColor
                    )
                }
            }

            // ── Pager ─────────────────────────────────────────────────────────
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                EventCard(event = events[page])
            }

            // ── Action buttons ────────────────────────────────────────────────
            val currentEvent = events[pagerState.currentPage]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionBtn(
                    icon     = Icons.Default.Edit,
                    label    = "Edit",
                    onClick  = { onEdit(currentEvent) },
                    modifier = Modifier.weight(1f)
                )
                ActionBtn(
                    icon     = Icons.Default.PersonAdd,
                    label    = "Registration",
                    onClick  = { onRegistration(currentEvent) },
                    modifier = Modifier.weight(1f)
                )
                ActionBtn(
                    icon     = Icons.Default.Chat,
                    label    = "Chat",
                    onClick  = { onChat(currentEvent) },
                    modifier = Modifier.weight(1f)
                )
                ActionBtn(
                    icon     = Icons.Default.Shield,
                    label    = "Access",
                    onClick  = { onAccess(currentEvent) },
                    modifier = Modifier.weight(1f)
                )
                ActionBtn(
                    icon     = Icons.Default.Delete,
                    label    = "Delete",
                    iconTint = Color(0xFFD32F2F),
                    bgColor  = Color(0xFFFFF0F0),
                    onClick  = { onDelete(currentEvent) },
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Navigation row ────────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilledTonalIconButton(
                    onClick = {
                        val target = (pagerState.currentPage - 1 + events.size) % events.size
                        scope.launch {
                            markerTriggered.value = false   // user-initiated
                            pagerState.animateScrollToPage(target, animationSpec = tween(300))
                        }
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous event")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = "${pagerState.currentPage + 1} / ${events.size}",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color      = LabelColor
                    )
                    Text(
                        text     = "${events.size} events nearby",
                        fontSize = 11.sp,
                        color    = HintColor
                    )
                }

                FilledTonalIconButton(
                    onClick = {
                        val target = (pagerState.currentPage + 1) % events.size
                        scope.launch {
                            markerTriggered.value = false   // user-initiated
                            pagerState.animateScrollToPage(target, animationSpec = tween(300))
                        }
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next event")
                }
            }

            // ── Swipe hint ────────────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.SwapHoriz, null,
                    tint     = HintColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Swipe to browse all events", fontSize = 12.sp, color = HintColor)
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Default.SwapHoriz, null,
                    tint     = HintColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─── EventCard ────────────────────────────────────────────────────────────────

@Composable
private fun EventCard(event: Event) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(width = 110.dp, height = 130.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1A1A2E)),
            contentAlignment = Alignment.Center
        ) {
            if (!event.posterUrl.isNullOrBlank()) {
                // Replace with AsyncImage (Coil) when ready:
                // AsyncImage(model = event.posterUrl, contentDescription = event.title,
                //     modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Text(
                    text       = event.title.take(2).uppercase(),
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = OrangePrimary
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text       = event.title.split(" ").take(2)
                            .joinToString("\n") { it.uppercase() },
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = event.date.takeLast(4).ifBlank { "2025" },
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = OrangePrimary
                    )
                }
            }
        }

        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(Modifier.size(7.dp).background(OrangePrimary, CircleShape))
                Text(
                    text       = "QUICK PREVIEW",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color      = OrangePrimary
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text       = event.title,
                fontSize   = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color(0xFF1A1A1A),
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
                lineHeight = 24.sp
            )
            Text(
                text     = "By ${event.clubName.ifBlank { "Campus Club" }}",
                fontSize = 13.sp,
                color    = HintColor
            )

            Spacer(Modifier.height(8.dp))

            MetaRow(Icons.Default.DateRange,  event.date.ifBlank { "Date TBD" })
            MetaRow(
                icon = Icons.Default.Schedule,
                text = buildString {
                    append(event.startTime.ifBlank { "Time TBD" })
                    if (!event.endTime.isNullOrBlank()) append(" – ${event.endTime}")
                }
            )
            MetaRow(Icons.Default.LocationOn, event.venue.ifBlank { "Venue TBD" })
        }
    }
}

// ─── MetaRow ─────────────────────────────────────────────────────────────────

@Composable
private fun MetaRow(icon: ImageVector, text: String) {
    Row(
        modifier              = Modifier.padding(vertical = 3.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Icon(icon, null, tint = OrangePrimary, modifier = Modifier.size(15.dp))
        Text(text = text, fontSize = 13.sp, color = Color(0xFF444444))
    }
}

// ─── ActionBtn ───────────────────────────────────────────────────────────────

@Composable
private fun ActionBtn(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.White,
    bgColor: Color  = OrangePrimary,
) {
    Column(
        modifier              = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(24.dp)) {
            Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Text(
            text       = label,
            fontSize   = 10.sp,
            fontWeight = FontWeight.Medium,
            color      = iconTint,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis
        )
    }
}