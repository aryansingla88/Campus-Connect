package com.example.campusconnect.feature.settings.model

import androidx.compose.ui.graphics.Color

// --- One editable social link row --------------------------------------------------
// `glyph`/`iconTint`/`iconBackground` back a SettingsGlyphChip instead of a vector icon —
// material-icons-extended doesn't ship brand marks, so each platform gets a short
// initialism on a brand-tinted chip instead (avoids reproducing a trademarked logo).
data class SocialLinkEntry(
    val id: Int,
    val platformLabel: String,
    val glyph: String,
    val iconTint: Color,
    val iconBackground: Color,
    val url: String,
    val placeholder: String
)

object SocialLinkDefaults {

    private val GitHubTint = Color(0xFF24292F)
    private val GitHubBg   = Color(0xFFEDEEF0)
    private val LinkedInTint = Color(0xFF0A66C2)
    private val LinkedInBg   = Color(0xFFE3F0FC)
    private val InstagramTint = Color(0xFFC13584)
    private val InstagramBg   = Color(0xFFFCE7F3)

    fun defaultLinks(): List<SocialLinkEntry> = listOf(
        SocialLinkEntry(
            id = 1,
            platformLabel = "GitHub",
            glyph = "GH",
            iconTint = GitHubTint,
            iconBackground = GitHubBg,
            url = "https://github.com/aryan",
            placeholder = "https://github.com/username"
        ),
        SocialLinkEntry(
            id = 2,
            platformLabel = "LinkedIn",
            glyph = "in",
            iconTint = Color.White,
            iconBackground = LinkedInTint,
            url = "https://www.linkedin.com/in/aryan",
            placeholder = "https://linkedin.com/in/username"
        ),
        SocialLinkEntry(
            id = 3,
            platformLabel = "Instagram",
            glyph = "IG",
            iconTint = InstagramTint,
            iconBackground = InstagramBg,
            url = "https://www.instagram.com/aryan",
            placeholder = "https://instagram.com/username"
        )
    )

    // Used when the user taps "+ Add More" — a blank row they can fill in themselves.
    fun blankLink(id: Int): SocialLinkEntry = SocialLinkEntry(
        id = id,
        platformLabel = "New Link",
        glyph = "?",
        iconTint = Color(0xFF6B7280),
        iconBackground = Color(0xFFF1F2F4),
        url = "",
        placeholder = "https://example.com/username"
    )
}
