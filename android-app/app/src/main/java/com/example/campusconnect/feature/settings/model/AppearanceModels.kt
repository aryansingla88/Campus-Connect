package com.example.campusconnect.feature.settings.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.ui.graphics.vector.ImageVector

enum class ThemeOptionId { LIGHT, DARK, SYSTEM }

data class ThemeOption(
    val id: ThemeOptionId,
    val label: String,
    val subtitle: String,
    val icon: ImageVector
)

object AppearanceDefaults {

    fun options(): List<ThemeOption> = listOf(
        ThemeOption(
            id = ThemeOptionId.LIGHT,
            label = "Light Mode",
            subtitle = "A clean and bright experience",
            icon = Icons.Outlined.LightMode
        ),
        ThemeOption(
            id = ThemeOptionId.DARK,
            label = "Dark Mode",
            subtitle = "A comfortable experience in low light",
            icon = Icons.Outlined.DarkMode
        ),
        ThemeOption(
            id = ThemeOptionId.SYSTEM,
            label = "System Default",
            subtitle = "Automatically matches your device settings",
            icon = Icons.Outlined.PhoneAndroid
        )
    )
}
