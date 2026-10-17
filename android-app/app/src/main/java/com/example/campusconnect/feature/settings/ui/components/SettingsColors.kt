package com.example.campusconnect.feature.settings.ui.components

import androidx.compose.ui.graphics.Color

// -----------------------------------------------------------------------------------
// NOTE ON REUSE: TextPrimary, TextMuted, CardBg, PageBg, DividerColor and Orange/
// OrangeLight/OrangeDark already exist in ProfileColors.kt
// (com.example.campusconnect.feature.profile.ui.components). Rather than redefine
// the same neutrals twice, files in this module import them directly from there —
// see the wildcard import at the top of files that use them. Only tokens that are
// new to Settings, or that need the exact spec hex below, live in this file.
// -----------------------------------------------------------------------------------

// --- Primary action color ---------------------------------------------------------
// Spec calls out #FF7A1A specifically for Settings, which is close to but not
// identical to Profile's Orange (#F97316) — kept as its own token so Save/Done/
// selection states match the spec exactly rather than silently drifting.
val PrimaryOrange      = Color(0xFFFF7A1A)
val PrimaryOrangeLight = Color(0xFFFFF1E7)
val PrimaryOrangeDark  = Color(0xFFE8690E)

// --- Icon chip accent colors (background + tint pairs) -----------------------------
val IconBlue      = Color(0xFF3B82F6)
val IconBlueBg    = Color(0xFFE7F0FE)

val IconPurple    = Color(0xFF9B59F5)
val IconPurpleBg  = Color(0xFFF1E9FE)

val IconGreen     = Color(0xFF22C55E)
val IconGreenBg   = Color(0xFFE4F9EC)

val IconOrange    = Color(0xFFF97316)
val IconOrangeBg  = Color(0xFFFFF0E4)

val IconRed       = Color(0xFFEF4444)
val IconRedBg     = Color(0xFFFDE9E9)

val IconTeal      = Color(0xFF14B8A6)
val IconTealBg    = Color(0xFFE1F7F4)

// --- Danger / destructive -----------------------------------------------------------
val DangerRed      = Color(0xFFE5484D)
val DangerRedBg    = Color(0xFFFCE8E8)
val DangerRowBg    = Color(0xFFFDF1F1)
val DangerBorder   = Color(0xFFF6D2D2)

// --- Info banner (Profile Visibility intro card) -------------------------------------
val InfoBannerBg     = Color(0xFFEFF6FF)
val InfoBannerBorder = Color(0xFFDCEAFE)
val InfoBannerIcon   = Color(0xFF3B82F6)
val InfoBannerText   = Color(0xFF3D5A80)

// --- Always-visible row (static, non-toggleable) --------------------------------------
val AlwaysVisibleIcon = Color(0xFF9AA3B2)
val AlwaysVisibleTag  = Color(0xFFAAB2C0)
