package com.example.campusconnect.feature.events.model

enum class MedalType(
    val label: String,
    val subtitle: String,
    val rank: Int
) {
    GOLD(
        label = "Gold Medal",
        subtitle = "Top Performer",
        rank = 1
    ),
    SILVER(
        label = "Silver Medal",
        subtitle = "Outstanding Work",
        rank = 2
    ),
    BRONZE(
        label = "Bronze Medal",
        subtitle = "Excellent Effort",
        rank = 3
    )
}

data class MedalAward(
    val eventId: Int,
    val medalType: MedalType,
    val registrationId: Int,
    val recipientName: String,
    val honorId: Int? = null,
    val isTeam: Boolean = false
)