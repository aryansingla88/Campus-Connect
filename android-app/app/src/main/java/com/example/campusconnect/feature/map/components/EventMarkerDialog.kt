package com.example.campusconnect.feature.map.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.campusconnect.R
import com.example.campusconnect.feature.map.model.MapEventInfo
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// Darker, more saturated orange to match the reference poster/UI screenshot.
private val OrangePrimary = Color(0xFFE65100)
private val OrangeAccent = Color(0xFFFF6D00)
private val TextDark = Color(0xFF1C1C1E)
private val TextMuted = Color(0xFF8A8F98)
private val BorderOrange = Color(0xFFFFB074)
private val NearbyChipBg = Color(0xFFFFF1E4)

/**
 * Single bottom-sheet "quick preview" dialog for a map event.
 *
 * - Dismiss is via the drag handle / swipe-down only (no separate close (X)
 *   button).
 * - Date/time are reformatted from whatever the backend sends (plain
 *   "HH:mm-HH:mm" ranges, single ISO-8601 timestamps, or two ISO-8601
 *   timestamps) into a clean "10:00 AM – 04:00 PM" style range.
 * - Below Register Now sits a swipeable card carousel of nearby events
 *   (HorizontalPager) with prev/next nav buttons + a swipe hint, instead of
 *   a static row.
 * - Notify Me / Register Now are tappable toggles: Notify Me flips to a
 *   filled "Notified" state with the bell icon giving a little ring
 *   animation; Register Now flips to an outlined "Registered" state with
 *   its icon swapping to a checkmark.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventMarkerDialog(
    event: MapEventInfo,
    onDismiss: () -> Unit,
    onNavigateClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onNotifyClick: () -> Unit = {},
    // Optional explicit end time (ISO-8601 or "hh:mm a" / "HH:mm"). If your
    // data model exposes a separate end-time field, pass it here to get a
    // true start–end range; otherwise the single `event.time` value is
    // shown on its own.
    endTime: String? = null,
    nearbyEvents: List<MapEventInfo> = emptyList(),
    onNearbyEventClick: (MapEventInfo) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { SheetDragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        EventQuickPreviewContent(
            event = event,
            endTime = endTime,
            nearbyEvents = nearbyEvents,
            onNavigateClick = onNavigateClick,
            onRegisterClick = onRegisterClick,
            onNotifyClick = onNotifyClick,
            onNearbyEventClick = onNearbyEventClick
        )
    }
}

@Composable
private fun SheetDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFD9D9D9))
        )
    }
}

@Composable
private fun EventQuickPreviewContent(
    event: MapEventInfo,
    endTime: String?,
    nearbyEvents: List<MapEventInfo>,
    onNavigateClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onNotifyClick: () -> Unit,
    onNearbyEventClick: (MapEventInfo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            EventPoster(
                event = event,
                modifier = Modifier
                    .width(118.dp)
                    .height(190.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                QuickPreviewTag()

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = event.title,
                    color = TextDark,
                    fontSize = 21.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "By ${event.hosts.firstOrNull()?.name ?: "Campus Club"}",
                    color = TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                MetaRow(icon = Icons.Default.CalendarToday, text = formatEventDate(event.date))
                MetaRow(icon = Icons.Default.AccessTime, text = formatEventTimeRange(event.time, endTime))
                MetaRow(
                    icon = Icons.Default.LocationOn,
                    text = event.venue?.ifBlank { "Venue TBD" } ?: "Venue TBD"
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            NotifyMeButton(onClick = onNotifyClick, modifier = Modifier.weight(1f))
            NavigatePillButton(onClick = onNavigateClick, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        RegisterNowButton(isJoined = event.isJoined, onClick = onRegisterClick)

        if (nearbyEvents.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            NearbyEventsPager(
                events = nearbyEvents,
                onEventClick = onNearbyEventClick
            )
        }
    }
}

@Composable
private fun EventPoster(
    event: MapEventInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                event.posterResId != null -> {
                    Image(
                        painter = painterResource(id = event.posterResId),
                        contentDescription = event.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                !event.posterUrl.isNullOrBlank() -> {
                    AsyncImage(
                        model = event.posterUrl,
                        contentDescription = event.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    // No poster supplied for this event: fall back to a
                    // generic static background image with the title
                    // overlaid.
                    FallbackPoster(title = event.title)
                }
            }
        }
    }
}

@Composable
private fun FallbackPoster(title: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        // TODO: swap R.drawable.default_event_poster_bg for whichever
        // static asset you want to use as the generic poster background.
        Image(
            painter = painterResource(id = R.drawable.default_event_poster_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.55f)
                        )
                    )
                )
        )

        // Bolder, more "poster-like" display type for the title overlay.
        // Swap FontFamily.SansSerif for a bundled custom font (via
        // FontFamily(Font(R.font.your_display_font))) if you have one.
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.6.sp,
            textAlign = TextAlign.Center,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 10.dp)
        )
    }
}

@Composable
private fun QuickPreviewTag() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(OrangePrimary)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "QUICK PREVIEW",
            color = OrangePrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Flat meta row: the icon is tinted orange and sits directly on the
 * sheet's white background — no circular badge behind it.
 */
