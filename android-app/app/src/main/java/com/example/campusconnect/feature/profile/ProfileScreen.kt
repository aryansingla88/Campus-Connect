package com.example.campusconnect.feature.profile

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

// ── Colors ───────────────────────────────────────────────────────────────────
private val Orange       = Color(0xFFF97316)
private val OrangeLight  = Color(0xFFFFF3EB)
private val OrangeDark   = Color(0xFFEA580C)
private val TextPrimary  = Color(0xFF1A1A1A)
private val TextMuted    = Color(0xFFAAAAAA)
private val CardBg       = Color(0xFFFFFFFF)
private val PageBg       = Color(0xFFF5F5F5)
private val DividerColor = Color(0xFFF0F0F0)
private val BadgeBlue    = Color(0xFF3B82F6)
private val BadgePurple  = Color(0xFFA855F7)
private val BadgeGreen   = Color(0xFF22C55E)
private val MedalGold    = Color(0xFFF59E0B)
private val MedalSilver  = Color(0xFF9CA3AF)
private val MedalOrange  = Color(0xFFF97316)

// ── Panel enum ────────────────────────────────────────────────────────────────
private enum class StatPanel { NONE, CONNECTIONS, HONOR, CLUBS, INTERESTS }

// ── Sample data models ────────────────────────────────────────────────────────
private enum class ConnectionStatus {
    ADD,
    PENDING,
    CONNECTED
}
private data class Connection(
    val initials: String,
    val name: String,
    val sub: String,
    val avatarBg: Color,
    val avatarText: Color,
    val status: ConnectionStatus
)

private enum class ClubStatus {
    JOIN,
    PENDING,
    JOINED
}

private data class Club(
    val name: String,
    val members: String,
    val iconBg: Color,
    val iconTint: Color,
    val status: ClubStatus
)


private data class HonorEntry(
    val rank: Int, val initials: String, val name: String,
    val points: Int, val avatarBg: Color, val isMe: Boolean = false
)

private val sampleConnections = listOf(
    Connection("RK", "Rahul Kumar",  "MCA 2nd Year", Color(0xFFFEF0E6), OrangeDark,ConnectionStatus.CONNECTED),
    Connection("PS", "Priya Sharma", "MCA 3rd Year", Color(0xFFE6F1FB), Color(0xFF0C447C),ConnectionStatus.CONNECTED),
    Connection("AV", "Amit Verma",   "MCA 2nd Year", Color(0xFFE1F5EE), Color(0xFF085041),ConnectionStatus.PENDING),
    Connection("SK", "Sneha Kaur",   "MCA 1st Year", Color(0xFFEEEDFE), Color(0xFF3C3489),ConnectionStatus.CONNECTED),
)

private val sampleClubs = listOf(
    Club("Dev Club",          "142 members",   Color(0xFFE6F1FB), Color(0xFF185FA5),ClubStatus.JOINED),
    Club("Innovators Hub",    "89 members",  Color(0xFFFEF0E6), OrangeDark,ClubStatus.PENDING),
    Club("Photography Club",  "56 members",  Color(0xFFEEEDFE), Color(0xFF3C3489),ClubStatus.JOINED),
    Club("Data Science Club", "98 members",  Color(0xFFE1F5EE), Color(0xFF085041),ClubStatus.JOIN),
)

private val sampleHonor = listOf(
    HonorEntry(1, "PS", "Priya Sharma",  340, Color(0xFFFEF0E6)),
    HonorEntry(2, "AS", "Aryan Sharma",  280, Color(0xFFFFE8D6), isMe = true),
    HonorEntry(3, "RK", "Rahul Kumar",   240, Color(0xFFE1F5EE)),
    HonorEntry(4, "AV", "Amit Verma",    190, Color(0xFFEEEDFE)),
)
private data class Honor(
    val title: String,
    val subtitle: String,
    val color: Color
)
private val sampleBadges = listOf(
    Honor(
        "Problem Solver",
        "Coding Excellence",
        BadgeBlue
    ),
    Honor(
        "Community Helper",
        "Helping Students",
        BadgePurple
    ),
    Honor(
        "Problem Solver",
        "Coding Excellence",
        BadgeBlue
    ),
    Honor(
        "Top Contributor",
        "Campus Impact",
        BadgeGreen
    )
)

private val sampleMedals = listOf(
    Honor(
        "Gold Medal",
        "Top Performer",
        MedalGold
    ),
    Honor(
        "Silver Medal",
        "Outstanding Work",
        MedalSilver
    )
)
private val sampleInterests = mutableListOf("AI/ML", "Web Development", "UI/UX Design", "Photography")

