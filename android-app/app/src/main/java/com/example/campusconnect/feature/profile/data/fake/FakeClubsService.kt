package com.example.campusconnect.feature.profile.data.fake

import androidx.compose.ui.graphics.Color
import com.example.campusconnect.feature.profile.model.Club
import com.example.campusconnect.feature.profile.model.ClubStatus
import com.example.campusconnect.feature.profile.ui.components.OrangeDark

object FakeClubsService {

    fun getClubs(): List<Club> {

        return listOf(

            Club(
                clubId = "dev_club",
                name = "Dev Club",
                logoUrl = null,
                memberCount = 142,
                status = ClubStatus.JOINED
            ),

            Club(
                clubId = "innovators_hub",
                name = "Innovators Hub",
                logoUrl = null,
                memberCount = 89,
                status = ClubStatus.PENDING
            ),

            Club(
                clubId = "photography_club",
                name = "Photography Club",
                logoUrl = null,
                memberCount = 56,
                status = ClubStatus.JOINED
            )
        )
    }
}