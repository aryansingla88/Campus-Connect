package com.example.campusconnect.feature.events

import com.example.campusconnect.model.Event

class FakeEventService {

    private val events = mutableListOf<Event>()

    init {
        // 👇 Default markers
        events.addAll(
            listOf(
                Event(
                    id = 1,
                    title = "Event A",
                    description = "Dummy",
                    latitude = 0.0,
                    longitude = 0.0,
                    eventTime = "",
                    createdBy = 1,
                    xRatio = 0.3f,
                    yRatio = 0.4f
                ),
                Event(
                    id = 2,
                    title = "Event B",
                    description = "Dummy",
                    latitude = 0.0,
                    longitude = 0.0,
                    eventTime = "",
                    createdBy = 1,
                    xRatio = 0.6f,
                    yRatio = 0.5f
                ),
                Event(
                    id = 3,
                    title = "Event C",
                    description = "Dummy",
                    latitude = 0.0,
                    longitude = 0.0,
                    eventTime = "",
                    createdBy = 1,
                    xRatio = 0.5f,
                    yRatio = 0.7f
                )
            )
        )
    }

    fun createEvent(event: Event): Boolean {
        events.add(event.copy(id = events.size + 1))
        return true
    }

    fun getEvents(): List<Event> {
        return events
    }
}