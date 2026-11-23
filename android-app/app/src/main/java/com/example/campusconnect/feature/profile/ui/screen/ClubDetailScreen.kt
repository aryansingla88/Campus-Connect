package com.example.clubs.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.campusconnect.R
import com.example.campusconnect.core.components.AppAvatar
import com.example.campusconnect.core.components.AvatarShape
import com.example.campusconnect.feature.profile.model.Club as ProfileClub
import com.example.campusconnect.feature.profile.model.ClubDetails
import com.example.campusconnect.feature.profile.model.ClubMember as ProfileClubMember
import com.example.campusconnect.feature.profile.model.ClubMemberFilter
import com.example.campusconnect.feature.profile.model.ClubMemberRole
import com.example.campusconnect.feature.profile.model.ClubLoadState
import com.example.campusconnect.feature.profile.model.clubLoadState
import com.example.campusconnect.feature.profile.model.ClubStatus
import com.example.campusconnect.feature.profile.model.filterClubMembers
import com.example.campusconnect.feature.profile.viewmodel.BaseProfileViewModel

// ---------------------------------------------------------------------------
// Theme colors
// ---------------------------------------------------------------------------
private val Orange = Color(0xFFE8590C)
private val OrangeSoft = Color(0xFFFDE8D8)
private val OrangeChipBg = Color(0xFFFFEBDD)
private val TextPrimary = Color(0xFF1B1B1F)
private val TextSecondary = Color(0xFF5F5F66)
private val GreenText = Color(0xFF2E7D32)
private val GreenBg = Color(0xFFDFF3E0)
private val CardBg = Color(0xFFFFFCFA)
private val ScreenBgTop = Color(0xFFFFF1E8)
private val ScreenBgBottom = Color(0xFFFFFAF6)

// ---------------------------------------------------------------------------
// Models
// ---------------------------------------------------------------------------
data class Club(
    val clubId: Int,
    val name: String,
    val clubType: String,
    val memberCount: Int,
    val isActive: Boolean = true,
    val description: String,
    val logoUrl: String? = null,
)

private val ClubMemberRole.label: String
    get() = when (this) {
        ClubMemberRole.PRESIDENT -> "President"
        ClubMemberRole.VICE_PRESIDENT -> "Vice President"
        ClubMemberRole.SECRETARY -> "Secretary"
        ClubMemberRole.JOINT_SECRETARY -> "Joint Secretary"
        ClubMemberRole.ADDITIONAL_SECRETARY -> "Additional Secretary"
        ClubMemberRole.COMMITTEE_HEAD -> "Committee Head"
        ClubMemberRole.MEMBER -> "Member"
    }

/** Badge shown on the right of a member row; matches the Core / Coordinators filter groups. */
private val ClubMemberRole.badge: String?
    get() = when (this) {
        ClubMemberRole.PRESIDENT,
        ClubMemberRole.VICE_PRESIDENT -> "Core"
        ClubMemberRole.SECRETARY,
        ClubMemberRole.JOINT_SECRETARY,
        ClubMemberRole.ADDITIONAL_SECRETARY,
        ClubMemberRole.COMMITTEE_HEAD -> "Coordinator"
        ClubMemberRole.MEMBER -> null
    }

private val ClubMemberFilter.label: String
    get() = when (this) {
        ClubMemberFilter.ALL -> "All"
        ClubMemberFilter.CORE -> "Core"
        ClubMemberFilter.COORDINATORS -> "Coordinators"
        ClubMemberFilter.MEMBERS -> "Members"
    }

