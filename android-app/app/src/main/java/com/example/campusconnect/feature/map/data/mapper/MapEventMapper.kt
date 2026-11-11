package com.example.campusconnect.feature.map.data.mapper

import com.example.campusconnect.feature.map.data.remote.response.EventCategoryResponse
import com.example.campusconnect.feature.map.data.remote.response.EventHostResponse
import com.example.campusconnect.feature.map.data.remote.response.EventMarkerResponse
import com.example.campusconnect.feature.map.data.remote.response.EventPreviewResponse
import com.example.campusconnect.feature.map.model.EventFilter
import com.example.campusconnect.feature.map.model.EventMarkerData
import com.example.campusconnect.feature.map.model.HostInfo
import com.example.campusconnect.feature.map.model.MapEventInfo

object MapEventMapper {
    /** Returns null when an event has no usable geographic location. */
    fun toMarker(response: EventMarkerResponse): EventMarkerData? {
        val latitude = response.latitude ?: return null
        val longitude = response.longitude ?: return null
        if (!latitude.isFinite() || latitude !in -90.0..90.0) return null
        if (!longitude.isFinite() || longitude !in -180.0..180.0) return null

        return EventMarkerData(
            sourceId = response.id,
            latitude = latitude,
            longitude = longitude,
            title = response.title,
            priority = response.priority ?: 0,
            startTime = response.startTime
        )
    }

    fun toInfo(response: EventPreviewResponse): MapEventInfo {
        val hosts = response.hosts.orEmpty().map(::toHostInfo)
        return MapEventInfo(
            id = response.id,
            title = response.title,
            date = response.startTime.orEmpty(),
            time = response.startTime.orEmpty(),
            description = response.description.orEmpty(),
            venue = response.venue,
            posterUrl = response.posterUrl,
            posterResId = null,
            hosts = hosts,
            endTime = response.endTime,
            subtitle = response.subtitle,
            registrationCount = response.registrationCount,
            registrationStatus = response.registrationStatus,
            categories = response.categories.orEmpty().map { EventFilter(it.id, it.name) },
            registrationType = response.registrationType,
            registrationLink = response.registrationLink,
            isReminderEnabled = response.isReminderEnabled,
            priority = response.priority
        )
    }

    fun toFilter(response: EventCategoryResponse): EventFilter = EventFilter(response.id, response.name)

    private fun toHostInfo(response: EventHostResponse): HostInfo = HostInfo(
        id = response.userId,
        name = response.fullName,
        avatarUrl = response.avatarUrl,
        role = response.role
    )
}
