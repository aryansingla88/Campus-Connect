package com.example.campusconnect.feature.events

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.campusconnect.model.Event

class FakeEventService {

    private val events = mutableListOf<Event>()

    init {
        events.addAll(
            listOf(
                Event(
                    id = 1,
                    title = "Event A",
                    description = "Dummy",
                    latitude = 0.0,
                    longitude = 0.0,
                    xRatio = 0.3f,
                    yRatio = 0.4f
                ),
                Event(
                    id = 2,
                    title = "Event B",
                    description = "Dummy",
                    latitude = 0.0,
                    longitude = 0.0,
                    xRatio = 0.6f,
                    yRatio = 0.5f
                ),
                Event(
                    id = 3,
                    title = "Event C",
                    description = "Dummy",
                    latitude = 0.0,
                    longitude = 0.0,
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

    /** Replace the event with the same id. Returns true if found and updated. */
    fun updateEvent(event: Event): Boolean {
        val index = events.indexOfFirst { it.id == event.id }
        if (index == -1) return false
        events[index] = event
        return true
    }

    /** Remove event by id. Returns true if found and removed. */
    @RequiresApi(Build.VERSION_CODES.N)
    fun deleteEvent(id: Int): Boolean {
        return events.removeIf { it.id == id }
    }

    fun getEvents(): List<Event> = events.toList()
}