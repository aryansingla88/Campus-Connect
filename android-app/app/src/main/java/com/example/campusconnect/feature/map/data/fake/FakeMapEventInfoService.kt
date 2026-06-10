package com.example.campusconnect.feature.map.data.fake

import android.R as AndroidR
import com.example.campusconnect.feature.map.model.MapEventInfo

object FakeMapEventInfoService {

    fun getEventInfo(
        eventId: String,
        fallbackTitle: String
    ): MapEventInfo {
        return when (eventId) {

            "event_1" -> MapEventInfo(
                id = eventId,
                title = "Dance Competition",
                hostName = "Cultural Club",
                date = "12 Jun",
                time = "6:00 PM",
                description = "A college dance competition is a high-energy event where students showcase choreography, technique, and synchronization across genres like hip-hop, contemporary, and traditional folk.",
                posterResId = null
            )

            "event_2" -> MapEventInfo(
                id = eventId,
                title = "Hackathon",
                hostName = "Coding Club",
                date = "15 Jun",
                time = "10:00 AM",
                description = "A 24-hour coding event where students build creative tech solutions, collaborate in teams, and present their ideas.",
                posterResId = AndroidR.drawable.ic_menu_gallery // temporary test poster
            )

            else -> MapEventInfo(
                id = eventId,
                title = fallbackTitle,
                hostName = "Campus Team",
                date = "Coming Soon",
                time = "TBA",
                description = "$fallbackTitle event details will be loaded from backend later.",
                posterResId = null
            )
        }
    }
}