// ---------------------------------------------------------------------------
// Route: wires membership state to the ViewModel (shared by own/other profile)
// ---------------------------------------------------------------------------
@Composable
fun ClubDetailRoute(
    club: ProfileClub,
    vm: BaseProfileViewModel,
    onBack: () -> Unit,
    onMemberClick: (Int) -> Unit = {},
    onManageClub: (ProfileClub) -> Unit = {}
) {
    val context = LocalContext.current
    LaunchedEffect(club.clubId) { vm.loadClubDetails(club.clubId) }

    val detailState = clubLoadState(club.clubId, vm.clubDetails, vm.clubDetailsLoadingIds, vm.clubDetailsErrors)
    val details = (detailState as? ClubLoadState.Loaded)?.value
    if (details == null) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            if (detailState is ClubLoadState.Loading) {
                CircularProgressIndicator(color = Orange)
                Spacer(Modifier.height(12.dp))
                Text("Loading club details…", color = TextSecondary)
            } else {
                Text((detailState as ClubLoadState.Error).message, color = TextSecondary)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onBack) { Text("Back") }
                    Button(onClick = { vm.loadClubDetails(club.clubId, force = true) }) { Text("Retry") }
                }
            }
        }
        return
    }

    ClubDetailScreen(
        club = details,
        clubLogoUrl = club.logoUrl,
        membersState = clubLoadState(club.clubId, vm.clubMembers, vm.clubMembersLoadingIds, vm.clubMembersErrors),
        onBack = onBack,
        membershipStatus = details.status,
        isMembershipLoading = club.clubId in vm.joiningClubIds,
        onManageClick = {
            if (details.status == ClubStatus.LEAD) {
                onManageClub(club.copy(status = ClubStatus.LEAD))
            }
        },
        onMembersSelected = { vm.loadClubMembers(club.clubId) },
        onRetryMembers = { vm.loadClubMembers(club.clubId, force = true) },
        onMemberClick = { onMemberClick(it.userId) },
        onJoinClick = {
            vm.joinClub(club.clubId) { result ->
                result.onFailure {
                    Toast.makeText(context, it.message ?: "Couldn't join club", Toast.LENGTH_SHORT).show()
                }
            }
        },
        onLeaveClick = {
            vm.leaveClub(club.clubId) { result ->
                result.onFailure {
                    Toast.makeText(context, it.message ?: "Couldn't leave club", Toast.LENGTH_SHORT).show()
                }
            }
        },
    )
}

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------
@Composable
fun ClubDetailScreen(
    club: ClubDetails,
    clubLogoUrl: String?,
    membersState: ClubLoadState<List<ProfileClubMember>>,
    onBack: () -> Unit,
    membershipStatus: ClubStatus = ClubStatus.NOT_JOINED,
    isMembershipLoading: Boolean = false,
    onJoinClick: () -> Unit = {},
    onLeaveClick: () -> Unit = {},
    onManageClick: () -> Unit = {},
    onNotifyClick: () -> Unit = {},
    onMembersSelected: () -> Unit = {},
    onRetryMembers: () -> Unit = {},
    onMemberClick: (ProfileClubMember) -> Unit = {},
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var confirmLeave by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ScreenBgTop, ScreenBgBottom)))
    ) {
        // Header (top bar + club info + membership action) over the background image
        Box {
            Image(
                painterResource(R.drawable.club_header_bg), contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.CenterEnd,
                modifier = Modifier.matchParentSize(),
            )
            Column(Modifier.statusBarsPadding().padding(bottom = 16.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = TextPrimary)
                    }
                }
                ClubHeader(
                    Club(
                        clubId = club.clubId,
                        name = club.name,
                        clubType = club.clubType,
                        memberCount = club.memberCount,
                        description = club.description.orEmpty(),
                        logoUrl = clubLogoUrl,
                    ),
                    Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(16.dp))
                MembershipActions(
                    status = membershipStatus,
                    loading = isMembershipLoading,
                    onJoin = onJoinClick,
                    onLeave = { confirmLeave = true },
                    onManage = onManageClick,
                    onNotify = onNotifyClick,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }

        ClubTabs(
            selected = selectedTab,
            onSelect = { selectedTab = it },
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        Spacer(Modifier.height(8.dp))

        when (selectedTab) {
            0 -> NoticesTab()
            else -> {
                LaunchedEffect(club.clubId) { onMembersSelected() }
                MembersTab(
                    state = membersState,
                    onRetry = onRetryMembers,
                    onMemberClick = onMemberClick
                )
            }
        }
    }

    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { Text("Leave ${club.name}?") },
            text = { Text("You'll stop seeing this club's notices. You can request to join again later.") },
            confirmButton = {
                TextButton(onClick = { confirmLeave = false; onLeaveClick() }) {
                    Text("Leave", color = Color(0xFFC62828))
                }
            },
            dismissButton = { TextButton(onClick = { confirmLeave = false }) { Text("Cancel") } },
        )
    }
}

