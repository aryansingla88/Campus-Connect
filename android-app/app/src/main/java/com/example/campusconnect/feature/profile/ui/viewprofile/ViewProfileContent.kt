package com.example.campusconnect.feature.profile.ui.viewprofile

import androidx.compose.foundation.background
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
import com.example.campusconnect.model.PublicUserProfile
import com.example.campusconnect.feature.profile.ui.shared.*

/**
 * Read-only profile content shown when viewing another user's profile.
 * Pass the hydrated [PublicUserProfile]; stub fields appear blank until real API.
 */
@Composable
fun ViewProfileContent(profile: PublicUserProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ViewProfileSection(
            title = "BASIC INFORMATION",
            items = listOf(
                Triple(Icons.Outlined.School,        "COURSE",       profile.course),
                Triple(Icons.Outlined.CalendarToday, "YEAR (Batch)", profile.year),
                Triple(Icons.Outlined.House,         "HOSTEL",       profile.hostel),
                Triple(Icons.Outlined.House,         "HOMETOWN",     profile.hometown),
            )
        )
        ViewProfileSection(
            title = "SOCIAL PRESENCE",
            items = emptyList(),
            extraContent = {
                ViewSocialLinksCard(
                    github    = profile.github,
                    linkedin  = profile.linkedin,
                    instagram = profile.instagram
                )
            }
        )
        // Contact shown only if non-empty (respect privacy settings later)
        if (profile.email.isNotBlank() || profile.phone.isNotBlank()) {
            ViewProfileSection(
                title = "CONTACT DETAILS",
                items = buildList {
                    if (profile.phone.isNotBlank()) add(Triple(Icons.Outlined.Phone, "PHONE", profile.phone))
                    if (profile.email.isNotBlank()) add(Triple(Icons.Outlined.Email, "EMAIL", profile.email))
                }
            )
        }
        ViewProfileSection(
            title = "ACCOUNT INFO",
            items = listOf(
                Triple(Icons.Outlined.AccessTime, "MEMBER SINCE", profile.memberSince),
            )
        )
    }
}

@Composable
private fun ViewProfileSection(
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
                    ViewInfoRow(icon = icon, label = label, value = value)
                    if (index < items.lastIndex || extraContent != null)
                        HorizontalDivider(color = PageBg, thickness = 1.dp)
                }
                extraContent?.invoke()
            }
        }
    }
}

@Composable
private fun ViewInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
                value.ifBlank { "—" },
                fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = TextPrimary,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ViewSocialLinksCard(github: String, linkedin: String, instagram: String) {
    Column {
        if (github.isNotBlank()) {
            SocialLinkRow(iconColor = Color(0xFF1A1A1A), iconText = "gh", label = "GITHUB",    value = github)
            HorizontalDivider(color = PageBg, thickness = 1.dp)
        }
        if (linkedin.isNotBlank()) {
            SocialLinkRow(iconColor = Color(0xFF0077B5), iconText = "in", label = "LINKEDIN",  value = linkedin)
            HorizontalDivider(color = PageBg, thickness = 1.dp)
        }
        if (instagram.isNotBlank()) {
            SocialLinkRow(iconColor = Color(0xFFE1306C), iconText = "ig", label = "INSTAGRAM", value = instagram)
        }
    }
}

@Composable
private fun SocialLinkRow(iconColor: Color, iconText: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
