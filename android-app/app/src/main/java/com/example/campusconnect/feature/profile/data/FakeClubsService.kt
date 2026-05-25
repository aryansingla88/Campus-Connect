package com.example.campusconnect.feature.profile.data

import androidx.compose.ui.graphics.Color
import com.example.campusconnect.feature.profile.model.Club
import com.example.campusconnect.feature.profile.model.ClubStatus
import com.example.campusconnect.feature.profile.ui.components.OrangeDark

object FakeClubsService {

    fun getClubs(): List<Club> {

        return listOf(

            Club(
                name = "Dev Club",
                members = "142 members",
                iconBg = Color(0xFFE6F1FB),
                iconTint = Color(0xFF185FA5),
                status = ClubStatus.JOINED
            ),

            Club(
                name = "Innovators Hub",
                members = "89 members",
                iconBg = Color(0xFFFEF0E6),
                iconTint = OrangeDark,
                status = ClubStatus.PENDING
            ),

            Club(
                name = "Photography Club",
                members = "56 members",
                iconBg = Color(0xFFEEEDFE),
                iconTint = Color(0xFF3C3489),
                status = ClubStatus.JOINED
            )
        )
    }
}