package com.example.campusconnect.feature.events.ui.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.events.model.Event
import com.example.campusconnect.feature.events.model.ParticipantTeam
import com.example.campusconnect.feature.events.model.SoloParticipant
import com.example.campusconnect.feature.events.model.TeamMember

private val OrangePrimary = Color(0xFFFF6F00)
private val OrangeLight   = Color(0xFFFFF3E0)
private val CardBg        = Color(0xFFF9F9F9)
private val TextPrimary   = Color(0xFF1A1A1A)
private val TextMuted     = Color(0xFF9E9E9E)
private val DividerColor  = Color(0xFFF0F0F0)
private val Navy          = Color(0xFF1F2A44)
private val Peach          = Color(0xFFFFE3D1)
private val PeachSoft      = Color(0xFFFFF1E6)
private val OrangeDeep     = Color(0xFFFF6B1A)

/**
 * Full-height participants drawer that slides in from the right edge.
 *
 * Meant to be composed ALWAYS (not only while open) inside a `Box(Modifier.fillMaxSize())`
 * in the same composition as the preview sheet — that is what lets the exit animation
 * play. While closed it draws nothing and has no pointer handlers, so every touch falls
 * through to the preview sheet underneath (Edit / Access / Delete keep working).
 *
 * The orange button that OPENS the drawer lives on the preview sheet. Closing is
 * handled by tapping the scrim or pressing Back.
 */
@Composable
fun EventParticipantsDrawer(
    event: Event,
    teams: List<ParticipantTeam>,
    solo: List<SoloParticipant>,
    total: Int,
    isOpen: Boolean,
    onToggle: () -> Unit
) {

    val expanded = remember(event.id) {
        mutableStateMapOf<Int, Boolean>().apply { teams.forEach { put(it.id, false) } }
    }

    // Scrim fades in/out instead of popping, and is removed once fully transparent.
    val scrimAlpha = animateFloatAsState(
        targetValue   = if (isOpen) 1f else 0f,
        animationSpec = tween(if (isOpen) 280 else 240),
        label         = "participants_scrim"
    )
    val scrimVisible by remember { derivedStateOf { scrimAlpha.value > 0.001f } }

    Box(modifier = Modifier.fillMaxSize()) {

        // Dim overlay — only intercepts touches while the drawer is actually open.
        if (scrimVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = scrimAlpha.value }
                    .background(Color(0x55000000))
                    .then(
                        if (isOpen) Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = onToggle
                        ) else Modifier
                    )
            )
        }

        // Panel slides in from the right edge.
        AnimatedVisibility(
            visible  = isOpen,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec  = tween(280)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(240)
            )
        ) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp),
                    shape           = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                    color           = Color.White,
                    tonalElevation  = 0.dp,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {

                        ParticipantsHeader(
                            eventTitle = event.title,
                            total      = total,
                            teamCount  = teams.size,
                            soloCount  = solo.size
                        )

                        if (teams.isEmpty() && solo.isEmpty()) {
                            EmptyParticipants(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                            )
                        } else LazyColumn(
                            modifier            = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding(),
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

// ─── Header — soft peach banner, event name + live counts (no close button) ─

@Composable
private fun ParticipantsHeader(
    eventTitle: String,
    total: Int,
    teamCount: Int,
    soloCount: Int
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
                    center = Offset(size.width * 1.02f, size.height * 0.62f)
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
                    imageVector        = Icons.Default.Group,
                    contentDescription = null,
                    tint               = OrangeDeep,
                    modifier           = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "Participants",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Navy
                )
                Text(
                    text     = eventTitle,
                    fontSize = 13.sp,
                    color    = Color(0xFF6B7280),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatTile(value = total.toString(),     label = "Registered", modifier = Modifier.weight(1f))
            StatTile(value = teamCount.toString(), label = "Teams",      modifier = Modifier.weight(1f))
            StatTile(value = soloCount.toString(), label = "Solo",       modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Peach, RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text       = value,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold,
            color      = OrangeDeep
        )
        Text(
            text     = label,
            fontSize = 11.sp,
            color    = Color(0xFF6B7280)
        )
    }
}

// ─── Empty state ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyParticipants(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.padding(horizontal = 24.dp).padding(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(width = 180.dp, height = 140.dp)) {
            val w = size.width
            val h = size.height

            // soft blob + ground shadow
            drawOval(PeachSoft, topLeft = Offset(0f, h * 0.08f), size = Size(w, h * 0.82f))
            drawOval(Peach.copy(alpha = 0.7f), topLeft = Offset(w * 0.15f, h * 0.88f), size = Size(w * 0.7f, h * 0.06f))

            // side figures (light)
            val light = Color(0xFFFFC9A8)
            drawCircle(light, radius = w * 0.065f, center = Offset(w * 0.27f, h * 0.50f))
            drawArc(light, 180f, 180f, true, Offset(w * 0.16f, h * 0.60f), Size(w * 0.22f, h * 0.30f))
            drawCircle(light, radius = w * 0.065f, center = Offset(w * 0.73f, h * 0.50f))
            drawArc(light, 180f, 180f, true, Offset(w * 0.62f, h * 0.60f), Size(w * 0.22f, h * 0.30f))

            // centre figure (solid)
            drawCircle(OrangeDeep, radius = w * 0.095f, center = Offset(w * 0.5f, h * 0.42f))
            drawArc(OrangeDeep, 180f, 180f, true, Offset(w * 0.34f, h * 0.58f), Size(w * 0.32f, h * 0.44f))

            // little "pop" rays above the head
            val stroke = 3.dp.toPx()
            drawLine(OrangeDeep, Offset(w * 0.50f, h * 0.12f), Offset(w * 0.50f, h * 0.21f), stroke, StrokeCap.Round)
            drawLine(OrangeDeep, Offset(w * 0.405f, h * 0.16f), Offset(w * 0.44f, h * 0.235f), stroke, StrokeCap.Round)
            drawLine(OrangeDeep, Offset(w * 0.595f, h * 0.16f), Offset(w * 0.56f, h * 0.235f), stroke, StrokeCap.Round)
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text       = "No participants yet",
            fontSize   = 17.sp,
            fontWeight = FontWeight.Bold,
            color      = Navy,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text      = "Registered attendees will appear here.",
            fontSize  = 13.sp,
            color     = Color(0xFF6B7280),
            textAlign = TextAlign.Center
        )
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
                    text = team.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val leader = team.members.firstOrNull { it.isLeader }
                if (leader != null) {
                    Text(
                        text = "Led by ${leader.name}",
                        fontSize = 10.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
