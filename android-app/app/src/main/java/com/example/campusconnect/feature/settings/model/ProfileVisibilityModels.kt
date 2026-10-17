package com.example.campusconnect.feature.settings.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.campusconnect.feature.settings.ui.components.*

// --- "Always Visible" — static, non-toggleable fields -----------------------------------
data class AlwaysVisibleField(
    val label: String,
    val icon: ImageVector
)

// --- "Control Visibility" — user-toggleable fields ---------------------------------------
enum class VisibilityFieldId {
    PHONE_NUMBER, SOCIAL_LINKS, HOMETOWN, GENDER, AGE, HONORS_ACHIEVEMENTS
}

data class VisibilityToggle(
    val id: VisibilityFieldId,
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color,
    val isOn: Boolean
)

object ProfileVisibilityDefaults {

    fun alwaysVisibleFields(): List<AlwaysVisibleField> = listOf(
        AlwaysVisibleField("Full Name", Icons.Outlined.Badge),
        AlwaysVisibleField("Username", Icons.Outlined.AlternateEmail),
        AlwaysVisibleField("Course", Icons.Outlined.MenuBook),
        AlwaysVisibleField("Year (Batch)", Icons.Outlined.CalendarToday)
    )

    fun controlFields(): List<VisibilityToggle> = listOf(
        VisibilityToggle(
            id = VisibilityFieldId.PHONE_NUMBER,
            label = "Phone Number",
            subtitle = "Show your phone number on profile",
            icon = Icons.Outlined.Phone,
            iconTint = IconOrange,
            iconBackground = IconOrangeBg,
            isOn = true
        ),
        VisibilityToggle(
            id = VisibilityFieldId.SOCIAL_LINKS,
            label = "Social Links",
            subtitle = "Show your social profiles",
            icon = Icons.Outlined.Link,
            iconTint = IconPurple,
            iconBackground = IconPurpleBg,
            isOn = true
        ),
        VisibilityToggle(
            id = VisibilityFieldId.HOMETOWN,
            label = "Hometown",
            subtitle = "Show your hometown on profile",
            icon = Icons.Outlined.Home,
            iconTint = IconBlue,
            iconBackground = IconBlueBg,
            isOn = true
        ),
        VisibilityToggle(
            id = VisibilityFieldId.GENDER,
            label = "Gender",
            subtitle = "Show your gender on profile",
            icon = Icons.Outlined.Wc,
            iconTint = IconTeal,
            iconBackground = IconTealBg,
            isOn = false
        ),
        VisibilityToggle(
            id = VisibilityFieldId.AGE,
            label = "Age",
            subtitle = "Show your age on profile",
            icon = Icons.Outlined.Cake,
            iconTint = IconRed,
            iconBackground = IconRedBg,
            isOn = false
        ),
        VisibilityToggle(
            id = VisibilityFieldId.HONORS_ACHIEVEMENTS,
            label = "Honors & Achievements",
            subtitle = "Show your honors and achievements in header",
            icon = Icons.Outlined.WorkspacePremium,
            iconTint = IconOrange,
            iconBackground = IconOrangeBg,
            isOn = true
        )
    )
}
