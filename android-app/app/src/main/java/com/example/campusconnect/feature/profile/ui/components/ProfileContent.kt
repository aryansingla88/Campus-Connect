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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.profile.model.ProfileMode
import com.example.campusconnect.feature.profile.model.PublicUserProfile
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter


// -- Constants ----------------------------------------------------
private val HOSTEL_OPTIONS = listOf("Hostel 1","Hostel 2","Hostel 3","Hostel 4","Hostel 5","Day Scholar")
private val DOB_ISO     = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val DOB_DISPLAY = DateTimeFormatter.ofPattern("dd MMM yyyy")
private fun dobToAge(dob: String)     = runCatching { Period.between(LocalDate.parse(dob, DOB_ISO), LocalDate.now()).years }.getOrNull()
private fun dobToDisplay(dob: String) = runCatching { LocalDate.parse(dob, DOB_ISO).format(DOB_DISPLAY) }.getOrElse { dob }

//-------------------------------
// Profile Content
//-------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    profile: PublicUserProfile,
    mode: ProfileMode,
    isEditMode: Boolean = false,
    onValueChange: (PublicUserProfile) -> Unit = {}
) {
    val canShowPhone =
        mode == ProfileMode.OWN || profile.showPhone

    val canShowSocials =
        mode == ProfileMode.OWN || profile.showSocials

    val context = LocalContext.current

    //-------------------------------
    // Open Social URL
    //-------------------------------

    fun openSocialUrl(
        value: String,
        platform: String
    ) {
        if (value.isBlank()) return

        val url = when (platform) {

            "github" -> {
                if (
                    value.startsWith("http://") ||
                    value.startsWith("https://")
                ) {
                    value
                } else {
                    "https://github.com/$value"
                }
            }

            "linkedin" -> {
                if (
                    value.startsWith("http://") ||
                    value.startsWith("https://")
                ) {
                    value
                } else {
                    "https://linkedin.com/in/$value"
                }
            }

            "instagram" -> {
                if (
                    value.startsWith("http://") ||
                    value.startsWith("https://")
                ) {
                    value
                } else {
                    "https://instagram.com/${value.removePrefix("@")}"
                }
            }

            else -> value
        }

        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
        )
    }

    //-------------------------------
    // Profile Content
    //-------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 12.dp,
                end = 12.dp,
                top = 0.dp,
                bottom = 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        //-------------------------------
        // Basic Information
        //-------------------------------

        ProfileSection(title = "Basic Information") {

            if (
                mode == ProfileMode.OWN &&
                profile.fullName.isNotBlank()
            ) {
                InfoRow(
                    Icons.Outlined.Person,
                    "FULL NAME",
                    profile.fullName,
                    isEditMode = false
                )

                SectionDivider()
            }

            if (profile.programName.isNotBlank()) {
                InfoRow(
                    Icons.Outlined.School,
                    "COURSE",
                    profile.programName,
                    isEditMode = false
                )

                SectionDivider()
            }

            if (
                profile.academicYear.isNotBlank() &&
                profile.batch.isNotBlank()
            ) {
                InfoRow(
                    Icons.Outlined.CalendarToday,
                    "YEAR (BATCH)",
                    "${profile.academicYear} (${profile.batch})",
                    isEditMode = false
                )

                SectionDivider()
            }

            if (
                profile.hostel.isNotBlank() ||
                isEditMode
            ) {
                InfoRow(
                    Icons.Outlined.House,
                    "HOSTEL",
                    profile.hostel,
                    isEditMode,
                    inputType = InputType.Dropdown(HOSTEL_OPTIONS)
                ) {
                    onValueChange(
                        profile.copy(hostel = it)
                    )
                }

                SectionDivider()
            }

            if (
                profile.hometown.isNotBlank() ||
                isEditMode
            ) {
                InfoRow(
                    Icons.Outlined.LocationOn,
                    "HOMETOWN",
                    profile.hometown,
                    isEditMode
                ) {
                    onValueChange(
                        profile.copy(hometown = it)
                    )
                }

                SectionDivider()
            }

            if (
                profile.gender.isNotBlank() ||
                profile.dob.isNotBlank()
            ) {
                GenderAgeRow(
                    gender = profile.gender,
                    dob = profile.dob
                )
            }
        }

        //-------------------------------
        // Social Presence
        //-------------------------------

        if (canShowSocials) {

            val hasAnySocial =
                profile.github.isNotBlank() ||
                        profile.linkedin.isNotBlank() ||
                        profile.instagram.isNotBlank()

            if (hasAnySocial || isEditMode) {

                ProfileSection(
                    title = "Social Presence"
                ) {

                    //-------------------------------
                    // Edit Mode
                    //-------------------------------

                    if (isEditMode) {

                        InfoRow(
                            icon = Icons.Outlined.Link,
                            label = "GITHUB",
                            value = profile.github,
                            isEditMode = true
                        ) {
                            onValueChange(
                                profile.copy(
                                    github = it
                                )
                            )
                        }

                        SectionDivider()

                        InfoRow(
                            icon = Icons.Outlined.Link,
                            label = "LINKEDIN",
                            value = profile.linkedin,
                            isEditMode = true
                        ) {
                            onValueChange(
                                profile.copy(
                                    linkedin = it
                                )
                            )
                        }

                        SectionDivider()

                        InfoRow(
                            icon = Icons.Outlined.Link,
                            label = "INSTAGRAM",
                            value = profile.instagram,
                            isEditMode = true
                        ) {
                            onValueChange(
                                profile.copy(
                                    instagram = it
                                )
                            )
                        }

                        //-------------------------------
                        // Normal / View Mode
                        //-------------------------------

                    } else {

                        SocialPresenceCard(
                            github = profile.github
                                .takeIf { it.isNotBlank() },

                            linkedin = profile.linkedin
                                .takeIf { it.isNotBlank() },

                            instagram = profile.instagram
                                .takeIf { it.isNotBlank() },

                            other = null,

                            onGithubClick = {
                                openSocialUrl(
                                    profile.github,
                                    "github"
                                )
                            },

                            onLinkedInClick = {
                                openSocialUrl(
                                    profile.linkedin,
                                    "linkedin"
                                )
                            },

                            onInstagramClick = {
                                openSocialUrl(
                                    profile.instagram,
                                    "instagram"
                                )
                            }
                        )
                    }
                }
            }
        }

        //-------------------------------
        // Contact Details
        //-------------------------------

        val hasContact =
            isEditMode ||
                    (
                            canShowPhone &&
                                    profile.phone.isNotBlank()
                            ) ||
                    profile.email.isNotBlank()

        if (hasContact) {

            ProfileSection(
                title = "Contact Details"
            ) {

                if (
                    canShowPhone &&
                    (
                            profile.phone.isNotBlank() ||
                                    isEditMode
                            )
                ) {
                    InfoRow(
                        Icons.Outlined.Phone,
                        "PHONE",
                        profile.phone,
                        isEditMode
                    ) {
                        onValueChange(
                            profile.copy(
                                phone = it
                            )
                        )
                    }

                    if (profile.email.isNotBlank()) {
                        SectionDivider()
                    }
                }

                if (profile.email.isNotBlank()) {
                    InfoRow(
                        Icons.Outlined.Email,
                        "EMAIL",
                        profile.email
                    )
                }
            }
        }

        //-------------------------------
        // Account Info
        //-------------------------------

        if (profile.memberSince.isNotBlank()) {

            ProfileSection(
                title = "Account Info"
            ) {
                InfoRow(
                    Icons.Outlined.AccessTime,
                    "MEMBER SINCE",
                    profile.memberSince
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// InputType — sealed class so InfoRow knows what control to render
// ---------------------------------------------------------------------------
private sealed interface InputType {
    data object FreeText                          : InputType
    data class  Int(val hint: String)             : InputType
    data class  Dropdown(val options: List<String>) : InputType
}

// --- Info row -----------------------------------------------------------------
@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    isEditMode: Boolean    = false,
    inputType: InputType   = InputType.FreeText,
    onValueChange: (String) -> Unit = {}
) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconChip(icon)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 10.sp, color = TextMuted, letterSpacing = 0.4.sp)
            if (isEditMode) {
                when (inputType) {
                    is InputType.FreeText          -> FreeTextInput(value, "Add ${label.lowercase()}", onValueChange)
                    is InputType.Int               -> IntInput(value, inputType.hint, onValueChange)
                    is InputType.Dropdown          -> DropdownInput(value, inputType.options, "Select ${label.lowercase()}", onValueChange)
                }
            } else {
                ViewText(value)
            }
        }
        if (!isEditMode) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// --- Gender + Age row -----------------------------------------------------------------
