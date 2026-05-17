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

// ── Root screen ──────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {},
    onEditProfile: () -> Unit = {}
) {
    Scaffold(
        containerColor = PageBg,
        topBar = {
            ProfileTopBar(onBack = onBack, onSettings = onSettings)
        },
        bottomBar = {
            EditProfileButton(onClick = onEditProfile)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Fixed header (not scrollable)
            ProfileHeader()
            StatsRow()

            // Scrollable content below stats
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
                        Triple(Icons.Outlined.Person,        "FULL NAME",    "Aryan Singla"),
                        Triple(Icons.Outlined.School,        "COURSE",       "Masters of Computer Application"),
                        Triple(Icons.Outlined.CalendarToday, "YEAR",         "3rd Year (2023 – 2027)"),
                        Triple(Icons.Outlined.House,         "HOSTEL",       "H6"),
                        Triple(Icons.Outlined.Wc,            "GENDER",       "Male"),
                        Triple(Icons.Outlined.Cake,          "AGE",          "22"),
                    )
                )

                ProfileSection(
                    title = "ABOUT YOU",
                    items = listOf(
                        Triple(Icons.Outlined.Edit,  "BIO",       "Tech enthusiast, problem solver and always up for new ideas."),
                        Triple(Icons.Outlined.Star,  "INTERESTS", "AI/ML, Web Dev, UI/UX, Photography"),
                    ),
                    extraContent = { SocialLinksCard() }
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

// ── Profile header ───────────────────────────────────────────────────────────
@Composable
private fun ProfileHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 20.dp, end =20.dp , top = 6.dp , bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(OrangeLight)
                .border(2.5.dp, Orange, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("AS", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = OrangeDark)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Aryan Sharma", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("@aryan.sharma", fontSize = 13.sp, color = Orange)

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "Tech enthusiast, problem solver and always up for new ideas.",
            fontSize = 12.sp,
            color = TextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Badges & medals
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("BADGES", fontSize = 10.sp, color = TextMuted, letterSpacing = 0.6.sp)
            Spacer(modifier = Modifier.width(4.dp))
            BadgeDot(BadgeBlue)
            Spacer(modifier = Modifier.width(3.dp))
            BadgeDot(BadgePurple)
            Spacer(modifier = Modifier.width(3.dp))
            BadgeDot(BadgeGreen)

            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.width(1.dp).height(14.dp).background(DividerColor))
            Spacer(modifier = Modifier.width(8.dp))

            Text("MEDALS", fontSize = 10.sp, color = TextMuted, letterSpacing = 0.6.sp)
            Spacer(modifier = Modifier.width(4.dp))
            BadgeDot(MedalGold)
            Spacer(modifier = Modifier.width(3.dp))
            BadgeDot(MedalSilver)
            Spacer(modifier = Modifier.width(3.dp))
            BadgeDot(MedalOrange)
        }
    }
}

@Composable
private fun BadgeDot(color: Color) {
    Box(
        modifier = Modifier
            .size(15.dp)
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
            .background(Color.White)
            .border(width = 1.dp, color = DividerColor)
    ) {
        listOf(
            "24" to "CONNECTIONS",
            "12" to "HONOR",
            "6"  to "CLUBS",
            "4"  to "INTERESTS"
        ).forEachIndexed { index, (value, label) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (index < 3) Modifier.border(
                            width = 1.dp,
                            color = DividerColor,
                            shape = RoundedCornerShape(0.dp)
                        ) else Modifier
                    )
                    .padding(vertical = 11.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(label, fontSize = 9.sp, color = TextMuted, letterSpacing = 0.6.sp)
            }
        }
    }
}

// ── Section wrapper ──────────────────────────────────────────────────────────
@Composable
private fun ProfileSection(
    title: String,
    items: List<Triple<ImageVector, String, String>>,
    extraContent: (@Composable () -> Unit)? = null
) {
    Column {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Orange,
            letterSpacing = 0.9.sp,
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
                    if (index < items.lastIndex || extraContent != null) {
                        HorizontalDivider(color = PageBg, thickness = 1.dp)
                    }
                }
                extraContent?.invoke()
            }
        }
    }
}

// ── Info row ─────────────────────────────────────────────────────────────────
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
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(OrangeLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Orange, modifier = Modifier.size(17.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 10.sp, color = TextMuted, letterSpacing = 0.4.sp)
            Text(
                value,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Social links card ────────────────────────────────────────────────────────
@Composable
private fun SocialLinksCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(OrangeLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Link, contentDescription = null, tint = Orange, modifier = Modifier.size(17.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("SOCIAL LINKS", fontSize = 10.sp, color = TextMuted, letterSpacing = 0.4.sp)
            Text("3 connected", fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SocialDot(Color(0xFF0077B5), "in")
            SocialDot(Color(0xFF1A1A1A), "gh")
            SocialDot(Color(0xFFE1306C), "ig")
        }
        Spacer(modifier = Modifier.width(6.dp))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SocialDot(color: Color, letter: String) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(letter, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

// ── Fixed edit button ────────────────────────────────────────────────────────
@Composable
private fun EditProfileButton(onClick: () -> Unit) {
    Surface(
        color = PageBg,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange)
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Edit Profile",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.1.sp
                )
            }
        }
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