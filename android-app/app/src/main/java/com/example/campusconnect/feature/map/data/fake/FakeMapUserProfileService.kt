package com.example.campusconnect.feature.map.data.fake

import com.example.campusconnect.feature.map.model.MapUserProfile

class FakeMapUserProfileService {

    fun getProfileByMarkerId(markerId: Int): MapUserProfile {
        return when (markerId) {
            1 -> MapUserProfile(
                id = 1,
                fullName = "Aryan Sharma",
                course = "MCA",
                startYear = 2023,
                endYear = 2025,
                description = "Tech enthusiast, problem solver and always motivated and up for new ideas. Love to play sports, make friends, and focus deeply on DSA and projects.",
                badges = listOf("⭐", "💻", "🌱"),
                medals = listOf(1, 2, 3),
                mutualFriendsCount = 4
            )

            2 -> MapUserProfile(
                id = 2,
                fullName = "Priya Singh",
                course = "MCA",
                startYear = 2023,
                endYear = 2025,
                description = "Creative developer who enjoys design, collaboration, and building useful projects with clean UI and practical thinking.",
                badges = listOf("🎨", "🎤", "🏆"),
                medals = listOf(1, 2),
                mutualFriendsCount = 3
            )

            else -> MapUserProfile(
                id = markerId,
                fullName = "Campus User",
                course = "MCA",
                startYear = 2023,
                endYear = 2025,
                description = "Curious learner and active campus member interested in technology and collaboration.",
                badges = listOf("⭐", "💻"),
                medals = listOf(1),
                mutualFriendsCount = 2
            )
        }
    }
}