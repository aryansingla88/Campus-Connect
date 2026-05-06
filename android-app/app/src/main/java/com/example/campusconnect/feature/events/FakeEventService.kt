package com.example.campusconnect.feature.events

import com.example.campusconnect.model.Event

class FakeEventService {

    private val events = mutableListOf<Event>()

    fun createEvent(event: Event): Boolean {
        events.add(event.copy(id = events.size + 1))
        println("Fake API: Event added → $event")
        return true
    }

    fun getEvents(): List<Event> {
        return events.toList()
    }
}