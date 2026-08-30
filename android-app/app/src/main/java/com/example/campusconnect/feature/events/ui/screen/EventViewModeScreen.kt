@file:OptIn(ExperimentalFoundationApi::class)

package com.example.campusconnect.feature.events.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.campusconnect.feature.events.model.Event
import kotlin.math.absoluteValue

// ─── Theme ────────────────────────────────────────────────────────────────────
private val Orange       = Color(0xFFFF4D00)
private val OrangeLight  = Color(0xFFFFF3E0)
private val TextDark     = Color(0xFF1A1A1A)
private val TextMuted    = Color(0xFF888888)

// ─── EventViewModeScreen ──────────────────────────────────────────────────────

/**
 * Full-screen "view mode" triggered when the user taps the Visibility button.
 *
 * Features:
 *  • Circular fan-style HorizontalPager: right of the last poster wraps to the
 *    first, left of the first wraps to the last.
 *  • Overlapping cards: the active (center) poster visually overlaps the
 *    left/right neighbours via negative page spacing + zIndex stacking.
 *  • Blurred version of the same poster covers the map background
 *  • Search bar + filter icon at top
 *  • Register Now (orange filled) + Notify Me (outlined) at bottom
 *  • Expand/fullscreen button bottom-right (square icon, no info icon)
 *  • Registered / Notified states shown as filled light variants
 *  • Handles portrait and landscape posters: blurred bg fills gaps
 *
 * @param events         All live events to browse
 * @param onBack         Back arrow pressed
 * @param onRegister     Register Now tapped; [isRegistered] tracks state
 * @param onNotify       Notify Me tapped; [isNotified] tracks state
 * @param onOpenDetail   Expand/fullscreen square button tapped
 */