@Composable
private fun MetaRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OrangePrimary,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(9.dp))

        Text(
            text = text,
            color = TextDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Tapping flips the button's own state: filled <-> outlined, with the
 * label swapping from "Notify Me" to "Notified" — and the bell gives a
 * quick ring (rotation wiggle) the moment it becomes active.
 */
@Composable
private fun NotifyMeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isNotified by remember { mutableStateOf(false) }
    val bellRotation = remember { Animatable(0f) }

    LaunchedEffect(isNotified) {
        if (isNotified) {
            bellRotation.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 450
                    0f at 0
                    -25f at 90
                    20f at 180
                    -15f at 270
                    8f at 360
                    0f at 450
                }
            )
        }
    }

    val backgroundColor = if (isNotified) OrangePrimary else Color.White
    val contentColor = if (isNotified) Color.White else OrangePrimary

    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .then(
                if (!isNotified) {
                    Modifier.border(width = 1.dp, color = BorderOrange, shape = RoundedCornerShape(14.dp))
                } else {
                    Modifier
                }
            )
            .clickable {
                isNotified = !isNotified
                onClick()
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = if (isNotified) "Notified" else "Notify me",
            tint = contentColor,
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer { rotationZ = bellRotation.value }
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isNotified) "Notified" else "Notify Me",
            color = contentColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NavigatePillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(OrangeAccent)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Navigate",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.NearMe,
            contentDescription = "Navigate",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Tapping flips the button's own state too: filled orange "Register Now"
 * <-> outlined "Registered", with the icon swapping from the person-add
 * glyph to a checkmark once registered. Starts from [isJoined] so it
 * reflects real registration state, but resets its local toggle whenever
 * [isJoined] changes upstream (e.g. after a real registration call
 * completes).
 */
@Composable
private fun RegisterNowButton(
    isJoined: Boolean,
    onClick: () -> Unit
) {
    var registered by remember(isJoined) { mutableStateOf(isJoined) }

    val backgroundColor = if (registered) Color.White else OrangePrimary
    val contentColor = if (registered) OrangePrimary else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .then(
                if (registered) {
                    Modifier.border(width = 1.dp, color = OrangePrimary, shape = RoundedCornerShape(16.dp))
                } else {
                    Modifier
                }
            )
            .clickable {
                registered = !registered
                onClick()
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (registered) Icons.Default.Check else Icons.Default.PersonAdd,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (registered) "Registered" else "Register Now",
            color = contentColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/**
 * Swipeable card carousel of nearby events, sitting below Register Now.
 * Each page is one full-width event card; prev/next buttons and a swipe
 * hint sit underneath, mirroring the reference navigation-row pattern.
 * The prev/next buttons are filled in the same orange as Register Now.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun NearbyEventsPager(
    events: List<MapEventInfo>,
    onEventClick: (MapEventInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { events.size })
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            pageSpacing = 12.dp
        ) { page ->
            val nearbyEvent = events[page]
            NearbyEventCard(
                event = nearbyEvent,
                onClick = { onEventClick(nearbyEvent) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Navigation row ────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilledTonalIconButton(
                onClick = {
                    val target = (pagerState.currentPage - 1 + events.size) % events.size
                    scope.launch {
                        pagerState.animateScrollToPage(target, animationSpec = tween(300))
                    }
                },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = OrangePrimary
                )
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous event", tint = Color.White)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${events.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark
                )
                Text(
                    text = "${events.size} events nearby",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            FilledTonalIconButton(
                onClick = {
                    val target = (pagerState.currentPage + 1) % events.size
                    scope.launch {
                        pagerState.animateScrollToPage(target, animationSpec = tween(300))
                    }
                },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = OrangePrimary
                )
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next event", tint = Color.White)
            }
        }

        // ── Swipe hint ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.SwapHoriz,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Swipe to browse all events", fontSize = 12.sp, color = TextMuted)
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Default.SwapHoriz,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun NearbyEventCard(
    event: MapEventInfo,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NearbyChipBg)
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            when {
                event.posterResId != null -> Image(
                    painter = painterResource(id = event.posterResId),
                    contentDescription = event.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                !event.posterUrl.isNullOrBlank() -> AsyncImage(
                    model = event.posterUrl,
                    contentDescription = event.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                else -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OrangePrimary)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                color = TextDark,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = formatEventDate(event.date),
                color = TextMuted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = event.venue?.ifBlank { "Venue TBD" } ?: "Venue TBD",
                color = TextMuted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Date / time formatting helpers
// ---------------------------------------------------------------------------

private val isoDateTimeRegex =
    Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?(?:Z|[+-]\d{2}:?\d{2})?""")
private val isoTimeOfDayRegex = Regex("""T(\d{2}):(\d{2})""")

/**
 * Reformats a date string into "20 May 2025" style. Tries a handful of
 * common incoming formats and falls back to the raw string (or "Date TBD")
 * if none match.
 */
private fun formatEventDate(raw: String): String {
    if (raw.isBlank()) return "Date TBD"

    val inputPatterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd",
        "yyyy/MM/dd",
        "dd-MM-yyyy",
        "dd/MM/yyyy",
        "MM-dd-yyyy",
        "MM/dd/yyyy"
    )

    for (pattern in inputPatterns) {
        try {
            val parser = SimpleDateFormat(pattern, Locale.getDefault())
            parser.isLenient = false
            val parsed = parser.parse(raw.trim()) ?: continue
            val outFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return outFormat.format(parsed)
        } catch (e: Exception) {
            // Try the next pattern.
        }
    }

    // Already human-readable (or unrecognized) — show as-is.
    return raw
}

/** Converts a 24-hour hour/minute pair into "hh:mm AM/PM" text. */
private fun to12Hour(hour24: Int, minute: Int): String {
    val period = if (hour24 < 12) "AM" else "PM"
    val hour12Raw = hour24 % 12
    val hour12 = if (hour12Raw == 0) 12 else hour12Raw
    return String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, period)
}

