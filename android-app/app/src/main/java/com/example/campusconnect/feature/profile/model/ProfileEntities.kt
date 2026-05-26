package com.example.campusconnect.feature.profile.model

import androidx.compose.ui.graphics.Color


// --- Connection -----------------------------------------------------------------
enum class ConnectionStatus { ADD, PENDING, CONNECTED }

data class Connection(
    val userId: String,
    val initials: String,
    val name: String,
    val sub: String,
    val avatarBg: Color,
    val avatarText: Color,
    val status: ConnectionStatus
)

// -- Club -----------------------------------------------------------------
enum class ClubStatus { JOIN, PENDING, JOINED }

data class Club(
    val name: String,
    val members: String,
    val iconBg: Color,
    val iconTint: Color,
    val status: ClubStatus
)

// -- Honor -----------------------------------------------------------------
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