@Composable
fun EventViewModeScreen(
    events: List<Event>,
    onBack: () -> Unit,
    onRegister: (Event) -> Unit = {},
    onNotify: (Event) -> Unit = {},
    onOpenDetail: (Event) -> Unit = {},
) {
    if (events.isEmpty()) {
        EmptyViewMode(onBack)
        return
    }

    var searchQuery by remember { mutableStateOf("") }
    var showFilter  by remember { mutableStateOf(false) }

    // Per-event registered / notified state (local fake state — wire to VM later)
    var registeredIds by remember { mutableStateOf(setOf<Int>()) }
    var notifiedIds   by remember { mutableStateOf(setOf<Int>()) }

    val filtered = remember(searchQuery, events) {
        if (searchQuery.isBlank()) events
        else events.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.clubName.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    // Virtual page count = 999 * events so swiping either direction feels infinite.
    // Start in the middle so both prev and next always exist. The actual event
    // shown for any virtual page is `page % filtered.size`, which is what makes
    // this wrap seamlessly: swipe past the last real event and you land back on
    // the first one (and vice-versa going backwards).
    val isCircular = filtered.size >= 2

    val virtualCount = when {
        filtered.isEmpty() -> 1
        isCircular -> filtered.size * 999
        else -> filtered.size
    }

    val startPage = when {
        filtered.isEmpty() -> 0
        isCircular -> filtered.size * 499
        else -> 0
    }

    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { virtualCount }
    )

    LaunchedEffect(filtered) {
        if (filtered.isNotEmpty()) {
            pagerState.scrollToPage(startPage)
        }
    }

    val currentEvent = filtered.getOrNull(
        if (filtered.isNotEmpty()) {
            if (isCircular) {
                pagerState.currentPage % filtered.size
            } else {
                pagerState.currentPage.coerceIn(0, filtered.lastIndex)
            }
        } else {
            0
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // ── BLURRED MAP / POSTER BACKGROUND ──────────────────────────────────
        // Layer 1: light map placeholder (white/grey — represents the light map)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0EFE9))  // light map tone
        )

        // Layer 2: blurred poster of current event bleeds into bg
        currentEvent?.let { event ->
            PosterBlurredBackground(event = event)
        }

        // Layer 3: soft white gradient overlay so content stays readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f   to Color(0x55FFFFFF),
                        0.3f to Color(0x22FFFFFF),
                        0.7f to Color(0x44FFFFFF),
                        1f   to Color(0xCCFFFFFF)
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // ── TOP BAR ───────────────────────────────────────────────────────
            TopBar(
                searchQuery   = searchQuery,
                onQueryChange = { searchQuery = it },
                onBack        = onBack,
                onFilter      = { showFilter = !showFilter }
            )

            Spacer(Modifier.height(8.dp))

            // ── POSTER CAROUSEL (circular + overlapping) ────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (filtered.isEmpty()) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {

                            // Search icon
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        color = Color(0xFFFFE4D6),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = Color(0xFFFF4D00),
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            Spacer(Modifier.height(20.dp))

                            Text(
                                text = "No events found",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A),
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "We couldn't find any events matching",
                                fontSize = 14.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = "\"$searchQuery\"",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFF4D00),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(Modifier.height(18.dp))

                            Text(
                                text = "Try searching for another event, club or category.",
                                fontSize = 12.sp,
                                color = TextMuted.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }

                } else {
                    HorizontalPager(
                        state          = pagerState,
                        // Larger horizontal padding narrows each page slot so
                        // neighbours peek in from both edges.
                        // NEGATIVE spacing is what actually creates the overlap:
                        // it pulls adjacent page slots closer than their own
                        // width, so their drawn content overlaps in the shared
                        // region. zIndex below then decides who's on top.
                        contentPadding = PaddingValues(
                            horizontal = if (isCircular) 72.dp else 24.dp
                        ),
                        pageSpacing = if (isCircular) (-52).dp else 0.dp,
                        modifier       = Modifier.fillMaxSize()
                    ) { page ->
                        val pageOffset = (pagerState.currentPage - page) +
                                pagerState.currentPageOffsetFraction
                        val distance = pageOffset.absoluteValue.coerceIn(0f, 1f)

                        // Side cards shrink...
                        val scale by animateFloatAsState(
                            targetValue   = 1f - 0.28f * distance,
                            animationSpec = tween(200),
                            label         = "poster_scale_$page"
                        )
                        // ...and dim...
                        val alpha by animateFloatAsState(
                            targetValue   = 1f - 0.35f * distance,
                            animationSpec = tween(200),
                            label         = "poster_alpha_$page"
                        )

                        val actualIndex = if (isCircular) {
                            page % filtered.size
                        } else {
                            page
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxHeight(0.78f)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                    // Pull side cards IN, under the center card.
                                    // pageOffset is negative for the right
                                    // neighbour and positive for the left one,
                                    // so translating by +pageOffset (no extra
                                    // negation) moves each neighbour back
                                    // toward the center rather than away from
                                    // it — this is what previously had the
                                    // wrong sign and cancelled the overlap.
                                    if (isCircular) {
                                        translationX = pageOffset * size.width * 0.34f
                                    }
                                }
                                // Center card (distance ~0) always paints last,
                                // i.e. on top of both neighbours.
                                .zIndex(if (isCircular) 2f - distance else 1f)
                                .clip(RoundedCornerShape(24.dp))
                        ) {
                            // `page % filtered.size` is the wraparound: this is
                            // what makes the poster to the right of the last
                            // event be the first event again.
                            PosterCard(event = filtered[actualIndex])
                        }
                    }
                }
            }

            // ── BOTTOM ACTIONS ────────────────────────────────────────────────
            currentEvent?.let { event ->
                BottomActions(
                    event = event,
                    isRegistered = event.id in registeredIds,
                    isNotified = event.id in notifiedIds,

                    onRegister = {
                        registeredIds =
                            if (event.id in registeredIds) {
                                registeredIds - event.id
                            } else {
                                registeredIds + event.id
                            }

                        onRegister(event)
                    },

                    onNotify = {
                        notifiedIds =
                            if (event.id in notifiedIds) {
                                notifiedIds - event.id
                            } else {
                                notifiedIds + event.id
                            }

                        onNotify(event)
                    },

                    onExpand = {
                        onOpenDetail(event)
                    }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── FILTER BOTTOM SHEET (simple) ──────────────────────────────────────────
    if (showFilter) {
        FilterSheet(onDismiss = { showFilter = false })
    }
}

// ─── PosterBlurredBackground ──────────────────────────────────────────────────
// Blurred version of the current event's poster fills the screen background.
// If no posterUrl, falls back to a solid orange-tinted gradient.

@Composable
private fun PosterBlurredBackground(event: Event) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .blur(radius = 28.dp)            // heavy blur
            .graphicsLayer { alpha = 0.45f } // semi-transparent
    ) {
        if (!event.posterUrl.isNullOrBlank()) {
            AsyncImage(
                model             = event.posterUrl,
                contentDescription = null,
                contentScale      = ContentScale.Crop,
                modifier          = Modifier.fillMaxSize()
            )
        } else {
            // Colour-coded fallback from category
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(
                                categoryAccent(event.category).copy(alpha = 0.6f),
                                Color(0xFFF0EFE9)
                            )
                        )
                    )
            )
        }
    }
}

// ─── PosterCard ───────────────────────────────────────────────────────────────
// The main poster cell. Handles any aspect ratio:
// • Blurred version of the same image fills the full card area first
// • Then the actual image is rendered centred at its natural aspect ratio
// • This avoids cropping while still filling the card edge-to-edge

