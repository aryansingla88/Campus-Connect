@file:OptIn(ExperimentalFoundationApi::class)

package com.example.campusconnect.feature.events.ui.preview

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.campusconnect.feature.events.model.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

// ─── Theme ────────────────────────────────────────────────────────────────────
private val OrangePrimary = Color(0xFFFF6F00)
private val LabelColor    = Color(0xFF2A2A2A)
private val HintColor     = Color(0xFFAAAAAA)

// Participants nib — right edge, vertically centred on the screen.
private val NibWidth     = 36.dp
private val NibHeight    = 52.dp
private val DismissDrag  = 110.dp  // drag distance that closes the sheet

// Circular pager: a virtual page count, page % size picks the real event.
// Keep this SMALL. With ~1,000,000 pages the pager's absolute scroll offset
// (page × width) overflows Float precision (~16.7M px), so offsets round off by
// tens of pixels and the pager settles stuck between two pages / scrolls jerkily.
// 2,000 pages = ±1,000 swipes from the middle, offsets stay exact.
private const val LoopPages = 2_000
private fun loopStartPage(index: Int, count: Int) = (LoopPages / 2) - ((LoopPages / 2) % count) + index

// ─── EventPreviewSheet ────────────────────────────────────────────────────────

/**
 * Swipeable bottom-sheet event preview.
 *
 * Why this is NOT a ModalBottomSheet any more:
 * ModalBottomSheet lives in its own window, and the participants drawer used to
 * open in a second Dialog window on top of it. Two windows fighting over touch +
 * focus is what froze the sheet and made the nib vanish. Everything now lives in
 * the screen's own composition (scrim + sheet + nib here, drawer in EventScreen),
 * so nothing can block anything else.
 *
 * Fill the parent: call this inside a `Box(Modifier.fillMaxSize())`.
 *
 * Closing: tap the dimmed background, drag the sheet / handle / nib downward, or
 * press back. The nib opens the participants drawer when tapped.
 *
 * Pager sync strategy — avoids the feedback loop:
 *  • Marker tap  → [activeIndex] changes → [LaunchedEffect] scrolls pager
 *                  → we set markerTriggered = true so the settle callback
 *                    knows NOT to call [onPageChanged] for that scroll.
 *  • Pager swipe → settle detected via [snapshotFlow] when isScrollInProgress
 *                  → only calls [onPageChanged] when markerTriggered is false.
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
    participantsOpen: Boolean = false,
    onParticipants: () -> Unit = {},
) {
    if (events.isEmpty()) return

    val loop            = events.size > 1
    val pagerState      = rememberPagerState(
        initialPage = if (loop) loopStartPage(activeIndex.coerceIn(0, events.lastIndex), events.size) else 0,
        pageCount   = { if (events.size > 1) LoopPages else 1 }
    )
    // Real event index behind the (virtual) current page.
    val realPage        = pagerState.currentPage % events.size
    val scope           = rememberCoroutineScope()
    // True while a marker-tap-initiated scroll is in flight.
    // Prevents the settle observer from echoing the change back to the ViewModel.
    val markerTriggered = remember { mutableStateOf(false) }

    // Always-fresh values for the long-lived collectors below.
    val latestActiveIndex by rememberUpdatedState(activeIndex.coerceIn(0, events.lastIndex))
    val latestEventCount  by rememberUpdatedState(events.size)

    // ── Marker tap → scroll pager (shortest way round the loop) ──────────────
    LaunchedEffect(activeIndex) {
        val n      = events.size
        val target = activeIndex.coerceIn(0, n - 1)
        // Never fight a swipe/fling that is already moving the pager —
        // that tug-of-war is what left it stuck between two pages.
        if (pagerState.isScrollInProgress) return@LaunchedEffect
        val current = pagerState.currentPage
        if (current % n != target) {
            var delta = ((target - current % n) % n + n) % n
            if (delta > n / 2) delta -= n
            markerTriggered.value = true
            try {
                pagerState.animateScrollToPage(
                    page          = current + delta,
                    animationSpec = tween(durationMillis = 350)
                )
            } finally {
                // finally → also reset if the user grabs the pager mid-animation
                markerTriggered.value = false
            }
        }
    }

    // ── Pager swipe → update ViewModel ────────────────────────────────────────
    // Report only when the pager is FULLY settled (not scrolling AND offset ≈ 0),
    // and only if it differs from what the ViewModel already has — no echo loop.
    LaunchedEffect(pagerState) {
        snapshotFlow {
            if (!pagerState.isScrollInProgress && abs(pagerState.currentPageOffsetFraction) < 0.005f)
                pagerState.currentPage % latestEventCount
            else -1
        }.collect { settled ->
            if (settled >= 0 && !markerTriggered.value && settled != latestActiveIndex) {
                onPageChanged(settled)
            }
        }
    }

    // ── Sheet motion (all read inside graphicsLayer → no per-frame recomposition) ──
    val dismissDragPx = with(LocalDensity.current) { DismissDrag.toPx() }
    var containerHeightPx by remember { mutableStateOf(0) }
    val enterFraction = remember { Animatable(1f) }   // 1 = fully below screen, 0 = resting
    val dragOffsetPx  = remember { Animatable(0f) }   // finger drag / dismiss slide
    var isDismissing  by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { enterFraction.animateTo(0f, tween(300)) }

    val dismiss: () -> Unit = {
        if (!isDismissing) {
            isDismissing = true
            scope.launch {
                dragOffsetPx.animateTo(containerHeightPx.toFloat(), tween(220))
                onClose()
            }
        }
    }

    // Back closes the sheet — unless the participants drawer is open
    // (EventScreen's own BackHandler closes the drawer first).
    BackHandler(enabled = !participantsOpen && !isDismissing) { dismiss() }

    val dragState = rememberDraggableState { delta ->
        if (!isDismissing) {
            scope.launch {
                dragOffsetPx.snapTo((dragOffsetPx.value + delta).coerceAtLeast(0f))
            }
        }
    }
    val onDragStopped: suspend CoroutineScope.(Float) -> Unit = { velocity ->
        if (dragOffsetPx.value > dismissDragPx || velocity > 1800f) dismiss()
        else dragOffsetPx.animateTo(0f, tween(200))
    }

    // 0 = sheet fully shown, 1 = fully off-screen (drives scrim + nib fade).
    val hiddenFraction: () -> Float = {
        val h = containerHeightPx
        // The nib is screen-anchored and must remain visible even before the
        // sheet reports its first measured height.
        if (h == 0) 0f
        else ((enterFraction.value * h + dragOffsetPx.value) / h).coerceIn(0f, 1f)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Scrim: tap the background to close ────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 1f - hiddenFraction() }
                .background(Color.Black.copy(alpha = 0.32f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = dismiss
                )
        )

        // ── Nib + sheet move as one unit ──────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onSizeChanged { containerHeightPx = it.height }
                .graphicsLayer {
                    alpha = if (containerHeightPx == 0) 0f else 1f   // hide until measured
                    translationY = enterFraction.value * containerHeightPx + dragOffsetPx.value
                }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .draggable(
                        state         = dragState,
                        orientation   = Orientation.Vertical,
                        onDragStopped = onDragStopped
                    ),
                shape           = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color           = Color.White,
                tonalElevation  = 0.dp,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    // ── Drag handle ───────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 8.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .background(Color(0xFFE0E0E0), CircleShape)
                        )
                    }

                    // ── Pager ─────────────────────────────────────────────────
                    HorizontalPager(
                        state    = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                        // One page per swipe — with a looping pager a hard fling must not skip around.
                        flingBehavior = PagerDefaults.flingBehavior(
                            state             = pagerState,
                            pagerSnapDistance = PagerSnapDistance.atMost(1)
                        )
                    ) { page ->
                        EventCard(event = events[page % events.size])
                    }

                    // ── Action buttons ────────────────────────────────────────
                    val currentEvent = events[realPage]

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

                    // ── Navigation row ────────────────────────────────────────
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                val target = pagerState.currentPage - 1
                                if (loop) scope.launch {
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
                                text       = "${realPage + 1} / ${events.size}",
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
                                val target = pagerState.currentPage + 1
                                if (loop) scope.launch {
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

                    // ── Swipe hint ────────────────────────────────────────────
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
                    Spacer(Modifier.navigationBarsPadding())
                }
            }
        }

        // ── Participants nib: right edge, centre of the screen ────────────────
        // Tap → open drawer. Drag down → closes the preview (same drag as the sheet).
        val nibAlpha by animateFloatAsState(
            targetValue   = if (participantsOpen) 0f else 1f,
            animationSpec = tween(180),
            label         = "participants_nib_alpha"
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = NibWidth, height = NibHeight)
                .graphicsLayer { alpha = nibAlpha * (1f - hiddenFraction()) }
                .clip(RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
                .background(OrangePrimary)
                .draggable(
                    state         = dragState,
                    orientation   = Orientation.Vertical,
                    onDragStopped = onDragStopped
                )
                .clickable(enabled = !participantsOpen && !isDismissing, onClick = onParticipants),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Group,
                contentDescription = "Open participants",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
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
                AsyncImage(
                    model = event.posterUrl,
                    contentDescription = "${event.title} poster",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val posterYear = event.date.takeLast(4).ifBlank { "2025" }
                    val normalizedTitle = remember(event.title) {
                        event.title.trim().replace(Regex("\\s+"), " ").ifBlank { "EVENT" }
                    }
                    val withoutDuplicateYear = normalizedTitle
                        .replace(Regex("\\s+$posterYear$", RegexOption.IGNORE_CASE), "")
                        .ifBlank { normalizedTitle }
                    val posterTitle = remember(withoutDuplicateYear) { withoutDuplicateYear }
                    val titleFontSize = remember(posterTitle) { mutableStateOf(16.sp) }
                    Text(
                        text       = posterTitle,
                        fontSize   = titleFontSize.value,
                        lineHeight = titleFontSize.value * 1.15f,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White,
                        textAlign  = TextAlign.Center,
                        maxLines   = 1,
                        softWrap   = false,
                        overflow   = TextOverflow.Clip,
                        onTextLayout = { layout ->
                            if (layout.hasVisualOverflow && titleFontSize.value > 6.sp) {
                                titleFontSize.value = (titleFontSize.value.value - 1f).coerceAtLeast(6f).sp
                            }
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = posterYear,
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
