package com.example.campusconnect.feature.map.data.remote.response

import com.example.campusconnect.feature.map.model.HostInfo
import com.example.campusconnect.feature.map.model.MapEventInfo
import com.google.gson.annotations.SerializedName

data class EventPreviewResponse(
    val id: Int,
    val title: String,
    val description: String? = null,
    val posterUrl: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val venue: String? = null,
    val registrationType: String? = null,
    val registrationLink: String? = null,
    val isJoined: Boolean? = null,
    val isReminderEnabled: Boolean? = null,
    val priority: Int? = null,
    val hosts: List<EventHostResponse>? = null
)

fun EventPreviewResponse.toMapEventInfo(): MapEventInfo {
    val hostList = hosts?.map { host ->
        HostInfo(
            id = host.userId,
            name = host.fullName,
            avatarUrl = host.avatarUrl
        )
    } ?: emptyList()

    return MapEventInfo(
        id = id,
        title = title,
        hostName = hostList.firstOrNull()?.name ?: "Campus Team",
        date = startTime ?: "TBA",
        time = if (startTime != null && endTime != null) {
            "$startTime - $endTime"
        } else {
            "TBA"
        },
        description = description ?: "",
        venue = venue ?: "Campus Complex",
        posterUrl = posterUrl,
        posterResId = null,
        hosts = hostList,
        isJoined = isJoined ?: false
    )
}

