package com.example.campusconnect.feature.profile.data

import androidx.compose.ui.graphics.Color
import com.example.campusconnect.model.*
import com.example.campusconnect.feature.profile.ui.shared.*

// Placeholder – replace with real network/repo calls when the API is ready.
object FakePublicProfileService {

    fun getProfile(userId: String) = com.example.campusconnect.model.PublicUserProfile(
        userId       = userId,
        displayName  = "Rahul Kumar",
        username     = "@rahul.kumar",
        bio          = "Tech enthusiast, problem solver and always up for new ideas.",
        initials     = "RK",
        course       = "Masters of Computer Application",
        year         = "2nd Year (2024 – 2028)",
        hostel       = "H4",
        hometown     = "Delhi",
        gender       = "Male",
        age          = 21,
        phone        = "+91 91234 56789",
        email        = "525110036@nitkkr.ac.in",
        memberSince  = "August 2024",
        github       = "github.com/rahul",
        linkedin     = "linkedin.com/in/rahul",
        instagram    = "@rahul.kumar",
        connectionCount = 18,
        honorRank    = 3,
        clubCount    = 2,
        interestCount = 3,
    )

    val connections: List<Connection> = listOf(
        Connection("AS", "Aryan Sharma", "MCA 2nd Year", Color(0xFFFFF3EB), OrangeDark, ConnectionStatus.CONNECTED),
        Connection("PS", "Priya Sharma", "MCA 3rd Year", Color(0xFFE6F1FB), Color(0xFF0C447C), ConnectionStatus.ADD),
    )

    val clubs: List<Club> = listOf(
        Club("Dev Club",         "142 members", Color(0xFFE6F1FB), Color(0xFF185FA5), ClubStatus.JOINED),
        Club("Innovators Hub",   "89 members",  Color(0xFFFEF0E6), OrangeDark,        ClubStatus.JOINED),
    )

    val badges: List<Honor> = listOf(
        Honor("Problem Solver",  "Coding Excellence", BadgeBlue),
        Honor("Top Contributor", "Campus Impact",     BadgeGreen),
    )

    val medals: List<Honor> = listOf(
        Honor("Silver Medal", "Outstanding Work", MedalSilver),
    )

    val interests: List<String> = listOf("AI/ML", "Web Development", "Gaming")
}