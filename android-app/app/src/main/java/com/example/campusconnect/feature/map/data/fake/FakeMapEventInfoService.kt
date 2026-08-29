package com.example.campusconnect.feature.map.data.fake

import com.example.campusconnect.feature.map.model.HostInfo
import com.example.campusconnect.feature.map.model.MapEventInfo

object FakeMapEventInfoService {

    fun getEventInfo(
        eventId: Int,
        fallbackTitle: String = "Campus Event"
    ): MapEventInfo {
        return when (eventId) {

            // Event 1
            1 -> MapEventInfo(
                id = eventId,
                title = "Dance Competition",
                hostName = "Cultural Club",
                date = "12 Jun",
                time = "6:00 PM",
                description = "A college dance competition is a high-energy event where students showcase choreography.",
                venue = "Student Center Ballrooms",
                posterUrl = null,
                posterResId = com.example.campusconnect.R.drawable.test_poster,
                isJoined = true,
                hosts = listOf(
                    HostInfo(1, "Alex"),
                    HostInfo(2, "Maria"),
                    HostInfo(3, "Chen"),
                    HostInfo(4, "Fatima")
                )
            )

            // Event 2
            2 -> MapEventInfo(
                id = eventId,
                title = "Hackathon",
                hostName = "Coding Club",
                date = "15 Jun",
                time = "10:00 AM",
                description = "A 24-hour coding event where students build creative tech solutions, collaborate in teams, and present their ideas.",
                venue = "Main Auditorium",
                posterUrl = null,
                posterResId = null,
                isJoined = true,
                hosts = listOf(
                    HostInfo(1, "Rahul"),
                    HostInfo(2, "Priya"),
                    HostInfo(3, "Amit"),
                    HostInfo(4, "Neha"),
                    HostInfo(5, "Karan"),
                    HostInfo(6, "Sanya")
                )
            )

            // Default Fallback
            else -> MapEventInfo(
                id = eventId,
                title = fallbackTitle,
                hostName = "Campus Team",
                date = "Coming Soon",
                time = "TBA",
                description = "$fallbackTitle event details will be loaded from backend later.",
                venue = "Campus Complex",
                posterUrl = null,
                posterResId = null,
                isJoined = false,
                hosts = emptyList()
            )
        }
    }
}