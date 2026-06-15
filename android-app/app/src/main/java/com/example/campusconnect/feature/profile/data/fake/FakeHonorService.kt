package com.example.campusconnect.feature.profile.data.fake

import com.example.campusconnect.feature.profile.model.HonorType
import com.example.campusconnect.feature.profile.model.ProfileHonor
import com.example.campusconnect.feature.profile.model.ProfileHonors

object FakeHonorService {

    fun getProfileHonors(): ProfileHonors {

        return ProfileHonors(

            honorRank = 12,

            badges = listOf(

                ProfileHonor(
                    honorId = "problem_solver",
                    type = HonorType.BADGE,
                    title = "Problem Solver",
                    subtitle = "Coding Excellence",
                    iconUrl = null,
                    priority = 1
                ),

                ProfileHonor(
                    honorId = "community_helper",
                    type = HonorType.BADGE,
                    title = "Community Helper",
                    subtitle = "Helping Students",
                    iconUrl = null,
                    priority = 2
                ),

                ProfileHonor(
                    honorId = "top_contributor",
                    type = HonorType.BADGE,
                    title = "Top Contributor",
                    subtitle = "Campus Impact",
                    iconUrl = null,
                    priority = 3
                )
            ),

            medals = listOf(

                ProfileHonor(
                    honorId = "gold_medal",
                    type = HonorType.MEDAL,
                    title = "Gold Medal",
                    subtitle = "Top Performer",
                    iconUrl = null,
                    priority = 1
                ),

                ProfileHonor(
                    honorId = "silver_medal",
                    type = HonorType.MEDAL,
                    title = "Silver Medal",
                    subtitle = "Outstanding Work",
                    iconUrl = null,
                    priority = 2
                )
            )
        )
    }
}