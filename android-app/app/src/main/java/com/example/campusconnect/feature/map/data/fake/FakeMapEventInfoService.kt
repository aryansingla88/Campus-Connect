package com.example.campusconnect.feature.map.data.fake

import com.example.campusconnect.feature.map.model.HostInfo
import com.example.campusconnect.feature.map.model.MapEventInfo

object FakeMapEventInfoService {

    fun getEventInfo(
        eventId: String,
        fallbackTitle: String
    ): MapEventInfo {
        return when (eventId) {

            // Event 1: Poster Card Variant
            "event_1", "EVENT_1", "1" -> MapEventInfo(
                id = eventId,
                title = "Dance Competition",
                hostName = "Cultural Club",
                date = "12 Jun",
                time = "6:00 PM",
                description = "A college dance competition is a high-energy event where students showcase choreography.",
                venue = "Student Center Ballrooms",
              //  posterUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800",
                posterUrl =null,
                posterResId = com.example.campusconnect.R.drawable.test_poster,
               // posterUrl = "https://picsum.photos/600/800",
                // Only Event 1 gets poster
                isJoined = true,
                hosts = listOf(
                    HostInfo(1, "Alex"),
                    HostInfo(2, "Maria"),
                    HostInfo(3, "Chen"),
                    HostInfo(4, "Fatima")
                )
            )

            // Event 2: Normal Description Card Variant
            "event_2", "EVENT_2", "2" -> MapEventInfo(
                id = eventId,
                title = "Hackathon",
                hostName = "Coding Club",
                date = "15 Jun",
                time = "10:00 AM",
                description = "A 24-hour coding event where students build creative tech solutions, collaborate in teams, and present their ideas.",
                venue = "Main Auditorium",
                posterUrl = null,
                posterResId = null, // No poster = Normal Description Card
                isJoined = true,
                hosts = listOf(
                    HostInfo(1, "Rahul"),
                    HostInfo(2, "Priya"),
                    HostInfo(3, "Amit"),
                    HostInfo(4, "Neha"),
                    HostInfo(5, "Karan"),
                    HostInfo(6, "Sanya"),
                    HostInfo(7, "Rahul"),
                    HostInfo(8, "Priya"),
                    HostInfo(9, "Amit"),
                    HostInfo(10, "Neha"),
                    HostInfo(11, "Karan"),
                    HostInfo(12, "Sanya")
                )
            )

            // Default Fallback: Normal Description Card Variant
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
                isJoined = false, // Fixed: Added missing parameter here
                hosts = emptyList()
            )
        }
    }
}