// ---------------------------------------------------------------------------
// Membership action (Join / Request pending / Joined + bell)
// ---------------------------------------------------------------------------
@Composable
private fun MembershipActions(
    status: ClubStatus,
    loading: Boolean,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onManage: () -> Unit,
    onNotify: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    val buttonModifier = Modifier.height(42.dp)
    val outline = BorderStroke(1.dp, Orange.copy(alpha = 0.45f))
    val outlineColors = ButtonDefaults.outlinedButtonColors(
        containerColor = OrangeSoft.copy(alpha = 0.5f),
        contentColor = Orange,
        disabledContentColor = Orange.copy(alpha = 0.7f),
    )

    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        when (status) {
            ClubStatus.NOT_JOINED -> Button(
                onClick = { if (!loading) onJoin() },
                modifier = buttonModifier.weight(1f),
                shape = shape,
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
            ) { ActionLabel(Icons.Outlined.PersonAdd, "Join Club", loading) }

            ClubStatus.PENDING -> OutlinedButton(
                onClick = {}, enabled = false,
                modifier = buttonModifier.weight(1f),
                shape = shape, border = outline, colors = outlineColors,
            ) { ActionLabel(Icons.Outlined.Schedule, "Request Pending", false) }

            ClubStatus.JOINED -> {
                OutlinedButton(
                    onClick = { if (!loading) onLeave() },
                    modifier = buttonModifier.weight(1f),
                    shape = shape, border = outline, colors = outlineColors,
                ) { ActionLabel(Icons.Outlined.PersonRemove, "Leave Club", loading) }
                OutlinedIconButton(
                    onClick = onNotify,
                    modifier = buttonModifier.width(42.dp),
                    shape = shape, border = outline,
                    colors = IconButtonDefaults.outlinedIconButtonColors(containerColor = OrangeSoft.copy(alpha = 0.5f)),
                ) { Icon(Icons.Outlined.NotificationsNone, "Club notifications", tint = Orange) }
            }

            ClubStatus.LEAD -> {
                Button(
                    onClick = onManage,
                    modifier = buttonModifier.weight(1f),
                    shape = shape,
                    colors = ButtonDefaults.buttonColors(containerColor = Orange),
                ) { ActionLabel(Icons.Outlined.Settings, "Manage Club", false) }
                OutlinedIconButton(
                    onClick = onNotify,
                    modifier = buttonModifier.width(42.dp),
                    shape = shape, border = outline,
                    colors = IconButtonDefaults.outlinedIconButtonColors(containerColor = OrangeSoft.copy(alpha = 0.5f)),
                ) { Icon(Icons.Outlined.NotificationsNone, "Club notifications", tint = Orange) }
            }
        }
    }
}

@Composable
private fun ActionLabel(icon: ImageVector, text: String, loading: Boolean) {
    if (loading) {
        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
    } else {
        Icon(icon, null, Modifier.size(18.dp))
    }
    Spacer(Modifier.width(8.dp))
    Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
}

