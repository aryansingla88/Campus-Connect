package com.example.campusconnect.feature.map.data.fake

import com.example.campusconnect.feature.map.model.MapBadge
import com.example.campusconnect.feature.map.model.MapMedal
import com.example.campusconnect.feature.map.model.MapUserProfile

object FakeMapUserProfileService {

    private val users = listOf(
        MapUserProfile(
            userId = "user_1",
            fullName = "Aryan Sharma",
            username = "@aryan.sharma",
            course = "B.Tech CSE",
            batch = "2024-2028",
            description = "Tech enthusiast, problem solver and always up for new ideas.",
            badges = listOf(
                MapBadge("b1", "Tech", "⭐"),
                MapBadge("b2", "Code", "💻"),
                MapBadge("b3", "Eco", "🌱")
            ),
            medals = listOf(
                MapMedal("m1", "Gold", 1),
                MapMedal("m2", "Silver", 2),
                MapMedal("m3", "Bronze", 3)
            ),
            isFriend = false
        ),
        MapUserProfile(
            userId = "user_2",
            fullName = "Priya Singh",
            username = "@priya.singh",
            course = "MCA",
            batch = "2023-2025",
            description = "Creative developer who enjoys design, events and campus collaborations.",
            badges = listOf(
                MapBadge("b1", "Design", "🎨"),
                MapBadge("b2", "Events", "🎤"),
                MapBadge("b3", "Lead", "🏆")
            ),
            medals = listOf(
                MapMedal("m1", "Gold", 1),
                MapMedal("m2", "Silver", 2)
            ),
            isFriend = false
        )
    )

    fun getUserProfile(userId: String): MapUserProfile {
        return users.firstOrNull { it.userId == userId }
            ?: users.first()
    }
}