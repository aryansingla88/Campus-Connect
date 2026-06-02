package com.example.campusconnect.feature.profile.data.fake

import androidx.compose.ui.graphics.Color
import com.example.campusconnect.feature.profile.model.Honor
import com.example.campusconnect.feature.profile.model.HonorEntry
import com.example.campusconnect.feature.profile.ui.components.*

object FakeHonorService {

    fun getHonorEntries(): List<HonorEntry> {

        return listOf(
            HonorEntry(1, "PS", "Priya Sharma", 340, Color(0xFFFEF0E6)),
            HonorEntry(2, "AS", "Aryan Sharma", 280, Color(0xFFFFE8D6), isMe = true),
            HonorEntry(3, "RK", "Rahul Kumar", 240, Color(0xFFE1F5EE)),
            HonorEntry(4, "AV", "Amit Verma", 190, Color(0xFFEEEDFE)),
        )
    }

    fun getBadges(): List<Honor> {

        return listOf(
            Honor("Problem Solver", "Coding Excellence", BadgeBlue),
            Honor("Community Helper", "Helping Students", BadgePurple),
            Honor("Top Contributor", "Campus Impact", BadgeGreen),
        )
    }

    fun getMedals(): List<Honor> {

        return listOf(
            Honor("Gold Medal", "Top Performer", MedalGold),
            Honor("Silver Medal", "Outstanding Work", MedalSilver),
        )
    }
}