// ── Root screen ───────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {},
    onEditProfile: () -> Unit = {}
) {
    var activePanel by remember { mutableStateOf(StatPanel.NONE) }


    Scaffold(
        containerColor = PageBg,
        topBar = { ProfileTopBar(onBack = onBack, onSettings = onSettings) },
        bottomBar = {
            ProfileBottomBar(
                activePanel = activePanel,
                onEditProfile = onEditProfile,
                onRequestsClick = { },
                onManageCollectionClick = { }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Fixed: header + stats (never scrolls)
            ProfileHeader()
            StatsRow(
                activePanel = activePanel,
                onStatClick = { panel ->
                    activePanel =
                        if (activePanel == panel)
                            StatPanel.NONE
                        else panel

                }
            )

            // Animated panel / profile content
            AnimatedContent(
                targetState = activePanel,
                transitionSpec = {
                    fadeIn() + slideInVertically { it / 10 } togetherWith
                            fadeOut() + slideOutVertically { -it / 10 }
                },
                label = "panel_transition"
            ) { panel ->
                when (panel) {
                    StatPanel.NONE        -> ProfileContent()
                    StatPanel.CONNECTIONS -> ConnectionsPanel()
                    StatPanel.HONOR       -> HonorPanel()
                    StatPanel.CLUBS       -> ClubsPanel()
                    StatPanel.INTERESTS   -> InterestsPanel()
                }
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopBar(onBack: () -> Unit, onSettings: () -> Unit) {
    TopAppBar(
        title = {},
        modifier = Modifier.height(40.dp),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
        },
        actions = {
            IconButton(onClick = onSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = TextPrimary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

// ── Profile header ────────────────────────────────────────────────────────────
@Composable
private fun ProfileHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Color.White)
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(OrangeLight)
                    .border(3.dp, Orange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("AS", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = OrangeDark)
            }

            Spacer(Modifier.height(12.dp))
            Text("Aryan Sharma", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("@aryan.sharma", fontSize = 13.sp, color = Orange)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Tech enthusiast, problem solver and always up for new ideas.",
                fontSize = 12.sp, color = TextMuted,
                textAlign = TextAlign.Center, lineHeight = 18.sp,
                modifier = Modifier.widthIn(max = 260.dp)
            )
            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("BADGES", fontSize = 10.sp, color = TextMuted, letterSpacing = 0.6.sp)
                Spacer(Modifier.width(4.dp))
                BadgeDot(BadgeBlue); Spacer(Modifier.width(3.dp))
                BadgeDot(BadgePurple); Spacer(Modifier.width(3.dp))
                BadgeDot(BadgeGreen)
                Spacer(Modifier.width(8.dp))
                Box(Modifier.width(1.dp).height(14.dp).background(DividerColor))
                Spacer(Modifier.width(8.dp))
                Text("MEDALS", fontSize = 10.sp, color = TextMuted, letterSpacing = 0.6.sp)
                Spacer(Modifier.width(4.dp))
                BadgeDot(MedalGold); Spacer(Modifier.width(3.dp))
                BadgeDot(MedalSilver); Spacer(Modifier.width(3.dp))
                BadgeDot(MedalOrange)
            }
        }
    }
}

@Composable
private fun BadgeDot(color: Color) {
    Box(Modifier.size(15.dp).clip(CircleShape).background(color))
}

// ── Stats row (interactive) ───────────────────────────────────────────────────
@Composable
private fun StatsRow(
    activePanel: StatPanel,
    onStatClick: (StatPanel) -> Unit
) {
    data class StatItem(val value: String, val label: String, val icon: ImageVector, val panel: StatPanel)

    val stats = listOf(
        StatItem("24", "Connections", Icons.Outlined.People,       StatPanel.CONNECTIONS),
        StatItem("12", "Honor",       Icons.Outlined.EmojiEvents,  StatPanel.HONOR),
        StatItem("6",  "Clubs",       Icons.Outlined.Shield,       StatPanel.CLUBS),
        StatItem("4",  "Interests",   Icons.Outlined.Star,         StatPanel.INTERESTS),
    )

    // Floating card sitting below the curved hero
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
            .offset(y = (-3).dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
        ) {
            stats.forEachIndexed { index, stat ->
                val isActive = activePanel == stat.panel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                ){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStatClick(stat.panel) }
                            .background(if (isActive) OrangeLight else Color.White)
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = stat.icon,
                            contentDescription = stat.label,
                            tint = if (isActive) Orange else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            stat.value,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) Orange else TextPrimary
                        )
                        Text(
                            stat.label,
                            fontSize = 9.sp,
                            color = if (isActive) Orange else TextMuted,
                            letterSpacing = 0.4.sp
                        )
                    }
                    // Bottom indicator bar
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(0.5f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                .background(Orange)
                        )
                    }
                    // Right divider (except last)
                    if (index < stats.lastIndex) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(1.dp)
                                .fillMaxHeight(0.5f)
                                .background(DividerColor)
                        )
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(6.dp))
}

