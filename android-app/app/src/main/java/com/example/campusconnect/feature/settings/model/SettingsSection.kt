package com.example.campusconnect.feature.settings.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.campusconnect.feature.settings.ui.components.*

// --- Row model -------------------------------------------------------------------------
data class SettingsItem(
    val id: SettingsItemId,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color,
    val isDestructive: Boolean = false
)

// --- Section model -----------------------------------------------------------------
data class SettingsSection(
    val title: String,
    val items: List<SettingsItem>
)

// --- Static content -----------------------------------------------------------------
// Purely presentational — nothing here is fetched from a repository. Rows that open a
// real screen or sheet (Profile Visibility, Location Visibility, Manage Social Links,
// Notifications, App Appearance, Language, Delete Account) are fully built; everything
// else is a route stub for later wiring (see SettingsDestinations).
object SettingsContent {

    fun dangerItem(): SettingsItem = SettingsItem(
        id = SettingsItemId.DELETE_ACCOUNT,
        title = "Delete Account",
        subtitle = "",
        icon = Icons.Filled.DeleteOutline,
        iconTint = DangerRed,
        iconBackground = DangerRedBg,
        isDestructive = true
    )

    fun sections(): List<SettingsSection> = listOf(

        SettingsSection(
            title = "Account",
            items = listOf(
                SettingsItem(
                    id = SettingsItemId.EDIT_PROFILE,
                    title = "Edit Profile",
                    subtitle = "Update your personal information",
                    icon = Icons.Outlined.Person,
                    iconTint = IconBlue,
                    iconBackground = IconBlueBg
                ),
                SettingsItem(
                    id = SettingsItemId.CHANGE_PASSWORD,
                    title = "Change Password",
                    subtitle = "Keep your account secure",
                    icon = Icons.Outlined.Lock,
                    iconTint = IconPurple,
                    iconBackground = IconPurpleBg
                ),
                SettingsItem(
                    id = SettingsItemId.PROFILE_VISIBILITY,
                    title = "Profile Visibility",
                    subtitle = "Control who can see your profile information",
                    icon = Icons.Outlined.Visibility,
                    iconTint = IconGreen,
                    iconBackground = IconGreenBg
                ),
                SettingsItem(
                    id = SettingsItemId.LOCATION_VISIBILITY,
                    title = "Location Visibility",
                    subtitle = "Choose whether to show your location",
                    icon = Icons.Outlined.LocationOn,
                    iconTint = IconOrange,
                    iconBackground = IconOrangeBg
                ),
                SettingsItem(
                    id = SettingsItemId.MANAGE_SOCIAL_LINKS,
                    title = "Manage Social Links",
                    subtitle = "Add or remove your social profiles",
                    icon = Icons.Outlined.Link,
                    iconTint = IconBlue,
                    iconBackground = IconBlueBg
                )
            )
        ),

        SettingsSection(
            title = "Preferences",
            items = listOf(
                SettingsItem(
                    id = SettingsItemId.NOTIFICATIONS,
                    title = "Notifications",
                    subtitle = "Manage your notification preferences",
                    icon = Icons.Outlined.NotificationsNone,
                    iconTint = IconRed,
                    iconBackground = IconRedBg
                ),
                SettingsItem(
                    id = SettingsItemId.APP_APPEARANCE,
                    title = "App Appearance",
                    subtitle = "Light, dark or system theme",
                    icon = Icons.Outlined.Palette,
                    iconTint = IconPurple,
                    iconBackground = IconPurpleBg
                ),
                SettingsItem(
                    id = SettingsItemId.LANGUAGE,
                    title = "Language",
                    subtitle = "Choose your preferred language",
                    icon = Icons.Outlined.Language,
                    iconTint = IconBlue,
                    iconBackground = IconBlueBg
                )
            )
        ),

        SettingsSection(
            title = "Data & Privacy",
            items = listOf(
                SettingsItem(
                    id = SettingsItemId.MANAGE_DATA,
                    title = "Manage Data",
                    subtitle = "View and manage your app data",
                    icon = Icons.Outlined.Storage,
                    iconTint = IconBlue,
                    iconBackground = IconBlueBg
                ),
                SettingsItem(
                    id = SettingsItemId.PRIVACY_SECURITY,
                    title = "Privacy & Security",
                    subtitle = "Learn how we protect your data",
                    icon = Icons.Outlined.Shield,
                    iconTint = IconGreen,
                    iconBackground = IconGreenBg
                )
            )
        ),

        SettingsSection(
            title = "Support",
            items = listOf(
                SettingsItem(
                    id = SettingsItemId.HELP_FAQ,
                    title = "Help & FAQ",
                    subtitle = "Find answers to common questions",
                    icon = Icons.Outlined.HelpOutline,
                    iconTint = IconOrange,
                    iconBackground = IconOrangeBg
                ),
                SettingsItem(
                    id = SettingsItemId.CONTACT_US,
                    title = "Contact Us",
                    subtitle = "Get in touch with the team",
                    icon = Icons.Outlined.MailOutline,
                    iconTint = IconBlue,
                    iconBackground = IconBlueBg
                ),
                SettingsItem(
                    id = SettingsItemId.TERMS_POLICIES,
                    title = "Terms & Policies",
                    subtitle = "Read our terms, privacy policy and guidelines",
                    icon = Icons.Outlined.Description,
                    iconTint = IconPurple,
                    iconBackground = IconPurpleBg
                )
            )
        ),

        SettingsSection(
            title = "About",
            items = listOf(
                SettingsItem(
                    id = SettingsItemId.ABOUT_APP,
                    title = "About Campus Connect",
                    subtitle = "App version, acknowledgements and more",
                    icon = Icons.Outlined.Info,
                    iconTint = IconBlue,
                    iconBackground = IconBlueBg
                )
            )
        )
    )
}
