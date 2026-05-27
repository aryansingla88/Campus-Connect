package com.example.campusconnect.feature.profile.data

import androidx.compose.ui.graphics.Color
import com.example.campusconnect.model.*
import com.example.campusconnect.feature.profile.ui.shared.*

object FakeProfileService {

    val connections: List<Connection> = listOf(
        Connection("RK", "Rahul Kumar",  "MCA 2nd Year", Color(0xFFFEF0E6), OrangeDark,   ConnectionStatus.CONNECTED),
        Connection("PS", "Priya Sharma", "MCA 3rd Year", Color(0xFFE6F1FB), Color(0xFF0C447C), ConnectionStatus.CONNECTED),
        Connection("AV", "Amit Verma",   "MCA 2nd Year", Color(0xFFE1F5EE), Color(0xFF085041), ConnectionStatus.PENDING),
        Connection("SK", "Sneha Kaur",   "MCA 1st Year", Color(0xFFEEEDFE), Color(0xFF3C3489), ConnectionStatus.CONNECTED),
    )

    val clubs: List<Club> = listOf(
        Club("Dev Club",          "142 members", Color(0xFFE6F1FB), Color(0xFF185FA5), ClubStatus.JOINED),
        Club("Innovators Hub",    "89 members",  Color(0xFFFEF0E6), OrangeDark,        ClubStatus.PENDING),
        Club("Photography Club",  "56 members",  Color(0xFFEEEDFE), Color(0xFF3C3489), ClubStatus.JOINED),
        Club("Data Science Club", "98 members",  Color(0xFFE1F5EE), Color(0xFF085041), ClubStatus.JOIN),
    )

    val honorEntries: List<HonorEntry> = listOf(
        HonorEntry(1, "PS", "Priya Sharma", 340, Color(0xFFFEF0E6)),
        HonorEntry(2, "AS", "Aryan Sharma", 280, Color(0xFFFFE8D6), isMe = true),
        HonorEntry(3, "RK", "Rahul Kumar",  240, Color(0xFFE1F5EE)),
        HonorEntry(4, "AV", "Amit Verma",   190, Color(0xFFEEEDFE)),
    )

    val badges: List<Honor> = listOf(
        Honor("Problem Solver",    "Coding Excellence", BadgeBlue),
        Honor("Community Helper",  "Helping Students",  BadgePurple),
        Honor("Problem Solver",    "Coding Excellence", BadgeBlue),
        Honor("Top Contributor",   "Campus Impact",     BadgeGreen),
    )

    val medals: List<Honor> = listOf(
        Honor("Gold Medal",   "Top Performer",   MedalGold),
        Honor("Silver Medal", "Outstanding Work", MedalSilver),
    )

    val interests: List<String> = listOf(
        "AI/ML", "Web Development", "UI/UX Design", "Photography"
    )
}