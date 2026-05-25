package com.example.campusconnect.feature.profile.ui.components

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
import com.example.campusconnect.feature.profile.model.ProfileMode
import com.example.campusconnect.feature.profile.model.PublicUserProfile

@Composable
fun ProfileContent(
    profile: PublicUserProfile,
    mode: ProfileMode
) {
    val clickable = mode == ProfileMode.OWN

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Basic Information ─────────────────────────────────────────────────
        ProfileSection(title = "BASIC INFORMATION") {
            if (mode == ProfileMode.OWN && profile.fullName.isNotBlank()) {
                InfoRow(Icons.Outlined.Person, "FULL NAME", profile.fullName, clickable)
                SectionDivider()
            }
            if (profile.course.isNotBlank()) {
                InfoRow(Icons.Outlined.School, "COURSE", profile.course, clickable)
                SectionDivider()
            }
            if (profile.year.isNotBlank()) {
                InfoRow(Icons.Outlined.CalendarToday, "YEAR (BATCH)", profile.year, clickable)
                SectionDivider()
            }
            if (profile.hostel.isNotBlank()) {
                InfoRow(Icons.Outlined.House, "HOSTEL", profile.hostel, clickable)
                SectionDivider()
            }
            if (profile.hometown.isNotBlank()) {
                InfoRow(Icons.Outlined.LocationOn, "HOMETOWN", profile.hometown, clickable)
                SectionDivider()
            }
            if (profile.gender.isNotBlank() || profile.age != 0) {
                GenderAgeRow(gender = profile.gender, age = profile.age.toString())
            }
        }

        // ── Social Presence ───────────────────────────────────────────────────
        if (profile.showSocials) {
            val hasAnySocial = profile.github.isNotBlank() ||
                    profile.linkedin.isNotBlank() ||
                    profile.instagram.isNotBlank()
            if (hasAnySocial) {
                ProfileSection(title = "SOCIAL PRESENCE") {
                    if (profile.github.isNotBlank()) {
                        SocialLinkRow(Color(0xFF1A1A1A), "gh", "GITHUB",    profile.github,    clickable)
                        if (profile.linkedin.isNotBlank() || profile.instagram.isNotBlank()) SectionDivider()
                    }
                    if (profile.linkedin.isNotBlank()) {
                        SocialLinkRow(Color(0xFF0077B5), "in", "LINKEDIN",  profile.linkedin,  clickable)
                        if (profile.instagram.isNotBlank()) SectionDivider()
                    }
                    if (profile.instagram.isNotBlank()) {
                        SocialLinkRow(Color(0xFFE1306C), "ig", "INSTAGRAM", profile.instagram, clickable)
                    }
                }
            }
        }

        // ── Contact Details ───────────────────────────────────────────────────
        val hasContact = (profile.showPhone && profile.phone.isNotBlank()) ||
                profile.email.isNotBlank()
        if (hasContact) {
            ProfileSection(title = "CONTACT DETAILS") {
                if (profile.showPhone && profile.phone.isNotBlank()) {
                    InfoRow(Icons.Outlined.Phone, "PHONE", profile.phone, clickable)
                    if (profile.email.isNotBlank()) SectionDivider()
                }
                if (profile.email.isNotBlank()) {
                    InfoRow(Icons.Outlined.Email, "EMAIL", profile.email, clickable)
                }
            }
        }

        // ── Account Info ──────────────────────────────────────────────────────
        if (profile.memberSince.isNotBlank()) {
            ProfileSection(title = "ACCOUNT INFO") {
                InfoRow(Icons.Outlined.AccessTime, "MEMBER SINCE", profile.memberSince, clickable)
            }
        }
    }
}

// ── Section wrapper ───────────────────────────────────────────────────────────
@Composable
private fun ProfileSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
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
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
    }
}

// ── Info row ──────────────────────────────────────────────────────────────────
@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    clickable: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (clickable) Modifier.clickable {} else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconChip(icon)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 10.sp, color = TextMuted, letterSpacing = 0.4.sp)
            Text(
                value.ifBlank { "—" },
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Gender + Age row ──────────────────────────────────────────────────────────
@Composable
private fun GenderAgeRow(gender: String, age: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (gender.isNotBlank()) {
            SmallInfoItem(
                icon     = Icons.Outlined.Wc,
                label    = "GENDER",
                value    = gender,
                modifier = Modifier.weight(1f)
            )
        }
        if (gender.isNotBlank() && age.isNotBlank()) {
            Box(modifier = Modifier.width(1.dp).height(34.dp).background(DividerColor))
        }
        if (age.isNotBlank()) {
            SmallInfoItem(
                icon     = Icons.Outlined.Cake,
                label    = "AGE",
                value    = age,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SmallInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        IconChip(icon)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 10.sp, color = TextMuted)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
    }
}

// ── Social link row ───────────────────────────────────────────────────────────
@Composable
private fun SocialLinkRow(
    iconColor: Color,
    iconText: String,
    label: String,
    value: String,
    clickable: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (clickable) Modifier.clickable {} else Modifier)
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
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ── Shared small components ───────────────────────────────────────────────────
@Composable
private fun IconChip(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(OrangeLight),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Orange, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(color = PageBg, thickness = 1.dp)
}