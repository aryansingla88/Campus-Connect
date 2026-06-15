package com.example.campusconnect.feature.events.ui.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.events.data.FakeParticipantsService
import com.example.campusconnect.feature.events.data.ParticipantTeam
import com.example.campusconnect.feature.events.data.SoloParticipant
import com.example.campusconnect.feature.events.data.TeamMember
import com.example.campusconnect.model.Event

private val OrangePrimary = Color(0xFFFF6F00)
private val OrangeLight   = Color(0xFFFFF3E0)
private val CardBg        = Color(0xFFF9F9F9)
private val TextPrimary   = Color(0xFF1A1A1A)
private val TextMuted     = Color(0xFF9E9E9E)
private val DividerColor  = Color(0xFFF0F0F0)

@Composable
fun EventParticipantsDrawer(
    event    : Event,
    isOpen   : Boolean,
    onToggle : () -> Unit
) {
    val service = remember { FakeParticipantsService() }
    val teams   = remember(event.id) { service.getTeams(event.id) }
    val solo    = remember(event.id) { service.getSoloParticipants(event.id) }
    val total   = remember(event.id) { service.getTotalCount(event.id) }


    val expanded = remember(event.id) {
        mutableStateMapOf<Int, Boolean>().apply { teams.forEach { put(it.id, false) } }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Dim overlay when open
        if (isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x55000000))
                    .clickable { onToggle() }
            )
        }

        // ── Nib + Drawer as one unit anchored to right edge ───────────────────
        // They live in a Row so the nib always sits left of the drawer panel.
        // When the drawer is closed, only the nib is visible (drawer is gone).
        // When open, both slide in together — nib stays attached to drawer edge.
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
        ) {
            Row(
                modifier          = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nib — always rendered, sticks to left side of drawer
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 52.dp)
                        .clip(RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
                        .background(OrangePrimary)
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = if (isOpen) Icons.Default.ChevronRight
                        else        Icons.Default.Group,
                        contentDescription = if (isOpen) "Close participants"
                        else        "Open participants",
                        tint               = Color.White,
                        modifier           = Modifier.size(18.dp)
                    )
                }

                // Drawer panel — slides in/out, nib moves with it
                AnimatedVisibility(
                    visible = isOpen,
                    enter   = slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec  = tween(280)
                    ),
                    exit    = slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(240)
                    )
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(280.dp)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { _, dragAmount ->
                                    if (dragAmount > 10f) onToggle()
                                }
                            },
                        shape           = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                        color           = Color.White,
                        tonalElevation  = 0.dp,
                        shadowElevation = 8.dp
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 12.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Participants",
                                        fontSize   = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = TextPrimary
                                    )
                                    Text(
                                        "${event.title}  ·  $total registered",
                                        fontSize = 11.sp,
                                        color    = TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = onToggle) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint               = TextMuted
                                    )
                                }
                            }

                            HorizontalDivider(color = DividerColor)

                            LazyColumn(
                                modifier            = Modifier.fillMaxSize(),
                                contentPadding      = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (teams.isNotEmpty()) {
                                    item(key = "section_teams") { SectionLabel("Teams") }
                                    items(teams, key = { "team_${it.id}" }) { team ->
                                        TeamCard(
                                            team       = team,
                                            isExpanded = expanded[team.id] ?: false,
                                            onToggle   = {
                                                expanded[team.id] = !(expanded[team.id] ?: false)
                                            }
                                        )
                                    }
                                }
                                if (solo.isNotEmpty()) {
                                    item(key = "section_solo") {
                                        SectionLabel(
                                            label    = "Individual participants",
                                            modifier = Modifier.padding(
                                                top = if (teams.isNotEmpty()) 6.dp else 0.dp
                                            )
                                        )
                                    }
                                    items(solo, key = { "solo_${it.id}" }) { participant ->
                                        SoloCard(participant)
                                    }
                                }
                                item(key = "bottom_spacer") { Spacer(Modifier.height(16.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── TeamCard — header shows name + member count only, no type ───────────────

@Composable
private fun TeamCard(
    team       : ParticipantTeam,
    isExpanded : Boolean,
    onToggle   : () -> Unit
) {
    val chevronAngle by animateFloatAsState(
        targetValue   = if (isExpanded) 180f else 0f,
        animationSpec = tween(200),
        label         = "chevron_${team.id}"
    )

    Card(
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        // Collapsed header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InitialsAvatar(
                initials = team.name
                    .split(" ").take(2)
                    .joinToString("") { it.take(1).uppercase() }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = team.name,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                // Leader name as subtitle
                val leader = team.members.firstOrNull { it.isLeader }
                if (leader != null) {
                    Text(
                        text     = leader.name,
                        fontSize = 11.sp,
                        color    = TextMuted
                    )
                }
            }

            // Member count badge
            Box(
                modifier = Modifier
                    .background(OrangeLight, RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    "${team.members.size} members",
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color(0xFFBF360C)
                )
            }

            Icon(
                imageVector        = Icons.Default.ExpandMore,
                contentDescription = null,
                tint               = TextMuted,
                modifier           = Modifier.size(18.dp).rotate(chevronAngle)
            )
        }

        // Expanded member list — same style as SoloCard
        AnimatedVisibility(
            visible = isExpanded,
            enter   = expandVertically(animationSpec = tween(200)),
            exit    = shrinkVertically(animationSpec = tween(200))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                HorizontalDivider(color = DividerColor)
                team.members.forEachIndexed { index, member ->
                    MemberRow(member)
                    if (index < team.members.lastIndex) {
                        HorizontalDivider(
                            color    = DividerColor,
                            modifier = Modifier.padding(start = 52.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── MemberRow — same layout as SoloCard, leader gets a small badge ──────────

@Composable
private fun MemberRow(member: TeamMember) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(OrangeLight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = member.initials,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = OrangePrimary
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = member.name,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Text(
                text     = member.subtitle,   // "MCA 2nd Year" etc — no role
                fontSize = 11.sp,
                color    = TextMuted
            )
        }

        // Leader badge — only shown for team leader
        if (member.isLeader) {
            Box(
                modifier = Modifier
                    .background(OrangeLight, RoundedCornerShape(10.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    "Lead",
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color      = OrangePrimary
                )
            }
        }
    }
}

// ─── SoloCard ─────────────────────────────────────────────────────────────────

@Composable
private fun SoloCard(participant: SoloParticipant) {
    Card(
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InitialsAvatar(initials = participant.initials)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = participant.name,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    text     = participant.subtitle,
                    fontSize = 11.sp,
                    color    = TextMuted
                )
            }

            Box(
                modifier = Modifier
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("Solo", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = TextMuted)
            }
        }
    }
}

// ─── Shared helpers ───────────────────────────────────────────────────────────

@Composable
private fun InitialsAvatar(initials: String) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(OrangeLight),
        contentAlignment = Alignment.Center
    ) {
        Text(text = initials, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = OrangePrimary)
    }
}

@Composable
private fun SectionLabel(label: String, modifier: Modifier = Modifier) {
    Text(
        text          = label,
        fontSize      = 10.sp,
        fontWeight    = FontWeight.Medium,
        color         = TextMuted,
        letterSpacing = 0.04.sp,
        modifier      = modifier.padding(start = 2.dp, bottom = 2.dp)
    )
}