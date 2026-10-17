package com.example.campusconnect.feature.settings.model

enum class LanguageOptionId { ENGLISH, HINDI }

// `glyph` backs a SettingsGlyphChip (the language's own script) instead of a generic
// vector icon, matching the mock's "अ" chip for Hindi.
data class LanguageOption(
    val id: LanguageOptionId,
    val label: String,
    val subtitle: String,
    val glyph: String
)

object LanguageDefaults {

    fun options(): List<LanguageOption> = listOf(
        LanguageOption(
            id = LanguageOptionId.ENGLISH,
            label = "English",
            subtitle = "Default language",
            glyph = "A"
        ),
        LanguageOption(
            id = LanguageOptionId.HINDI,
            label = "हिंदी",
            subtitle = "Hindi",
            glyph = "अ"
        )
    )
}