/** Pulls just the time-of-day out of a full ISO-8601 timestamp, if present. */
private fun extractIsoTime(candidate: String): String? {
    val match = isoTimeOfDayRegex.find(candidate) ?: return null
    val hour = match.groupValues[1].toIntOrNull() ?: return null
    val minute = match.groupValues[2].toIntOrNull() ?: return null
    return to12Hour(hour, minute)
}

/**
 * Formats a single time value (ISO-8601 timestamp, "HH:mm", or "hh:mm a")
 * into "hh:mm AM/PM" text, or null if it can't be parsed.
 */
private fun formatSingleTime(raw: String): String? {
    extractIsoTime(raw)?.let { return it }

    val inputPatterns = listOf("HH:mm", "H:mm", "hh:mm a", "h:mm a")
    for (pattern in inputPatterns) {
        try {
            val parser = SimpleDateFormat(pattern, Locale.getDefault())
            parser.isLenient = false
            val parsed = parser.parse(raw.trim()) ?: continue
            val outFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return outFormat.format(parsed)
        } catch (e: Exception) {
            // Try the next pattern.
        }
    }
    return null
}

/**
 * Builds a "10:00 AM – 04:00 PM" style range from whatever shape the raw
 * data comes in:
 *  - an explicit [endTimeRaw] paired with [raw] as the start
 *  - two ISO-8601 timestamps embedded in [raw] itself
 *  - a plain "HH:mm-HH:mm" / "HH:mm to HH:mm" range
 *  - otherwise, just the single formatted time (no end available)
 */
private fun formatEventTimeRange(raw: String, endTimeRaw: String? = null): String {
    if (raw.isBlank()) return "Time TBD"

    if (!endTimeRaw.isNullOrBlank()) {
        val start = formatSingleTime(raw)
        val end = formatSingleTime(endTimeRaw)
        if (start != null && end != null) return "$start – $end"
    }

    val isoMatches = isoDateTimeRegex.findAll(raw).map { it.value }.toList()

    if (isoMatches.size >= 2) {
        val start = extractIsoTime(isoMatches[0])
        val end = extractIsoTime(isoMatches[1])
        if (start != null && end != null) return "$start – $end"
    }

    // Only safe to split on "-" when the string ISN'T an ISO datetime
    // (ISO dates already contain hyphens in the date portion).
    if (isoMatches.isEmpty()) {
        val separators = listOf("–", " to ", "-")
        for (sep in separators) {
            val split = raw.split(sep).map { it.trim() }
            if (split.size == 2 && split.all { it.isNotBlank() }) {
                val start = formatSingleTime(split[0])
                val end = formatSingleTime(split[1])
                if (start != null && end != null) return "$start – $end"
            }
        }
    }

    if (isoMatches.size == 1) {
        extractIsoTime(isoMatches[0])?.let { return it }
    }

    formatSingleTime(raw)?.let { return it }

    return raw
}