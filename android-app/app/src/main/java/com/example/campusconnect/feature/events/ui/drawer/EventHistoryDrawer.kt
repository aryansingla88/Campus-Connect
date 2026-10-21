package com.example.campusconnect.feature.events.ui.drawer

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.campusconnect.feature.events.data.remote.response.MedalCandidateResponse
import com.example.campusconnect.feature.events.model.EventHistoryItem
import com.example.campusconnect.feature.events.model.MedalAward
import com.example.campusconnect.feature.events.model.MedalType
import com.example.campusconnect.feature.events.model.ParticipantTeam
import com.example.campusconnect.feature.events.model.SoloParticipant

// ─── Theme ────────────────────────────────────────────────────────────────────
private val Orange       = Color(0xFFFF6F00)
private val OrangeLight  = Color(0xFFFFF3E0)
private val GreyText     = Color(0xFF9E9E9E)
private val TextPrimary  = Color(0xFF1A1A1A)
private val DividerColor = Color(0xFFF0F0F0)
private val CardBg       = Color(0xFFF9F9F9)
private val GreyMarker   = Color(0xFFBDBDBD)   // past event accent

// Header / empty-state palette (matches the participants drawer)
private val Navy      = Color(0xFF1F2A44)
private val Peach     = Color(0xFFFFE3D1)
private val PeachSoft = Color(0xFFFFF1E6)
private val SubtleText = Color(0xFF6B7280)

// Medal colours
private val GoldColor   = Color(0xFFFFB300)
private val SilverColor = Color(0xFF90A4AE)
private val BronzeColor = Color(0xFFBF8651)

// ─── EventHistoryDrawer ───────────────────────────────────────────────────────

