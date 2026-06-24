package com.example.campusconnect.feature.map.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.map.data.remote.MapApi
import com.example.campusconnect.feature.map.data.remote.request.EventRegReq
import com.example.campusconnect.feature.map.data.remote.response.EventMapRes
import com.example.campusconnect.feature.map.data.remote.response.MarkerRes
import com.example.campusconnect.feature.map.data.remote.response.PoiRes
import com.example.campusconnect.feature.map.data.remote.response.ShopRes
import com.example.campusconnect.feature.map.data.remote.response.UserMapRes
import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerSize
import com.example.campusconnect.feature.map.mapengine.MarkerType
import com.example.campusconnect.feature.map.model.MapEventInfo
import com.example.campusconnect.feature.map.model.MapPoiInfo
import com.example.campusconnect.feature.map.model.MapShopInfo
import com.example.campusconnect.feature.map.model.MapUserProfile

class ApiMapRepo(
    private val api: MapApi = RetrofitClient.mapApi
) : MapRepo {

    override suspend fun getMarkers(
        type: MarkerType?,
        search: String?
    ): Result<List<MapMarker>> {
        return runCatching {
            val response = api.getMarkers(
                type = type?.name,
                search = search
            )

            if (!response.success || response.data == null) {
                throw Exception(response.message ?: "Unable to load map markers")
            }

            response.data.map { it.toMapMarker() }
        }
    }

    override suspend fun searchMarkers(
        query: String,
        type: MarkerType?
    ): Result<List<MapMarker>> {
        return runCatching {
            val response = api.searchMarkers(
                query = query,
                type = type?.name
            )

            if (!response.success || response.data == null) {
                throw Exception(response.message ?: "Unable to search markers")
            }

            response.data.map { it.toMapMarker() }
        }
    }

    override suspend fun getUserProfile(
        userId: String
    ): Result<MapUserProfile> {
        return runCatching {
            val response = api.getUserProfile(userId)

            if (!response.success || response.data == null) {
                throw Exception(response.message ?: "Unable to load user profile")
            }

            response.data.toMapUserProfile()
        }
    }

    override suspend fun getPoiInfo(
        poiId: String
    ): Result<MapPoiInfo> {
        return runCatching {
            val response = api.getPoiInfo(poiId)

            if (!response.success || response.data == null) {
                throw Exception(response.message ?: "Unable to load POI")
            }

            response.data.toMapPoiInfo()
        }
    }

    override suspend fun getEventInfo(
        eventId: String
    ): Result<MapEventInfo> {
        return runCatching {
            val response = api.getEventInfo(eventId)

            if (!response.success || response.data == null) {
                throw Exception(response.message ?: "Unable to load event")
            }

            response.data.toMapEventInfo()
        }
    }

    override suspend fun getShopInfo(
        shopId: String
    ): Result<MapShopInfo> {
        return runCatching {
            val response = api.getShopInfo(shopId)

            if (!response.success || response.data == null) {
                throw Exception(response.message ?: "Unable to load shop")
            }

            response.data.toMapShopInfo()
        }
    }

    override suspend fun sendConnectionRequest(
        userId: String
    ): Result<Unit> {
        return runCatching {
            val response = api.sendConnectionRequest(userId)

            if (!response.success) {
                throw Exception(response.message ?: "Unable to send connection request")
            }

            Unit
        }
    }

    override suspend fun registerEvent(
        eventId: String
    ): Result<Unit> {
        return runCatching {
            val response = api.registerEvent(
                eventId = eventId,
                request = EventRegReq()
            )

            if (!response.success) {
                throw Exception(response.message ?: "Unable to register event")
            }

            Unit
        }
    }

    override suspend fun enableEventReminder(
        eventId: String
    ): Result<Unit> {
        return runCatching {
            val response = api.enableEventReminder(eventId)

            if (!response.success) {
                throw Exception(response.message ?: "Unable to enable reminder")
            }

            Unit
        }
    }

    override suspend fun disableEventReminder(
        eventId: String
    ): Result<Unit> {
        return runCatching {
            val response = api.disableEventReminder(eventId)

            if (!response.success) {
                throw Exception(response.message ?: "Unable to disable reminder")
            }

            Unit
        }
    }
}

private fun MarkerRes.toMapMarker(): MapMarker {
    val markerType = runCatching {
        MarkerType.valueOf(type.uppercase())
    }.getOrDefault(MarkerType.POI)

    val markerSize = runCatching {
        MarkerSize.valueOf(size?.uppercase() ?: "MEDIUM")
    }.getOrDefault(MarkerSize.MEDIUM)

    return MapMarker(
        id = entityId,
        sourceId = entityId,
        type = markerType,
        latitude = latitude,
        longitude = longitude,
        label = label,
        gender = gender,
        size = markerSize,
        isHighlighted = isHighlighted ?: false,
        priority = priority ?: 0,
        isActive = isActive ?: true
    )
}

private fun UserMapRes.toMapUserProfile(): MapUserProfile {
    return MapUserProfile(
        id = id,
        fullName = fullName,
        course = course ?: "",
        startYear = startYear ?: 0,
        endYear = endYear ?: 0,
        description = description ?: "",
        badges = badges ?: emptyList(),
        medals = medals ?: emptyList(),
        mutualFriendsCount = mutualFriendsCount ?: 0
    )
}

private fun PoiRes.toMapPoiInfo(): MapPoiInfo {
    return MapPoiInfo(
        id = id,
        name = name,
        type = type ?: category ?: "poi",
        description = description ?: ""
    )
}

private fun EventMapRes.toMapEventInfo(): MapEventInfo {
    return MapEventInfo(
        id = id,
        title = title,
        hostName = hostName ?: "Campus Team",
        date = date ?: startTime ?: "Coming Soon",
        time = time ?: "TBA",
        description = description ?: "",
        posterResId = null
    )
}

private fun ShopRes.toMapShopInfo(): MapShopInfo {
    return MapShopInfo(
        id = id,
        name = name,
        type = type ?: category ?: "shop",
        description = description ?: "",
        openingTime = openingTime,
        closingTime = closingTime,
        isOpen = isOpen ?: false,
        phone = phone
    )
}