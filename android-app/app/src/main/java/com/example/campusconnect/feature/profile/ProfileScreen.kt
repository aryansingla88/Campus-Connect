package com.example.campusconnect.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Color constants ──────────────────────────────────────────────────────────
private val Purple = Color(0xFF6C63FF)
private val PurpleLight = Color(0xFFEEEDFF)
private val TextPrimary = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF9E9EA7)
private val TextMuted = Color(0xFFBBBBC5)
private val CardBg = Color(0xFFF8F8FB)
private val DividerColor = Color(0xFFEEEEF5)
private val BadgeBlue = Color(0xFF4A90D9)
private val BadgePurple = Color(0xFF7B68EE)
private val BadgeGreen = Color(0xFF50C878)
private val MedalGold = Color(0xFFFFD700)
private val MedalSilver = Color(0xFFC0C0C0)
private val MedalBronze = Color(0xFFCD7F32)

// ── Tab enum ─────────────────────────────────────────────────────────────────
private enum class ProfileTab { PROFILE, CLUBS }

// ── Root screen ──────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(ProfileTab.PROFILE) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            ProfileTopBar(onBack = onBack, onSettings = onSettings)
        },

        // Fixed edit button only in Profile tab
        bottomBar = {

            if (selectedTab == ProfileTab.PROFILE) {

                Surface(
                    color = Color.White,
                    tonalElevation = 6.dp,
                    shadowElevation = 10.dp
                ) {

                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 20.dp,
                                vertical = 14.dp
                            )
                            .height(52.dp),

                        shape = RoundedCornerShape(14.dp),

                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                Purple.copy(alpha = 0.35f)
                            )
                        ),

                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Purple
                        )
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Edit Profile Information",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: avatar + name + badges
            ProfileHeader()

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            StatsRow()

            Spacer(modifier = Modifier.height(8.dp))

            // Tab row
            ProfileTabRow(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

            // Tab content
            when (selectedTab) {
                ProfileTab.PROFILE -> ProfileTabContent()
                ProfileTab.CLUBS   -> ClubsTabContent()
            }
        }
    }
}

// ── Top bar ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopBar(onBack: () -> Unit, onSettings: () -> Unit) {
    TopAppBar(
        title = {},
        modifier = Modifier.height(48.dp),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = onSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

// ── Profile header ───────────────────────────────────────────────────────────
@Composable
private fun ProfileHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8E8F0))
                .border(2.dp, Purple.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Name + handle + bio
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Aryan Sharma",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "@aryan.sharma",
                fontSize = 13.sp,
                color = Purple,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tech enthusiast, problem solver and always up for new ideas.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Badges + medals column
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Badges", fontSize = 10.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                BadgeCircle(color = BadgeBlue)
                BadgeCircle(color = BadgePurple)
                BadgeCircle(color = BadgeGreen)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Medals", fontSize = 10.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                BadgeCircle(color = MedalGold, size = 20.dp)
                BadgeCircle(color = MedalSilver, size = 20.dp)
                BadgeCircle(color = MedalBronze, size = 20.dp)
            }
        }
    }
}

@Composable
private fun BadgeCircle(color: Color, size: androidx.compose.ui.unit.Dp = 22.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

// ── Stats row ────────────────────────────────────────────────────────────────
@Composable
private fun StatsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(value = "24", label = "Connections")
        StatDivider()
        StatItem(value = "6", label = "Clubs")
        StatDivider()
        StatItem(value = "12", label = "Honor")
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(text = label, fontSize = 10.sp, color = TextMuted)
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 14.dp)
            .width(1.dp)
            .height(28.dp)
            .background(DividerColor)
    )
}

// ── Tab row ──────────────────────────────────────────────────────────────────
@Composable
private fun ProfileTabRow(
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
    ) {
        ProfileTab.values().forEach { tab ->
            val isSelected = selectedTab == tab
            val icon = if (tab == ProfileTab.PROFILE) Icons.Outlined.Person else Icons.Outlined.Group
            val label = tab.name.lowercase().replaceFirstChar { it.uppercase() }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Purple else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Purple else TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(if (isSelected) Purple else Color.Transparent)
                )
            }
        }
    }
    HorizontalDivider(color = DividerColor)
}

// ── Profile tab content ──────────────────────────────────────────────────────
@Composable
private fun ProfileTabContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Basic Information section
        SectionHeader(title = "Basic Information")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoCard(icon = Icons.Outlined.Person, label = "Full Name", value = "Aryan Sharma")
            InfoCard(
                icon = Icons.Outlined.School,
                label = "Course",
                value = "B.Tech – Computer Science Engineering"
            )
            InfoCard(
                icon = Icons.Outlined.CalendarToday,
                label = "Year",
                value = "3rd Year (2023 – 2027)"
            )
        }

        // About You section
        SectionHeader(title = "About You")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoCard(
                icon = Icons.Outlined.Edit,
                label = "Bio",
                value = "Tech enthusiast, problem solver and always up for new ideas."
            )
            InfoCard(
                icon = Icons.Outlined.Star,
                label = "Interests",
                value = "AI/ML, Web Development, UI/UX, Photography"
            )
            SocialLinksCard()
        }

    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    )
}

@Composable
private fun InfoCard(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .clickable {}
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PurpleLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 11.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

    }
}

@Composable
private fun SocialLinksCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .clickable {}
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PurpleLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Link,
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Social Links",
            fontSize = 13.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        // Social icons (LinkedIn, GitHub, Instagram)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SocialIcon(color = Color(0xFF0077B5), letter = "in")
            SocialIcon(color = Color(0xFF333333), letter = "gh")
            SocialIcon(color = Color(0xFFE1306C), letter = "ig")
        }

        Spacer(modifier = Modifier.width(6.dp))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SocialIcon(color: Color, letter: String) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            fontSize = 8.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Clubs tab (placeholder) ──────────────────────────────────────────────────
@Composable
private fun ClubsTabContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "No clubs joined yet.", color = TextMuted, fontSize = 14.sp)
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileScreen()
    }
}