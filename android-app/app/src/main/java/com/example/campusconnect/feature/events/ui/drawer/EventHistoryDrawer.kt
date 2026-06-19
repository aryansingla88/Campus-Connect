package com.example.campusconnect.feature.events.ui.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.campusconnect.feature.events.data.fake.FakeMedalService
import com.example.campusconnect.feature.events.data.fake.FakeParticipantsService
import com.example.campusconnect.feature.events.model.MedalAward
import com.example.campusconnect.feature.events.model.MedalType
import com.example.campusconnect.feature.events.model.ParticipantTeam
import com.example.campusconnect.feature.events.model.SoloParticipant
import com.example.campusconnect.feature.events.model.Event
import com.example.campusconnect.feature.events.model.EventStatus

// ─── Theme ────────────────────────────────────────────────────────────────────
private val Orange       = Color(0xFFFF6F00)
private val OrangeLight  = Color(0xFFFFF3E0)
private val GreyText     = Color(0xFF9E9E9E)
private val TextPrimary  = Color(0xFF1A1A1A)
private val DividerColor = Color(0xFFF0F0F0)
private val CardBg       = Color(0xFFF9F9F9)
private val GreyMarker   = Color(0xFFBDBDBD)   // past event accent

// Medal colours
private val GoldColor   = Color(0xFFFFB300)
private val SilverColor = Color(0xFF90A4AE)
private val BronzeColor = Color(0xFFBF8651)

// ─── EventHistoryDrawer ───────────────────────────────────────────────────────

/**
 * Right-side drawer triggered by a ≡ nib in the top-right of EventScreen.
 *
 * Tabs: Live | Past
 * Past events → expandable medal section per event with award / view logic.
 */
@Composable
fun EventHistoryDrawer(
    isOpen   : Boolean,
    onToggle : () -> Unit,
    events   : List<Event> = emptyList()
) {
    val medalService = remember { FakeMedalService() }

    // Force recompose when medals change
    var medalVersion by remember { mutableStateOf(0) }

    // Derived directly from the passed-in list so create/delete updates are instant
    val liveEvents = remember(events) { events.filter { it.status == EventStatus.LIVE } }
    val pastEvents = remember(events) { events.filter { it.status == EventStatus.PAST } }

    var selectedTab by remember { mutableStateOf(0) }  // 0=Live, 1=Past

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
                        .fillMaxWidth(0.78f)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, drag ->
                                if (drag > 12f) onToggle()
                            }
                        },
                    shape           = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                    color           = Color.White,
                    shadowElevation = 10.dp
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {

                        // ── Header ────────────────────────────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Event History",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary
                            )
                            IconButton(onClick = onToggle) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = GreyText)
                            }
                        }

                        // ── Tab row ───────────────────────────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TabChip(
                                label    = "Live  ${liveEvents.size}",
                                selected = selectedTab == 0,
                                color    = Orange,
                                onClick  = { selectedTab = 0 }
                            )
                            TabChip(
                                label    = "Past  ${pastEvents.size}",
                                selected = selectedTab == 1,
                                color    = Orange,
                                onClick  = { selectedTab = 1 }
                            )
                        }

                        HorizontalDivider(color = DividerColor, modifier = Modifier.padding(top = 10.dp))

                        // ── List ──────────────────────────────────────────────
                        LazyColumn(
                            modifier            = Modifier.weight(1f),
                            contentPadding      = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (selectedTab == 0) {
                                // LIVE tab
                                if (liveEvents.isEmpty()) {
                                    item(key = "live_empty") { EmptyState("No live events right now") }
                                } else {
                                    items(liveEvents, key = { "live_${it.id}" }) { event ->
                                        LiveEventCard(event)
                                    }
                                }
                            } else {
                                // PAST tab
                                if (pastEvents.isEmpty()) {
                                    item(key = "past_empty") { EmptyState("No past events") }
                                } else {
                                    items(pastEvents, key = { "past_${it.id}" }) { event ->
                                        val isExpanded = expandedEventId == event.id
                                        // read medalVersion so card recomposes on award
                                        val awards = remember(event.id, medalVersion) {
                                            medalService.getAwardsForEvent(event.id)
                                        }
                                        PastEventCard(
                                            event      = event,
                                            awards     = awards,
                                            isExpanded = isExpanded,
                                            onToggle   = {
                                                expandedEventId =
                                                    if (isExpanded) null else event.id
                                            },
                                            onAward    = { medalType ->
                                                val ps = FakeParticipantsService()
                                                val allRecipients = buildRecipients(
                                                    ps.getTeams(event.id),
                                                    ps.getSoloParticipants(event.id)
                                                )
                                                // Exclude anyone already holding a medal
                                                // in this event (regardless of which medal type)
                                                val alreadyAwardedIds = awards.map {
                                                    Pair(it.recipientId, it.isTeam)
                                                }.toSet()
                                                val available = allRecipients.filter {
                                                    Pair(it.id, it.isTeam) !in alreadyAwardedIds
                                                }
                                                awardingMedal = Triple(event.id, medalType, available)
                                            },
                                            onRemove   = { medalType ->
                                                medalService.removeAward(event.id, medalType)
                                                medalVersion++
                                            }
                                        )
                                    }
                                }
                            }
                            item(key = "bottom") { Spacer(Modifier.height(16.dp)) }
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
            onConfirm  = { recipient ->
                medalService.awardMedal(
                    MedalAward(
                        eventId = eventId,
                        medalType = medalType,
                        recipientId = recipient.id,
                        recipientName = recipient.name,
                        recipientSubtitle = recipient.subtitle,
                        isTeam = recipient.isTeam
                    )
                )
                medalVersion++
                awardingMedal = null
            },
            onDismiss = { awardingMedal = null }
        )
    }
}

