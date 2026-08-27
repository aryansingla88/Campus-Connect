package com.example.campusconnect.feature.events.data.fake

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.campusconnect.feature.events.model.Event
import com.example.campusconnect.feature.events.model.EventStatus

class FakeEventService {

    private val events = mutableListOf<Event>()

    init {
        events.addAll(listOf(

            // ── LIVE ─────────────────────────────────────────────────────────
            Event(
                id = 1,
                title = "Tech Fest 2025",
                description = "Annual technology festival with competitions, workshops and exhibitions.",
                latitude = 0.0,
                longitude = 0.0,
                xRatio = 0.3f,
                yRatio = 0.35f,
                date = "20 May 2025",
                startTime = "10:00 AM",
                endTime = "04:00 PM",
                createdBy = 1,
                clubName = "CS Club",
                isPoster = true,
                posterUrl = "https://i.ibb.co/LdQvj8wc/Screenshot-2026-08-27-233817.png",
                category = "Technology",
                visibilityType = "Public",
                visibilityValue = "All",
                registrationRequired = true,
                inAppRegistration = true,
                venue = "Main Auditorium",
                enableChat = true,
                status = EventStatus.LIVE
            ),
            Event(
                id = 2,
                title = "Music Night Live",
                description = "An evening of live performances by campus bands and solo artists.",
                latitude = 0.0,
                longitude = 0.0,
                xRatio = 0.65f,
                yRatio = 0.45f,
                date = "22 May 2025",
                startTime = "07:00 PM",
                endTime = "10:00 PM",
                createdBy = 1,
                clubName = "Beats Club",
                isPoster = true,
                posterUrl = "https://i.ibb.co/dhqWJxV/a5d40ead-a330-483c-a259-76a56a33d6b8.png",
                category = "Cultural",
                visibilityType = "Public",
                visibilityValue = "All",
                registrationRequired = false,
                venue = "Open Air Stage",
                enableChat = false,
                status = EventStatus.UPCOMING
            ),

            // ── PAST ─────────────────────────────────────────────────────────
            Event(
                id = 3,
                title = "Hackathon 2024",
                description = "24-hour coding competition for real-world problem solving.",
                latitude = 0.0,
                longitude = 0.0,
                xRatio = 0.5f,
                yRatio = 0.6f,
                date = "10 Jan 2025",
                startTime = "09:00 AM",
                endTime = "09:00 AM",
                createdBy = 1,
                clubName = "CS Club",
                isPoster = false,
                category = "Technology",
                visibilityType = "Public",
                visibilityValue = "All",
                registrationRequired = true,
                inAppRegistration = true,
                venue = "Lab Block A",
                enableChat = true,
                status = EventStatus.LIVE
            ),
            Event(
                id = 4,
                title = "Art Exhibition",
                description = "Showcase of student artwork — painting, sculpture, digital art.",
                latitude = 0.0,
                longitude = 0.0,
                xRatio = 0.2f,
                yRatio = 0.7f,
                date = "15 Feb 2025",
                startTime = "11:00 AM",
                endTime = "05:00 PM",
                createdBy = 1,
                clubName = "Fine Arts Society",
                isPoster = false,
                category = "Cultural",
                visibilityType = "Public",
                visibilityValue = "All",
                registrationRequired = false,
                venue = "Gallery Block B",
                enableChat = false,
                status = EventStatus.PAST
            ),
            Event(
                id = 5,
                title = "Debate Championship",
                description = "Inter-college debate on technology, society and the future.",
                latitude = 0.0,
                longitude = 0.0,
                xRatio = 0.75f,
                yRatio = 0.25f,
                date = "05 Mar 2025",
                startTime = "10:00 AM",
                endTime = "03:00 PM",
                createdBy = 1,
                clubName = "Literary Club",
                isPoster = false,
                category = "Academic",
                visibilityType = "Public",
                visibilityValue = "All",
                registrationRequired = true,
                inAppRegistration = false,
                registrationLink = "https://forms.example.com/debate",
                venue = "Seminar Hall B",
                enableChat = false,
                status = EventStatus.PAST
            )
        ))
    }

    fun createEvent(event: Event): Boolean {
        events.add(event.copy(id = (events.maxOfOrNull { it.id } ?: 0) + 1))
        return true
    }

    fun updateEvent(event: Event): Boolean {
        val index = events.indexOfFirst { it.id == event.id }
        if (index == -1) return false
        events[index] = event
        return true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun deleteEvent(id: Int): Boolean = events.removeIf { it.id == id }

    fun getEvents(): List<Event>     = events.toList()
    fun getLiveEvents(): List<Event> = events.filter { it.status == EventStatus.LIVE }
    fun getPastEvents(): List<Event> = events.filter { it.status == EventStatus.PAST }
}