// ---------------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------------
@Composable
private fun ClubHeader(club: Club, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(80.dp).clip(RoundedCornerShape(20.dp)).background(OrangeSoft),
                contentAlignment = Alignment.Center,
            ) {
                AppAvatar(
                    entityId = club.clubId,
                    displayName = club.name,
                    imageUrl = club.logoUrl,
                    size = 56.dp,
                    shape = AvatarShape.ROUNDED
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(club.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    "NIT Kurukshetra · ${club.clubType.toDisplayName()}",
                    fontSize = 14.sp, color = TextPrimary.copy(alpha = 0.8f),
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (club.isActive) {
                        Row(
                            Modifier.clip(RoundedCornerShape(50)).background(GreenBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(GreenText))
                            Spacer(Modifier.width(6.dp))
                            Text("Active", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = GreenText)
                        }
                        Spacer(Modifier.width(12.dp))
                    }
                    Icon(Icons.Outlined.Groups, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("${club.memberCount} members", fontSize = 14.sp, color = TextPrimary.copy(alpha = 0.8f))
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(club.description, fontSize = 15.sp, lineHeight = 22.sp, color = TextPrimary.copy(alpha = 0.85f))
    }
}

// "STUDENT_CLUB" -> "Student Club"
private fun String.toDisplayName() =
    split('_', ' ').filter { it.isNotEmpty() }
        .joinToString(" ") { it.lowercase().replaceFirstChar(Char::uppercase) }

// ---------------------------------------------------------------------------
// Tabs
// ---------------------------------------------------------------------------
@Composable
private fun ClubTabs(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val tabs = listOf("Notices" to Icons.Outlined.Campaign, "Members" to Icons.Outlined.Groups)
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        tabs.forEachIndexed { index, (label, icon) ->
            val isSelected = index == selected
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) OrangeSoft else Color.Transparent)
                    .clickable { onSelect(index) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    Modifier.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(icon, null, tint = if (isSelected) Orange else TextSecondary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        label, fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) Orange else TextSecondary,
                    )
                }
                // underline indicator
                Box(
                    Modifier.fillMaxWidth().height(3.dp)
                        .background(if (isSelected) Orange else Color.Transparent, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

// TODO(Club notices):
// 1. Add a backend notice data model.
// 2. Add a notice retrieval API.
// 3. Define publishing and management permissions.
// 4. Add Android notice DTOs and repository support.
// 5. Integrate notice list and detail screens.
// 6. Support pinned notices and attachments.
@Composable
private fun NoticesTab() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.Campaign, null, tint = Orange, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(10.dp))
            Text("No notices available yet", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}

// ---------------------------------------------------------------------------
// Members tab
// ---------------------------------------------------------------------------
@Composable
private fun MembersTab(
    state: ClubLoadState<List<ProfileClubMember>>,
    onRetry: () -> Unit,
    onMemberClick: (ProfileClubMember) -> Unit,
) {
    val members = (state as? ClubLoadState.Loaded)?.value.orEmpty()
    var filter by rememberSaveable { mutableStateOf(ClubMemberFilter.ALL) }
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

    if (state is ClubLoadState.Loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Orange) }
        return
    }
    if (state is ClubLoadState.Error) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(state.message, color = TextSecondary)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
        return
    }
    if (state is ClubLoadState.Loaded && members.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No approved members yet", color = TextSecondary) }
        return
    }

    val filtered = remember(members, filter, query) { filterClubMembers(members, filter, query) }
    LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Club Members", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                IconButton(onClick = { searchVisible = !searchVisible }) {
                    Icon(Icons.Outlined.Search, "Search members", tint = TextPrimary)
                }
            }
            if (searchVisible) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Search members") },
                    trailingIcon = {
                        if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
                            Icon(Icons.Outlined.Close, "Clear search")
                        }
                    }
                )
            }
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ClubMemberFilter.entries.toList()) { option ->
                    val selected = option == filter
                    Box(
                        Modifier.clip(RoundedCornerShape(50))
                            .background(if (selected) Orange else CardBg)
                            .border(1.dp, if (selected) Orange else OrangeSoft, RoundedCornerShape(50))
                            .clickable { filter = option }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "${option.label} (${filterClubMembers(members, option, "").size})",
                            fontSize = 14.sp,
                            color = if (selected) Color.White else TextPrimary,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
        if (filtered.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 36.dp), contentAlignment = Alignment.Center) {
                    Text("No members match this search", color = TextSecondary)
                }
            }
        } else {
            item {
                Surface(shape = RoundedCornerShape(18.dp), color = CardBg, shadowElevation = 1.dp) {
                    Column {
                        filtered.forEachIndexed { index, member ->
                            MemberRow(member, onClick = { onMemberClick(member) })
                            if (index < filtered.lastIndex) HorizontalDivider(
                                Modifier.padding(start = 80.dp, end = 12.dp),
                                color = Color(0xFFF1E6DE),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberRow(member: ProfileClubMember, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppAvatar(
            entityId = member.userId,
            displayName = member.fullName,
            imageUrl = member.avatarUrl,
            size = 52.dp,
            shape = AvatarShape.CIRCLE,
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                member.fullName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                member.role.label,
                fontSize = 14.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        member.role.badge?.let {
            Spacer(Modifier.width(8.dp))
            RoleBadge(it)
        }
    }
}

@Composable
private fun RoleBadge(text: String) {
    Box(Modifier.clip(RoundedCornerShape(50)).background(OrangeChipBg).padding(horizontal = 14.dp, vertical = 6.dp)) {
        Text(text, fontSize = 13.sp, color = Orange, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

// ---------------------------------------------------------------------------
