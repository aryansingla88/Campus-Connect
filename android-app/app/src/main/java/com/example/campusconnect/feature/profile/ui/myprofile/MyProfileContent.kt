package com.example.campusconnect.feature.profile.ui.myprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.profile.ui.shared.*

@Composable
fun MyProfileContent() {
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
                Triple(Icons.Outlined.CalendarToday, "YEAR (Batch)", "3rd Year (2023 – 2027)"),
                Triple(Icons.Outlined.House,         "HOSTEL",       "H6  (FANIBHUSHAN)"),
                Triple(Icons.Outlined.House,         "HOMETOWN",     "Chandigarh"),
            ),
            extraContent = { GenderAgeRow() }
        )
        ProfileSection(
            title = "SOCIAL PRESENCE",
            items = emptyList(),
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
            Text(
                value, fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = TextPrimary,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GenderAgeRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SmallInfoItem(icon = Icons.Outlined.Wc, label = "GENDER", value = "Male", modifier = Modifier.weight(1f))
        Box(modifier = Modifier.width(1.dp).height(34.dp).background(DividerColor))
        SmallInfoItem(icon = Icons.Outlined.Cake, label = "AGE", value = "22", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SmallInfoItem(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(OrangeLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Orange, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 10.sp, color = TextMuted)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
    }
}

@Composable
private fun SocialLinksCard() {
    Column {
        SocialLinkRow(iconColor = Color(0xFF1A1A1A), iconText = "gh", label = "GITHUB",    value = "github.com/aryan")
        HorizontalDivider(color = PageBg, thickness = 1.dp)
        SocialLinkRow(iconColor = Color(0xFF0077B5), iconText = "in", label = "LINKEDIN",  value = "linkedin.com/in/aryan")
        HorizontalDivider(color = PageBg, thickness = 1.dp)
        SocialLinkRow(iconColor = Color(0xFFE1306C), iconText = "ig", label = "INSTAGRAM", value = "@aryan.sharma")
    }
}

@Composable
private fun SocialLinkRow(iconColor: Color, iconText: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(iconColor),
            contentAlignment = Alignment.Center
        ) {
            Text(iconText, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 10.sp, color = TextMuted, letterSpacing = 0.4.sp)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}