// ── Profile content (default, no stat selected) ───────────────────────────────
@Composable
private fun ProfileContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileSection(
            title = "BASIC INFORMATION",
            items = listOf(
                Triple(Icons.Outlined.Person,        "FULL NAME", "Aryan Singla"),
                Triple(Icons.Outlined.School,        "COURSE",    "Masters of Computer Application"),
                Triple(Icons.Outlined.CalendarToday, "YEAR (Batch)",      "3rd Year (2023 – 2027)"),
                Triple(Icons.Outlined.House,         "HOSTEL",    "H6  (FANIBHUSHAN)"),
                Triple(Icons.Outlined.House,         "HOMETOWN",    "Chandigarh")
            ),
            extraContent = {
                GenderAgeRow()
            }
        )
        ProfileSection(
            title = "SOCIAL PRESENCE",
            items = emptyList(),
            extraContent = {
                SocialLinksCard()
            }
        )
        ProfileSection(
            title = "CONTACT DETAILS",
            items = listOf(
                Triple(Icons.Outlined.Phone, "PHONE", "+91 98765 43210"),
                Triple(Icons.Outlined.Email, "EMAIL", "525110035@nitkkr.ac.in"),
            )
        )
        ProfileSection(
            title = "ACCOUNT INFO",
            items = listOf(
                Triple(Icons.Outlined.AccessTime, "MEMBER SINCE", "August 2023"),
            )
        )
    }
}

