package com.example.campusconnect.feature.settings.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.campusconnect.feature.settings.ui.components.*

enum class LocationVisibilityId {
    PUBLIC, HIDDEN, CONNECTIONS_ONLY, CLUBS, BATCH, DEPARTMENT
}

data class LocationVisibilityOption(
    val id: LocationVisibilityId,
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color
)

data class SelectableClub(
    val id: Int,
    val name: String,
    val isSelected: Boolean
)

object LocationVisibilityDefaults {

    fun options(): List<LocationVisibilityOption> = listOf(
        LocationVisibilityOption(
            id = LocationVisibilityId.PUBLIC,
            label = "Public",
            subtitle = "Anyone on campus can see your location",
            icon = Icons.Outlined.Public,
            iconTint = IconGreen,
            iconBackground = IconGreenBg
        ),
        LocationVisibilityOption(
            id = LocationVisibilityId.HIDDEN,
            label = "Hidden",
            subtitle = "Your location is not visible to others",
            icon = Icons.Outlined.VisibilityOff,
            iconTint = Color(0xFF6B7280),
            iconBackground = Color(0xFFF1F2F4)
        ),
        LocationVisibilityOption(
            id = LocationVisibilityId.CONNECTIONS_ONLY,
            label = "Connections Only",
            subtitle = "Only your connections can see your location",
            icon = Icons.Outlined.People,
            iconTint = IconPurple,
            iconBackground = IconPurpleBg
        ),
        LocationVisibilityOption(
            id = LocationVisibilityId.CLUBS,
            label = "Clubs",
            subtitle = "Only members of selected clubs can see",
            icon = Icons.Outlined.Groups,
            iconTint = IconOrange,
            iconBackground = IconOrangeBg
        ),
        LocationVisibilityOption(
            id = LocationVisibilityId.BATCH,
            label = "Batch",
            subtitle = "Only your department and year",
            icon = Icons.Outlined.CalendarToday,
            iconTint = IconBlue,
            iconBackground = IconBlueBg
        ),
        LocationVisibilityOption(
            id = LocationVisibilityId.DEPARTMENT,
            label = "Department",
            subtitle = "Only your department",
            icon = Icons.Outlined.School,
            iconTint = IconTeal,
            iconBackground = IconTealBg
        )
    )

    fun clubs(): List<SelectableClub> = listOf(
        SelectableClub(1, "Programming Club", isSelected = true),
        SelectableClub(2, "Design Club", isSelected = true),
        SelectableClub(3, "Photography Club", isSelected = false),
        SelectableClub(4, "Finance Club", isSelected = false),
        SelectableClub(5, "Music Club", isSelected = false),
        SelectableClub(6, "Sports Club", isSelected = false)
    )
}