@Composable
private fun GenderAgeRow(
    gender: String,
    dob: String
) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Gender half
        SmallInfoItem(Icons.Outlined.Wc, "GENDER", modifier = Modifier.weight(1f)) {
            ViewText(gender)
        }

        Box(Modifier.width(1.dp).height(34.dp).background(DividerColor))

        // Age half
        SmallInfoItem(Icons.Outlined.Cake, "AGE", modifier = Modifier.weight(1f)) {
            ViewText(dobToAge(dob)?.toString() ?: "—")
        }
    }
}

// --- Section wrapper -----------------------------------------------------------------
@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text          = title,
            fontSize      = 18.sp,
            fontWeight    = FontWeight.Bold,
            color         = TextPrimary,
            modifier      = Modifier.padding(start = 2.dp, bottom = 8.dp)
        )
        Card(
            shape     = RoundedCornerShape(18.dp),
            colors    = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
    }
}

// --- SmallInfoItem — icon + label + any content slot -----------------------------------------------------------------
@Composable
private fun SmallInfoItem(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        IconChip(icon)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 10.sp, color = TextMuted)
            content()
        }
    }
}

// --- Social link row -----------------------------------------------------------------
@Composable
private fun SocialLinkRow(iconColor: Color, iconText: String, label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier          = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(iconColor), Alignment.Center) {
            Text(iconText, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 10.sp, color = TextMuted, letterSpacing = 0.4.sp)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}