// ── Section wrapper ───────────────────────────────────────────────────────────
@Composable
private fun ProfileSection(
    title: String,
    items: List<Triple<ImageVector, String, String>>,
    extraContent: (@Composable () -> Unit)? = null
) {
    Column {
        Text(
            text = title,
            fontSize = 11.sp, fontWeight = FontWeight.Bold,
            color = Orange, letterSpacing = 0.9.sp,
            modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                items.forEachIndexed { index, (icon, label, value) ->
                    InfoRow(icon = icon, label = label, value = value)
                    if (index < items.lastIndex || extraContent != null)
                        HorizontalDivider(color = PageBg, thickness = 1.dp)
                }
                extraContent?.invoke()
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(OrangeLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Orange, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 10.sp, color = TextMuted, letterSpacing = 0.4.sp)
            Text(value, fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = TextPrimary,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
@Composable
private fun GenderAgeRow() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),

        horizontalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        SmallInfoItem(
            icon = Icons.Outlined.Wc,
            label = "GENDER",
            value = "Male",
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(34.dp)
                .background(DividerColor)
        )
        SmallInfoItem(
            icon = Icons.Outlined.Cake,
            label = "AGE",
            value = "22",
            modifier = Modifier.weight(1f)
        )
    }
}
@Composable
private fun SmallInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier,
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(OrangeLight),

            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Orange,
                modifier = Modifier.size(17.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        Column {
            Text(
                label,
                fontSize = 10.sp,
                color = TextMuted
            )

            Text(
                value,
                fontSize = 13.sp,
                fontWeight =
                    FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun SocialLinksCard() {

    Column {

        SocialLinkRow(
            iconColor = Color(0xFF1A1A1A),
            iconText = "gh",
            label = "GITHUB",
            value = "github.com/aryan"
        )

        HorizontalDivider(
            color = PageBg,
            thickness = 1.dp
        )

        SocialLinkRow(
            iconColor = Color(0xFF0077B5),
            iconText = "in",
            label = "LINKEDIN",
            value = "linkedin.com/in/aryan"
        )

        HorizontalDivider(
            color = PageBg,
            thickness = 1.dp
        )

        SocialLinkRow(
            iconColor = Color(0xFFE1306C),
            iconText = "ig",
            label = "INSTAGRAM",
            value = "@aryan.sharma"
        )
    }
}
@Composable
private fun SocialLinkRow(
    iconColor: Color,
    iconText: String,
    label: String,
    value: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // open social link later
            }
            .padding(
                horizontal = 14.dp,
                vertical = 12.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconColor),

            contentAlignment =
                Alignment.Center
        ) {
            Text(
                iconText,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                label,
                fontSize = 10.sp,
                color = TextMuted,
                letterSpacing = 0.4.sp
            )

            Text(
                value,
                fontSize = 13.sp,
                fontWeight =
                    FontWeight.Medium,
                color = TextPrimary
            )
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
@Composable
private fun SocialDot(color: Color, letter: String) {
    Box(
        modifier = Modifier.size(28.dp).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(letter, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

// ── Connections panel ─────────────────────────────────────────────────────────
@Composable
private fun ConnectionsPanel() {
    var query by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PanelSearchBar(value = query, onValueChange = { query = it }, placeholder = "Search people to connect…",showEmbeddedPlus = true)
        val connections = remember {
            sampleConnections
                .map { it.copy() }
                .toMutableStateList()
        }

        connections.filter {
            query.isBlank() || it.name.contains(query, ignoreCase = true)
        }.forEachIndexed { index, c ->
            ProfileListCard(
                title = c.name,
                subtitle = c.sub,

                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(c.avatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            c.initials,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = c.avatarText
                        )
                    }
                },

                trailingContent = {

                    Button(onClick = {
                        when (c.status) {
                            ConnectionStatus.ADD -> {
                                connections[index] = c.copy(status = ConnectionStatus.PENDING)
                            }
                            ConnectionStatus.PENDING -> {
                                // no action
                            }
                            ConnectionStatus.CONNECTED -> {
                                // remove popup later
                            }
                        }
                    },

                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding =
                            PaddingValues(
                                horizontal = 10.dp,
                                vertical = 0.dp
                            ),

                        colors = ButtonDefaults
                            .buttonColors(containerColor = when (c.status) {

                                ConnectionStatus.ADD ->
                                    Orange

                                ConnectionStatus.PENDING ->
                                    OrangeLight

                                ConnectionStatus.CONNECTED ->
                                    Color.Transparent
                            },

                                contentColor = when (c.status) {

                                    ConnectionStatus.ADD ->
                                        Color.White

                                    ConnectionStatus.PENDING ->
                                        OrangeDark

                                    ConnectionStatus.CONNECTED ->
                                        TextMuted
                                }
                            ),

                        border =
                            if (c.status == ConnectionStatus.CONNECTED)
                                ButtonDefaults.outlinedButtonBorder
                            else null
                    ) {

                        Text(
                            when (c.status) {
                                ConnectionStatus.ADD ->
                                    "Add"

                                ConnectionStatus.PENDING ->
                                    "Pending"

                                ConnectionStatus.CONNECTED ->
                                    "Remove"
                            },

                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            )
        }
    }
}

// ── Clubs panel ───────────────────────────────────────────────────────────────
@Composable
private fun ClubsPanel() {
    var query by remember { mutableStateOf("") }
    val clubs = remember { sampleClubs.map { it.copy() }.toMutableStateList() }
    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PanelSearchBar(value = query, onValueChange = { query = it }, placeholder = "Search clubs to join…",showEmbeddedPlus = true)
        clubs.filter {
            query.isBlank() || it.name.contains(query, ignoreCase = true)
        }.forEachIndexed { index, c ->
            ProfileListCard(
                title = c.name,
                subtitle = c.members,

                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(c.iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Groups,
                            contentDescription = null,
                            tint = c.iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },

                trailingContent = {

                    Button(onClick = {
                        when (c.status) {
                            ClubStatus.JOIN -> {
                                clubs[index] = c.copy(status = ClubStatus.PENDING)
                            }
                            ClubStatus.PENDING -> {
                                // no action
                            }
                            ClubStatus.JOINED -> {
                                // leave popup later
                            }
                        }
                    },

                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding =
                            PaddingValues(
                                horizontal = 10.dp,
                                vertical = 0.dp
                            ),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = when (c.status) {

                                    ClubStatus.JOIN ->
                                        Orange

                                    ClubStatus.PENDING ->
                                        OrangeLight

                                    ClubStatus.JOINED ->
                                        Orange
                                },

                                contentColor = when (c.status) {

                                    ClubStatus.JOIN ->
                                        Color.White

                                    ClubStatus.PENDING ->
                                        OrangeDark

                                    ClubStatus.JOINED ->
                                        Color.White
                                }
                            )
                    ) {
                        Text(
                            when (c.status) {
                                ClubStatus.JOIN ->
                                    "Join"
                                ClubStatus.PENDING ->
                                    "Pending"
                                ClubStatus.JOINED ->
                                    "Joined"
                            },

                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            )
        }
    }
}

// ── Honor panel ───────────────────────────────────────────────────────────────
@Composable
private fun HonorPanel() {

    var showAllBadges by remember {
        mutableStateOf(false)
    }

    var showAllMedals by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ── Rank + Honor Board button ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column {

                Text(
                    "HONOR RANK",
                    fontSize = 10.sp,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )

                Text(
                    "#2",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Orange
                )
            }

            TextButton(
                onClick = {
                    // open honor board
                }
            ) {

                Icon(
                    Icons.Outlined.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(Modifier.width(4.dp))

                Text("Honor Board")
            }
        }
        HorizontalDivider(
            color = DividerColor,
            thickness = 1.dp
        )
        // ── Badges ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                "BADGES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Orange,
                letterSpacing = 0.6.sp
            )

            IconButton(
                onClick = {
                    showAllBadges =
                        !showAllBadges
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    if (showAllBadges)
                        Icons.Default.KeyboardArrowUp
                    else
                        Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextMuted
                )
            }
        }

        sampleBadges
            .take(
                if (showAllBadges)
                    sampleBadges.size
                else 3
            )
            .forEach { badge ->

                ProfileListCard(
                    title = badge.title,
                    subtitle = badge.subtitle,

                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    badge.color.copy(
                                        alpha = 0.15f
                                    )
                                ),
                            contentAlignment =
                                Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        badge.color
                                    )
                            )
                        }
                    }
                )
            }

        // ── Medals ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                "MEDALS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Orange,
                letterSpacing = 0.6.sp
            )

            IconButton(
                onClick = {
                    showAllMedals =
                        !showAllMedals
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    if (showAllMedals)
                        Icons.Default.KeyboardArrowUp
                    else
                        Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextMuted
                )
            }
        }

        sampleMedals
            .take(
                if (showAllMedals)
                    sampleMedals.size
                else 3
            )
            .forEach { medal ->

                ProfileListCard(
                    title = medal.title,
                    subtitle = medal.subtitle,

                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    medal.color.copy(
                                        alpha = 0.15f
                                    )
                                ),
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(
                                Icons.Outlined.WorkspacePremium,
                                contentDescription = null,
                                tint = medal.color,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
            }
    }
}
// ── Interests panel ───────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestsPanel() {
    val interests = remember { sampleInterests.toMutableStateList() }
    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 2.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Interests", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Orange)
                    .clickable {
                        // open add interest dialog
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Interest",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        interests.forEach { tag ->

            ProfileListCard(
                title = tag,
                subtitle = when (tag) {
                    "AI/ML" -> "Technology"
                    "Web Development" -> "Development"
                    "UI/UX Design" -> "Design"
                    "Photography" -> "Creative"
                    else -> "Interest"
                },

                trailingContent = {
                    IconButton(
                        onClick = {
                            interests.remove(tag)
                        }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        }
    }
}

// ── Shared search bar ─────────────────────────────────────────────────────────
@Composable
private fun PanelSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    showEmbeddedPlus: Boolean = false
) {
    val focusRequester = remember {
        FocusRequester()
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBg
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    focusRequester.requestFocus()
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )

            Box(
                modifier = Modifier.weight(1f)
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    singleLine = true,
                    textStyle =
                        androidx.compose.ui.text.TextStyle(
                            fontSize = 13.sp,
                            color = TextPrimary
                        ),
                    decorationBox = { inner ->

                        if (value.isEmpty()) {
                            Text(
                                placeholder,
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }

                        inner()
                    }
                )
            }

            if (showEmbeddedPlus) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Orange),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// ── Bottom button ───────────────────────────────────────────────────────────────
