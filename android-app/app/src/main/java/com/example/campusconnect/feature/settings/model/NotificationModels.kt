package com.example.campusconnect.feature.settings.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.campusconnect.feature.settings.ui.components.*

enum class NotificationId {
    CONNECTION_REQUESTS, CLUB_UPDATES, EVENT_REMINDERS, MENTIONS_REPLIES,
    APP_ANNOUNCEMENTS, PRODUCT_UPDATES
}

data class NotificationToggle(
    val id: NotificationId,
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color,
    val isOn: Boolean
)

object NotificationDefaults {

    fun activityToggles(): List<NotificationToggle> = listOf(
        NotificationToggle(
            id = NotificationId.CONNECTION_REQUESTS,
            label = "Connection Requests",
            subtitle = "New connection requests",
            icon = Icons.Outlined.PersonAddAlt,
            iconTint = IconOrange,
            iconBackground = IconOrangeBg,
            isOn = true
        ),
        NotificationToggle(
            id = NotificationId.CLUB_UPDATES,
            label = "Club Updates",
            subtitle = "Updates from clubs you're part of",
            icon = Icons.Outlined.Groups,
            iconTint = IconBlue,
            iconBackground = IconBlueBg,
            isOn = true
        ),
        NotificationToggle(
            id = NotificationId.EVENT_REMINDERS,
            label = "Event Reminders",
            subtitle = "Reminders for events you have registered",
            icon = Icons.Outlined.EmojiEvents,
            iconTint = IconOrange,
            iconBackground = IconOrangeBg,
            isOn = true
        ),
        NotificationToggle(
            id = NotificationId.MENTIONS_REPLIES,
            label = "Mentions & Replies",
            subtitle = "When someone mentions you",
            icon = Icons.Outlined.AlternateEmail,
            iconTint = IconPurple,
            iconBackground = IconPurpleBg,
            isOn = true
        )
    )

    fun systemToggles(): List<NotificationToggle> = listOf(
        NotificationToggle(
            id = NotificationId.APP_ANNOUNCEMENTS,
            label = "App Announcements",
            subtitle = "Important updates from Campus Connect",
            icon = Icons.Outlined.Campaign,
            iconTint = IconRed,
            iconBackground = IconRedBg,
            isOn = true
        ),
        NotificationToggle(
            id = NotificationId.PRODUCT_UPDATES,
            label = "Product Updates",
            subtitle = "New features and improvements",
            icon = Icons.Outlined.Settings,
            iconTint = IconBlue,
            iconBackground = IconBlueBg,
            isOn = false
        )
    )

    // "Quiet Hours" isn't one of the screens we were given, so it stays a plain nav
    // row here — same shape as every other Settings row — with no screen behind it yet.
    val quietHoursLabel = "Quiet Hours"
    val quietHoursSubtitle = "Pause notifications during a specific time"
}