// --- Shared small components -----------------------------------------------------------------
@Composable
private fun IconChip(icon: ImageVector) {
    Box(
        modifier         = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(OrangeLight),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Orange, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(color = PageBg, thickness = 1.dp)
}


// -- Shared edit-mode primitives ----------------------------------------------------

@Composable
private fun EditUnderline(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind { drawLine(OrangeDark, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx()) }
            .padding(bottom = 4.dp),
        content = content
    )
}


@Composable
private fun ViewText(value: String) {
    Text(
        text       = value.ifBlank { "—" },
        fontSize   = 13.5.sp,
        fontWeight = FontWeight.Medium,
        color      = TextPrimary,
        maxLines   = 2,
        overflow   = TextOverflow.Ellipsis
    )
}


// -- Input controls  ----------------------------------------------------

@Composable
private fun FreeTextInput(
    value: String,
    hint: String,
    onValueChange: (String) -> Unit
) {
    EditUnderline {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            ),
            decorationBox = { inner ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isBlank()) {
                        Text(text = hint, fontSize = 13.sp, color = TextMuted
                        )
                    }
                    inner()
                }
            }
        )
    }
}

@Composable
private fun IntInput(
    value: String,
    hint: String,
    onValueChange: (String) -> Unit
) {
    EditUnderline {
        BasicTextField(
            value = value,
            onValueChange = {
                onValueChange(it.filter(Char::isDigit))
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            ),
            decorationBox = { inner ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isBlank()) {
                        Text(
                            text = hint,
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                    inner()
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownInput(value: String, options: List<String>, hint: String, onValueChange: (String) -> Unit) {

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        EditUnderline(modifier = Modifier.menuAnchor()) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(
                    text       = value.ifBlank { hint },
                    fontSize   = 13.5.sp,
                    fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.Medium,
                    color      = if (value.isBlank()) TextMuted else TextPrimary
                )
                Icon(Icons.Default.KeyboardArrowDown, null, tint = OrangeDark, modifier = Modifier.size(16.dp))
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = {
                        Text(text = opt, fontSize = 14.sp,
                            fontWeight =
                                if (opt == value)
                                    FontWeight.SemiBold
                                else FontWeight.Medium,
                            color =
                                if (opt == value)
                                    Orange
                                else TextPrimary
                        )
                    },
                    onClick        = { onValueChange(opt); expanded = false },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DobInput(dob: String, onPickRequest: () -> Unit) {
    EditUnderline(modifier = Modifier.clickable { onPickRequest() }) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(
                text       = if (dob.isBlank()) "Pick date" else dobToDisplay(dob),
                fontSize   = 13.5.sp,
                fontWeight = if (dob.isBlank()) FontWeight.Normal else FontWeight.Medium,
                color      = if (dob.isBlank()) TextMuted else TextPrimary
            )
            Icon(Icons.Outlined.DateRange, "Pick date of birth", tint = OrangeDark, modifier = Modifier.size(16.dp))
        }
    }
}

