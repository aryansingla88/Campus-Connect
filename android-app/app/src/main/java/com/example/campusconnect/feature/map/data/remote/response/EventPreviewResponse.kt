package com.example.campusconnect.feature.map.data.remote.response

import com.example.campusconnect.feature.map.model.HostInfo
import com.example.campusconnect.feature.map.model.MapEventInfo
import com.google.gson.annotations.SerializedName

data class EventPreviewRes(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("posterUrl")
    val posterUrl: String? = null,

    @SerializedName("startTime")
    val startTime: String? = null,

    @SerializedName("endTime")
    val endTime: String? = null,

    @SerializedName("venue")
    val venue: String? = null,

    @SerializedName("latitude")
    val latitude: Double? = null,

    @SerializedName("longitude")
    val longitude: Double? = null,

    @SerializedName("registrationType")
    val registrationType: String? = null,

    @SerializedName("registrationLink")
    val registrationLink: String? = null,

    @SerializedName("isJoined")
    val isJoined: Boolean? = null,

    @SerializedName("isReminderEnabled")
    val isReminderEnabled: Boolean? = null,

    @SerializedName("priority")
    val priority: Int? = null,

    @SerializedName("hosts")
    val hosts: List<EventHostResponse>? = null
)

fun EventPreviewRes.toMapEventInfo(): MapEventInfo {
    val hostList = hosts?.map {
        HostInfo(
            id = it.userId, // No mismatch now (Int -> Int)
            name = it.fullName,
            avatarUrl = it.avatarUrl
        )
    } ?: emptyList()

    return MapEventInfo(
        id = id,
        title = title,
        hostName = hostList.firstOrNull()?.name ?: "Campus Team",
        date = startTime ?: "TBA",
        time = if (startTime != null && endTime != null) "$startTime - $endTime" else "TBA",
        description = description ?: "",
        venue = venue ?: "Campus Complex",
        posterUrl = posterUrl,
        posterResId = null,
        isJoined = isJoined ?: false,
        hosts = hostList
    )
}