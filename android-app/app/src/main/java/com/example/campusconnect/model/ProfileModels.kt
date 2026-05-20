package com.example.campusconnect.model

import androidx.compose.ui.graphics.Color

// ── Panel enum ────────────────────────────────────────────────────────────────
enum class StatPanel { NONE, CONNECTIONS, HONOR, CLUBS, INTERESTS }

// ── Connection ────────────────────────────────────────────────────────────────
enum class ConnectionStatus { ADD, PENDING, CONNECTED }

data class Connection(
    val initials: String,
    val name: String,
    val sub: String,
    val avatarBg: Color,
    val avatarText: Color,
    val status: ConnectionStatus
)

// ── Club ──────────────────────────────────────────────────────────────────────
enum class ClubStatus { JOIN, PENDING, JOINED }

data class Club(
    val name: String,
    val members: String,
    val iconBg: Color,
    val iconTint: Color,
    val status: ClubStatus
)

// ── Honor ─────────────────────────────────────────────────────────────────────
data class HonorEntry(
    val rank: Int,
    val initials: String,
    val name: String,
    val points: Int,
    val avatarBg: Color,
    val isMe: Boolean = false
)

data class Honor(
    val title: String,
    val subtitle: String,
    val color: Color
)