@Composable
private fun ProfileBottomBar(
    activePanel: StatPanel,
    onEditProfile: () -> Unit,
    onRequestsClick: () -> Unit,
    onManageCollectionClick: () -> Unit
) {
    val config = when (activePanel) {
        StatPanel.NONE -> Triple(
            "Edit Profile",
            Icons.Outlined.Edit,
            onEditProfile
        )

        StatPanel.CONNECTIONS -> Triple(
            "Requests",
            Icons.Outlined.PersonAdd,
            onRequestsClick
        )

        StatPanel.HONOR -> Triple(
            "Manage Collection",
            Icons.Outlined.WorkspacePremium,
            onManageCollectionClick
        )

        StatPanel.CLUBS,
        StatPanel.INTERESTS -> null
    }

    AnimatedVisibility(
        visible = config != null,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        config?.let { (text, icon, click) ->
            Surface(
                color = PageBg,
                shadowElevation = 8.dp,
                tonalElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Button(
                        onClick = click,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Orange
                        )
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
// ── StatsDetailsCards ───────────────────────────────────────────────────────────────────
@Composable
private fun ProfileListCard(
    title: String,
    subtitle: String,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBg
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            leadingContent?.invoke()

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Text(
                    subtitle,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            trailingContent?.invoke()
        }
    }
}
// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileScreen()
    }
}