@Composable
private fun PosterCard(event: Event) {
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (!event.posterUrl.isNullOrBlank()) {
            // Layer 1: blurred fill (handles odd aspect ratios)
            AsyncImage(
                model              = event.posterUrl,
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxSize()
                    .blur(12.dp)
            )

            // Layer 2: actual poster, fitted (no crop)
            AsyncImage(
                model              = event.posterUrl,
                contentDescription = event.title,
                contentScale       = ContentScale.Fit,
                modifier           = Modifier.fillMaxSize()
            )
        } else {
            // Fallback placeholder
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1A1A2E), categoryAccent(event.category))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text       = event.title.split(" ").take(2)
                            .joinToString("\n") { it.uppercase() },
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White,
                        textAlign  = TextAlign.Center,
                        lineHeight = 38.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text       = event.date,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color      = categoryAccent(event.category)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text     = event.venue,
                        fontSize = 12.sp,
                        color    = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Gradient overlay at bottom for readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xCC000000))
                    )
                )
        )

        // Event name at bottom of card
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text       = event.title,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CalendarToday, null,
                    tint     = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(12.dp)
                )
                Text(event.date, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                Text("·", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                Icon(
                    Icons.Default.LocationOn, null,
                    tint     = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    event.venue,
                    fontSize = 12.sp,
                    color    = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Live badge if applicable
            if (event.status.name == "LIVE") {
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFFD32F2F), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("LIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// ─── TopBar ───────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onFilter: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Back button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint     = TextDark,
                modifier = Modifier.size(20.dp)
            )
        }

        // Search bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint     = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value         = searchQuery,
                        onValueChange = onQueryChange,
                        singleLine    = true,
                        textStyle     = TextStyle(
                            fontSize = 13.sp,
                            color    = TextDark
                        ),
                        modifier      = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    "Search events...",
                                    fontSize = 13.sp,
                                    color    = TextMuted
                                )
                            }
                            inner()
                        }
                    )
                }
                if (searchQuery.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0F0F0))
                            .clickable { onQueryChange("") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close, null,
                            tint     = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        // Filter button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onFilter() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Tune,
                contentDescription = "Filter",
                tint     = TextDark,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─── BottomActions ────────────────────────────────────────────────────────────

@Composable
private fun BottomActions(
    event: Event,
    isRegistered: Boolean,
    isNotified: Boolean,
    onRegister: () -> Unit,
    onNotify: () -> Unit,
    onExpand: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 42.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // =========================
        // REGISTER / REGISTERED
        // =========================

        Button(
            onClick = onRegister,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                width = 1.dp,
                color = Color(0xFFFF4D00)
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRegistered) {
                    Color(0xFFFFF3E0)
                } else {
                    Color(0xFFFF4D00)
                },
                contentColor = if (isRegistered) {
                    Color(0xFFFF4D00)
                } else {
                    Color.White
                }
            ),
            contentPadding = PaddingValues(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = if (isRegistered) {
                    Icons.Default.Check
                } else {
                    Icons.Default.ConfirmationNumber
                },
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.width(7.dp))

            Text(
                text = if (isRegistered) {
                    "Registered"
                } else {
                    "Register Now"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }


        // =========================
        // NOTIFY / NOTIFIED
        // =========================

        OutlinedButton(
            onClick = onNotify,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                width = 1.dp,
                color = Color(0xFFFF4D00)
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isNotified) {
                    Color(0xFFFFF3E0)
                } else {
                    Color.White
                },
                contentColor = Color(0xFFFF4D00)
            ),
            contentPadding = PaddingValues(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = if (isNotified) {
                    Icons.Default.Check
                } else {
                    Icons.Default.NotificationsNone
                },
                contentDescription = null,
                modifier = Modifier.size(19.dp)
            )

            Spacer(Modifier.width(7.dp))

            Text(
                text = if (isNotified) {
                    "Notified"
                } else {
                    "Notify Me"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


// ─── FilterSheet ─────────────────────────────────────────────────────────────

@Composable
private fun FilterSheet(onDismiss: () -> Unit) {
    val categories = listOf("All", "Technology", "Cultural", "Sports", "Academic", "Workshop")
    var selected by remember { mutableStateOf("All") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* absorb clicks */ },
            shape          = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color          = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Handle
                Box(
                    modifier = Modifier
                        .width(36.dp).height(4.dp)
                        .background(Color(0xFFE0E0E0), CircleShape)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(16.dp))

                Text("Filter by category", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextDark)
                Spacer(Modifier.height(14.dp))

                // Category chips
                categories.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { cat ->
                            val isSelected = selected == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) Orange else Color(0xFFF0F0F0))
                                    .clickable { selected = cat }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    cat,
                                    fontSize   = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color      = if (isSelected) Color.White else TextMuted
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick  = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Orange)
                ) {
                    Text("Apply", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─── EmptyViewMode ────────────────────────────────────────────────────────────

@Composable
private fun EmptyViewMode(onBack: () -> Unit) {
    Box(
        modifier         = Modifier.fillMaxSize().background(Color(0xFFF0EFE9)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.EventBusy, null,
                tint     = TextMuted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text("No events to show", fontSize = 16.sp, color = TextMuted)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onBack,
                shape   = RoundedCornerShape(12.dp),
                colors  = ButtonDefaults.buttonColors(containerColor = Orange)
            ) {
                Text("Go Back", color = Color.White)
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun categoryAccent(category: String): Color = when (category.lowercase()) {
    "technology", "tech" -> Color(0xFFFF6F00)
    "cultural", "music"  -> Color(0xFF7B3FC4)
    "sports", "sport"    -> Color(0xFF1A7FC1)
    "academic"           -> Color(0xFF1D9E75)
    "workshop"           -> Color(0xFFE89B10)
    else                 -> Color(0xFFFF6F00)
}