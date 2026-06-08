package com.example.campusconnect.feature.profile.data.fake

import com.example.campusconnect.feature.profile.model.Interest
object FakeInterestsService {

    fun getSelectedInterests(): List<Interest> {

        return listOf(
            Interest("1", "AI/ML", "Technology"),
            Interest("2", "Web Development", "Technology"),
            Interest("3", "UI/UX Design", "Design"),
            Interest("4", "Photography", "Creative")
        )
    }

    fun getAllInterests(): List<Interest> {

        return listOf(
            Interest("1", "AI/ML", "Technology"),
            Interest("2", "Web Development", "Technology"),
            Interest("3", "UI/UX Design", "Design"),
            Interest("4", "Photography", "Creative"),
            Interest("5", "Gaming", "Entertainment"),
            Interest("6", "Open Source", "Technology"),
            Interest("7", "Hackathons", "Technology"),
            Interest("8", "App Development", "Technology"),
            Interest("9", "Cyber Security", "Technology"),
            Interest("10", "Data Science", "Technology"),
            Interest("11", "Public Speaking", "Career"),
            Interest("12", "Content Creation", "Creative"),
            Interest("13", "Competitive Coding", "Technology"),
            Interest("14", "Football", "Sports"),
            Interest("15", "Basketball", "Sports"),
            Interest("16", "Music", "Creative"),
            Interest("17", "Dance", "Creative"),
            Interest("18", "Entrepreneurship", "Career"),
            Interest("19", "Startups", "Career"),
            Interest("20", "Graphic Design", "Design")
        )
    }
}