/**
 * Right-side drawer triggered by a ≡ nib in the top-right of EventScreen.
 *
 * Tabs: Live | Upcoming | Past
 * Past events → expandable medal section per event with award / view logic.
 * Close: tap the dimmed background, swipe right, or press the nib/back — no X button.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventHistoryDrawer(
    isOpen: Boolean,
    onToggle: () -> Unit,
    liveEvents: List<EventHistoryItem>,
    upcomingEvents: List<EventHistoryItem>,
    pastEvents: List<EventHistoryItem>,
    medals: List<MedalAward>,
    teams: List<ParticipantTeam>,
    soloParticipants: List<SoloParticipant>,
    medalCandidates: List<MedalCandidateResponse>,
    onLoadMedals: (Int) -> Unit,
    onLoadParticipants: (Int) -> Unit,
    onLoadMedalCandidates: (Int) -> Unit,
    onAwardMedal: (MedalAward) -> Unit,
    onRemoveMedal: (Int, MedalType) -> Unit,
){

    var selectedTab by remember { mutableStateOf(0) }  // 0=Live, 1=Upcoming, 2=Past

    // Which past event is expanded for medal awarding
    var expandedEventId by remember { mutableStateOf<Int?>(null) }

    // Medal award dialog state
    var awardingMedal  by remember { mutableStateOf<Triple<Int, MedalType, List<SearchableRecipient>>?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {

        // Dim overlay
        if (isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x55000000))
                    .clickable { onToggle() }
            )
        }

        // Drawer + Nib anchored top-right
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
        ) {
            // ── Drawer panel ──────────────────────────────────────────────────
            AnimatedVisibility(
                visible = isOpen,
                enter   = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(280)),
                exit    = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(240))
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.82f)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, drag ->
                                if (drag > 12f) onToggle()
                            }
                        },
                    shape           = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                    color           = Color.White,
                    shadowElevation = 10.dp
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {

                        // ── Header + tabs (peach banner, no close button) ─────
                        EventsHeader(
                            selectedTab   = selectedTab,
                            liveCount     = liveEvents.size,
                            upcomingCount = upcomingEvents.size,
                            pastCount     = pastEvents.size,
                            onTabSelected = { selectedTab = it }
                        )

                        // ── List ──────────────────────────────────────────────
                        val isEmptyTab = when (selectedTab) {
                            0    -> liveEvents.isEmpty()
                            1    -> upcomingEvents.isEmpty()
                            else -> pastEvents.isEmpty()
                        }

                        LazyColumn(
                            modifier            = Modifier.weight(1f),
                            contentPadding      = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (selectedTab == 0) {

                                // LIVE
                                if (liveEvents.isEmpty()) {
                                    item(key = "live_empty") {
                                        EmptyState(
                                            title    = "No live events right now",
                                            subtitle = "Events that are happening now will appear here.",
                                            modifier = Modifier.fillParentMaxSize()
                                        )
                                    }
                                } else {
                                    items(liveEvents, key = { "live_${it.id}" }) { event ->
                                        LiveEventCard(event)
                                    }
                                }

                            } else if (selectedTab == 1) {

                                // UPCOMING
                                if (upcomingEvents.isEmpty()) {
                                    item(key = "upcoming_empty") {
                                        EmptyState(
                                            title    = "No upcoming events",
                                            subtitle = "Events you have scheduled will appear here.",
                                            modifier = Modifier.fillParentMaxSize()
                                        )
                                    }
                                } else {
                                    items(upcomingEvents, key = { "upcoming_${it.id}" }) { event ->
                                        UpcomingEventCard(event)
                                    }
                                }

                            } else {

                                // PAST
                                if (pastEvents.isEmpty()) {
                                    item(key = "past_empty") {
                                        EmptyState(
                                            title    = "No past events",
                                            subtitle = "Completed events will appear here.",
                                            modifier = Modifier.fillParentMaxSize()
                                        )
                                    }
                                } else {
                                    items(pastEvents, key = { "past_${it.id}" }) { event ->
                                        val isExpanded = expandedEventId == event.id

                                        LaunchedEffect(event.id) {
                                            onLoadMedals(event.id)
                                        }

                                        val awards = medals.filter {
                                            it.eventId == event.id
                                        }

                                        PastEventCard(
                                            event = event,
                                            awards = awards,
                                            isExpanded = isExpanded,
                                            onToggle = {
                                                if (!isExpanded) {
                                                    onLoadParticipants(event.id)
                                                    onLoadMedalCandidates(event.id)
                                                }

                                                expandedEventId =
                                                    if (isExpanded) null else event.id
                                            },
                                            onAward = { medalType ->

                                                val available = medalCandidates.map { candidate ->
                                                    SearchableRecipient(
                                                        registrationId = candidate.registrationId,
                                                        name = candidate.name,
                                                        subtitle = if (
                                                            candidate.courseId != null &&
                                                            candidate.admissionYear != null
                                                        ) {
                                                            candidate.name // temporary, next step course name resolve karenge
                                                        } else {
                                                            if (candidate.team) "Team" else ""
                                                        },
                                                        isTeam = candidate.team
                                                    )
                                                }

                                                awardingMedal =
                                                    Triple(
                                                        event.id,
                                                        medalType,
                                                        available
                                                    )
                                            },
                                            onRemove = { medalType ->
                                                onRemoveMedal(event.id, medalType)
                                            }
                                        )
                                    }
                                }
                            }
                            // No trailing spacer on an empty tab, so the empty state
                            // fills the viewport exactly and never scrolls.
                            if (!isEmptyTab) {
                                item(key = "bottom") { Spacer(Modifier.height(16.dp)) }
                            }
                        }
                    }
                }
            }

            // ── NIB removed — button lives in EventScreen top bar ─────────────
        }
    }

    // ── Medal award dialog ────────────────────────────────────────────────────
    awardingMedal?.let { (eventId, medalType, recipients) ->
        MedalAwardDialog(
            medalType  = medalType,
            recipients = recipients,
            onConfirm = { recipient ->

                onAwardMedal(
                    MedalAward(
                        eventId = eventId,
                        medalType = medalType,
                        registrationId = recipient.registrationId,
                        recipientName = recipient.name,
                        isTeam = recipient.isTeam
                    )
                )

                awardingMedal = null
            },
            onDismiss = { awardingMedal = null }
        )
    }
}

// ─── Header — soft peach banner with title + tab chips ───────────────────────

@Composable
private fun EventsHeader(
    selectedTab: Int,
    liveCount: Int,
    upcomingCount: Int,
    pastCount: Int,
    onTabSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFFFFEEE0), Color(0xFFFFF9F5))))
            .drawBehind {
                // soft decorative blobs, top-right
                drawCircle(
                    color  = Color(0xFFFFD6BA).copy(alpha = 0.45f),
                    radius = size.width * 0.34f,
                    center = Offset(size.width * 0.98f, size.height * 0.02f)
                )
                drawCircle(
                    color  = Color(0xFFFFE0CB).copy(alpha = 0.55f),
                    radius = size.width * 0.22f,
                    center = Offset(size.width * 1.02f, size.height * 0.55f)
                )
            }
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Peach, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint               = Orange,
                    modifier           = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text       = "Your Events",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = Navy
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabChip(
                label    = "Live · $liveCount",
                selected = selectedTab == 0,
                onClick  = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            TabChip(
                label    = "Upcoming · $upcomingCount",
                selected = selectedTab == 1,
                onClick  = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
            TabChip(
                label    = "Past · $pastCount",
                selected = selectedTab == 2,
                onClick  = { onTabSelected(2) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ─── LiveEventCard ────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun LiveEventCard(event: EventHistoryItem){
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Live dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color = Color(0xFFF12727))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    event.title,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = formatEventDate(event.startTime),
                        fontSize = 11.sp,
                        color = GreyText
                    )

                    Text(
                        text = "•",
                        fontSize = 15.sp,
                        color = GreyText
                    )

                    Text(
                        text = event.venue,
                        fontSize = 11.sp,
                        color = GreyText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Box(
                modifier = Modifier
                    .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("LIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
            }
        }
    }
}

// ─── PastEventCard ────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun PastEventCard(
    event      : EventHistoryItem,
    awards     : List<MedalAward>,
    isExpanded : Boolean,
    onToggle   : () -> Unit,
    onAward    : (MedalType) -> Unit,
    onRemove   : (MedalType) -> Unit
) {
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Grey dot for past
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(GreyMarker)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        event.title,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = formatEventDate(event.startTime),
                            fontSize = 11.sp,
                            color = GreyText
                        )

                        Text(
                            text = "•",
                            fontSize = 15.sp,
                            color = GreyText
                        )

                        Text(
                            text = event.venue,
                            fontSize = 11.sp,
                            color = GreyText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Medal count badge if any awarded
                if (awards.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(OrangeLight, RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "🏅 ${awards.size}",
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color      = Orange
                        )
                    }
                }

                Icon(
                    imageVector        = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint               = GreyText,
                    modifier           = Modifier.size(18.dp)
                )
            }

            // Expanded medal section
            if (isExpanded) {
                HorizontalDivider(color = DividerColor)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Medal Awards",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color      = GreyText
                    )
                    MedalType.entries.forEach { medalType ->
                        val existing = awards.firstOrNull { it.medalType == medalType }
                        MedalRow(
                            medalType = medalType,
                            award     = existing,
                            onAward   = { onAward(medalType) },
                            onRemove  = { onRemove(medalType) }
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun UpcomingEventCard(event: EventHistoryItem) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBg
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // Upcoming dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Orange)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = formatEventDate(event.startTime),
                        fontSize = 11.sp,
                        color = GreyText
                    )

                    Text(
                        text = "•",
                        fontSize = 15.sp,
                        color = GreyText
                    )

                    Text(
                        text = event.venue,
                        fontSize = 11.sp,
                        color = GreyText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .background(
                        OrangeLight,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(
                        horizontal = 8.dp,
                        vertical = 3.dp
                    )
            ) {
                Text(
                    "UPCOMING",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Orange
                )
            }
        }
    }
}

// ─── MedalRow — shows awarded name or "Award" search button ──────────────────

@Composable
private fun MedalRow(
    medalType : MedalType,
    award     : MedalAward?,
    onAward   : () -> Unit,
    onRemove  : () -> Unit
) {
    val (medalColor, medalEmoji) = when (medalType) {
        MedalType.GOLD   -> GoldColor   to "🥇"
        MedalType.SILVER -> SilverColor to "🥈"
        MedalType.BRONZE -> BronzeColor to "🥉"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(10.dp))
            .border(0.5.dp, DividerColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(medalEmoji, fontSize = 20.sp)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                medalType.label,
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color      = medalColor
            )
            if (award != null) {
                Text(
                    award.recipientName,
                    fontSize = 11.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    "Not yet awarded",
                    fontSize = 11.sp,
                    color = GreyText
                )
            }
        }

        if (award != null) {
            // Remove button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFEBEE))
                    .clickable { onRemove() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Remove", fontSize = 10.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Medium)
            }
        } else {
            // Award button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(OrangeLight)
                    .clickable { onAward() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Award", fontSize = 10.sp, color = Orange, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─── MedalAwardDialog — search participants and pick one ──────────────────────

@Composable
private fun MedalAwardDialog(
    medalType  : MedalType,
    recipients : List<SearchableRecipient>,
    onConfirm  : (SearchableRecipient) -> Unit,
    onDismiss  : () -> Unit
) {
    var query    by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<SearchableRecipient?>(null) }

    val filtered = remember(query) {
        if (query.isBlank()) recipients
        else recipients.filter { it.name.contains(query, ignoreCase = true) }
    }

    val (medalColor, medalEmoji) = when (medalType) {
        MedalType.GOLD   -> GoldColor   to "🥇"
        MedalType.SILVER -> SilverColor to "🥈"
        MedalType.BRONZE -> BronzeColor to "🥉"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .background(Color(0x99000000)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier  = Modifier
                    .fillMaxWidth(0.88f)
                    .fillMaxHeight(0.70f),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "$medalEmoji  ${medalType.label}",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color      = medalColor
                            )
                            Text(
                                "Search and select a recipient",
                                fontSize = 12.sp,
                                color    = GreyText
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = GreyText)
                        }
                    }

                    HorizontalDivider(color = DividerColor)

                    // Search bar
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        RecipientSearchBar(value = query, onValueChange = { query = it })
                    }

                    // Results list
                    LazyColumn(
                        modifier            = Modifier.weight(1f),
                        contentPadding      = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (filtered.isEmpty()) {
                            item(key = "empty") {
                                Box(
                                    modifier         = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (query.isBlank()) "All participants already have a medal"
                                        else                 "No participants found",
                                        fontSize = 13.sp,
                                        color    = GreyText
                                    )
                                }
                            }
                        } else {
                            items(
                                filtered,
                                key = { "${it.isTeam}_${it.registrationId}" }
                            ) { recipient ->

                                val isSelected =
                                    selected?.registrationId == recipient.registrationId &&
                                            selected?.isTeam == recipient.isTeam

                                RecipientCard(
                                    recipient = recipient,
                                    isSelected = isSelected,
                                    onClick = {
                                        selected =
                                            if (isSelected) null else recipient
                                    }
                                )
                            }
                        }
                        item(key = "bottom") { Spacer(Modifier.height(8.dp)) }
                    }

                    // Confirm button
                    HorizontalDivider(color = DividerColor)
                    Box(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Button(
                            onClick  = { selected?.let { onConfirm(it) } },
                            enabled  = selected != null,
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor         = Orange,
                                disabledContainerColor = Color(0xFFE0E0E0)
                            )
                        ) {
                            Text(
                                if (selected != null) "Award to ${selected!!.name}" else "Select a recipient",
                                color      = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── RecipientCard ────────────────────────────────────────────────────────────

@Composable
private fun RecipientCard(
    recipient  : SearchableRecipient,
    isSelected : Boolean,
    onClick    : () -> Unit
) {
    Card(
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (isSelected) OrangeLight else CardBg
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) Orange else DividerColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Orange else OrangeLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    recipient.initials,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (isSelected) Color.White else Orange
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recipient.name,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(recipient.subtitle, fontSize = 11.sp, color = GreyText)
            }

            // Team / Solo badge
            Box(
                modifier = Modifier
                    .background(
                        if (recipient.isTeam) OrangeLight else Color(0xFFF0F0F0),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    if (recipient.isTeam) "Team" else "Solo",
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color      = if (recipient.isTeam) Orange else GreyText
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint     = Orange,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ─── RecipientSearchBar ───────────────────────────────────────────────────────

@Composable
private fun RecipientSearchBar(value: String, onValueChange: (String) -> Unit) {
    val focus = remember { FocusRequester() }
    Card(
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { focus.requestFocus() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Search, null, tint = GreyText, modifier = Modifier.size(18.dp))
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value         = value,
                    onValueChange = onValueChange,
                    modifier      = Modifier.fillMaxWidth().focusRequester(focus),
                    singleLine    = true,
                    textStyle     = TextStyle(fontSize = 13.sp, color = TextPrimary),
                    decorationBox = { inner ->
                        if (value.isEmpty()) Text("Search participants…", fontSize = 13.sp, color = GreyText)
                        inner()
                    }
                )
            }
            if (value.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F0F0))
                        .clickable { onValueChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, null, tint = GreyText, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// ─── Tab chip — orange pill when selected, white pill with peach border otherwise ─

@Composable
private fun TabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (selected) Orange else Color.White)
            .then(if (selected) Modifier else Modifier.border(1.dp, Peach, shape))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            fontSize   = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color      = if (selected) Color.White else SubtleText,
            maxLines   = 1,
            softWrap   = false
        )
    }
}

// ─── EmptyState — calendar illustration + title + subtitle ───────────────────

@Composable
private fun EmptyState(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(width = 180.dp, height = 140.dp)) {
            val w = size.width
            val h = size.height

            // soft blob + ground shadow
            drawOval(PeachSoft, topLeft = Offset(0f, h * 0.08f), size = Size(w, h * 0.82f))
            drawOval(Peach.copy(alpha = 0.7f), topLeft = Offset(w * 0.15f, h * 0.88f), size = Size(w * 0.7f, h * 0.06f))

            // calendar: orange frame + header band, pale body
            val left    = w * 0.30f
            val top     = h * 0.38f
            val cw      = w * 0.40f
            val ch      = h * 0.46f
            val border  = w * 0.022f
            val headerH = h * 0.13f
            val light   = Color(0xFFFFC9A8)

            drawRoundRect(Orange, Offset(left, top), Size(cw, ch), CornerRadius(w * 0.05f))
            drawRoundRect(
                Color(0xFFFFF4EC),
                Offset(left + border, top + headerH),
                Size(cw - 2 * border, ch - headerH - border),
                CornerRadius(w * 0.03f)
            )

            // binder rings
            val ringW = w * 0.03f
            listOf(0.22f, 0.70f).forEach { f ->
                drawRoundRect(
                    light,
                    Offset(left + cw * f, top - h * 0.06f),
                    Size(ringW, h * 0.14f),
                    CornerRadius(ringW / 2)
                )
            }

            // date cells
            val sq  = w * 0.042f
            val gap = w * 0.028f
            val gx  = left + cw * 0.18f
            val gy  = top + headerH + ch * 0.17f
            listOf(light, light, Orange, light, Orange).forEachIndexed { i, c ->
                drawRoundRect(
                    c,
                    Offset(gx + (i % 3) * (sq + gap), gy + (i / 3) * (sq + gap)),
                    Size(sq, sq),
                    CornerRadius(sq * 0.25f)
                )
            }

            // little "pop" rays above the calendar
            val stroke = 3.dp.toPx()
            drawLine(Orange, Offset(w * 0.50f, h * 0.10f), Offset(w * 0.50f, h * 0.19f), stroke, StrokeCap.Round)
            drawLine(Orange, Offset(w * 0.405f, h * 0.14f), Offset(w * 0.44f, h * 0.22f), stroke, StrokeCap.Round)
            drawLine(Orange, Offset(w * 0.595f, h * 0.14f), Offset(w * 0.56f, h * 0.22f), stroke, StrokeCap.Round)
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text       = title,
            fontSize   = 17.sp,
            fontWeight = FontWeight.Bold,
            color      = Navy,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text      = subtitle,
            fontSize  = 13.sp,
            color     = SubtleText,
            textAlign = TextAlign.Center
        )
    }
}

// ─── SearchableRecipient — unified model for teams + solo in award dialog ─────

data class SearchableRecipient(
    val registrationId: Int,
    val name: String,
    val subtitle: String,
    val isTeam: Boolean,
    val initials: String = name
        .split(" ")
        .take(2)
        .joinToString("") {
            it.take(1).uppercase()
        }
)



@RequiresApi(Build.VERSION_CODES.O)
private fun formatEventDate(dateTime: String): String {
    return try {
        java.time.Instant.parse(dateTime)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
            .format(
                java.time.format.DateTimeFormatter.ofPattern(
                    "dd-MMM-yyyy"
                )
            )
    } catch (e: Exception) {
        dateTime.substringBefore("T")
    }
}