// ─── LiveEventCard ────────────────────────────────────────────────────────────

@Composable
private fun LiveEventCard(event: Event) {
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
                    .background(Orange)
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
                Text(
                    "${event.date}  ·  ${event.venue}",
                    fontSize = 11.sp,
                    color    = GreyText
                )
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

@Composable
private fun PastEventCard(
    event      : Event,
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
                    Text(
                        "${event.date}  ·  ${event.venue}",
                        fontSize = 11.sp,
                        color    = GreyText
                    )
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
                    color    = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    award.recipientSubtitle,
                    fontSize = 10.sp,
                    color    = GreyText
                )
            } else {
                Text(
                    "Not yet awarded",
                    fontSize = 11.sp,
                    color    = GreyText
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
                            items(filtered, key = { "${it.isTeam}_${it.id}" }) { recipient ->
                                val isSelected = selected?.id == recipient.id && selected?.isTeam == recipient.isTeam
                                RecipientCard(
                                    recipient  = recipient,
                                    isSelected = isSelected,
                                    onClick    = { selected = if (isSelected) null else recipient }
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

// ─── Tab chip ─────────────────────────────────────────────────────────────────

@Composable
private fun TabChip(label: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) color else Color(0xFFF0F0F0))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            label,
            fontSize   = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (selected) Color.White else GreyText
        )
    }
}

// ─── EmptyState ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(text: String) {
    Box(
        modifier         = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 13.sp, color = GreyText)
    }
}

// ─── SearchableRecipient — unified model for teams + solo in award dialog ─────

data class SearchableRecipient(
    val id       : Int,
    val name     : String,
    val subtitle : String,
    val isTeam   : Boolean,
    val initials : String = name
        .split(" ").take(2).joinToString("") { it.take(1).uppercase() }
)

private fun buildRecipients(
    teams : List<ParticipantTeam>,
    solo  : List<SoloParticipant>
): List<SearchableRecipient> {
    val teamRecipients = teams.map { team ->
        val leader = team.members.firstOrNull { it.isLeader }
        SearchableRecipient(
            id       = team.id,
            name     = team.name,
            subtitle = if (leader != null) "Led by ${leader.name}" else "${team.members.size} members",
            isTeam   = true
        )
    }
    val soloRecipients = solo.map {
        SearchableRecipient(
            id       = it.id,
            name     = it.name,
            subtitle = it.subtitle,
            isTeam   = false
        )
    }
    return teamRecipients + soloRecipients
}