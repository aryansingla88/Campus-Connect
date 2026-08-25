package com.example.campusconnect.feature.map.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.map.data.remote.MapApi
import com.example.campusconnect.feature.map.data.remote.request.EventRegReq
import com.example.campusconnect.feature.map.data.remote.response.EventMapRes
import com.example.campusconnect.feature.map.data.remote.response.PoiRes
import com.example.campusconnect.feature.map.data.remote.response.ShopRes
import com.example.campusconnect.feature.map.data.remote.response.toMarker
import com.example.campusconnect.feature.map.data.remote.response.toPoiInfo
import com.example.campusconnect.feature.map.data.remote.response.toMapUserProfile
import com.example.campusconnect.feature.map.data.remote.response.toMapEventInfo
import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerSize
import com.example.campusconnect.feature.map.mapengine.MarkerType
import com.example.campusconnect.feature.map.model.MapEventInfo
import com.example.campusconnect.feature.map.model.MapPoiInfo
import com.example.campusconnect.feature.map.model.MapShopInfo
import com.example.campusconnect.feature.map.model.MapUserProfile
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ApiMapRepo(
    private val api: MapApi = RetrofitClient.mapApi
) : MapRepo {

    override suspend fun getMarkers(
        type: MarkerType?
    ): Result<List<MapMarker>> {
        return runCatching {
            when (type) {
                MarkerType.USER -> {
                    val response = api.getVisibleUsers()
                    if (!response.success || response.data == null) throw Exception(response.message ?: "Unable to load users")
                    response.data.map { it.toMarker() }
                }
                MarkerType.POI -> {
                    val response = api.getPois()
                    if (!response.success || response.data == null) throw Exception(response.message ?: "Unable to load POIs")
                    response.data.map { it.toMarker() }
                }
                MarkerType.EVENT -> {
                    val response = api.getEvents()
                    if (!response.success || response.data == null) throw Exception(response.message ?: "Unable to load events")
                    response.data.map { it.toMapMarker() }
                }
                MarkerType.SHOP -> {
                    val response = api.getShops()
                    if (!response.success || response.data == null) throw Exception(response.message ?: "Unable to load shops")
                    response.data.map { it.toMapMarker() }
                }
                null -> coroutineScope {
                    val usersDeferred = async { runCatching { api.getVisibleUsers() }.getOrNull()?.data?.map { it.toMarker() } ?: emptyList() }
                    val poisDeferred = async { runCatching { api.getPois() }.getOrNull()?.data?.map { it.toMarker() } ?: emptyList() }
                    val eventsDeferred = async { runCatching { api.getEvents() }.getOrNull()?.data?.map { it.toMapMarker() } ?: emptyList() }

                    val users = usersDeferred.await()
                    val pois = poisDeferred.await()
                    val events = eventsDeferred.await()

                    users + pois + events
                }
            }
        }
    }

    override suspend fun getUserProfile(
        userId: String
    ): Result<MapUserProfile> {
        return runCatching {
            val numericUserId = userId.replace("USER_", "").toIntOrNull() ?: 1
            val response = api.getUserProfile(numericUserId)

            if (!response.success || response.data == null) {
                throw Exception(response.message ?: "Unable to load user profile")
            }

            response.data.toMapUserProfile()
        }
    }

    override suspend fun getPoiInfo(
        poiId: String,
        fallbackName: String
    ): Result<MapPoiInfo> {
        return runCatching {
            val response = api.getPoiInfo(poiId)

            if (!response.success || response.data == null) {
                MapPoiInfo(
                    id = poiId,
                    name = fallbackName.ifBlank { "Campus POI" },
                    category = "GENERAL"
                )
            } else {
                response.data.toPoiInfo()
            }
        }
    }

    override suspend fun getEventInfo(
        eventId: String
    ): Result<MapEventInfo> {
        return runCatching {
            val numericId = eventId.replace("EVENT_", "").replace("event_", "").toIntOrNull() ?: 1
            val response = api.getEventPreview(numericId)

            if (!response.success || response.data == null) {
                throw Exception(response.message ?: "Unable to load event preview")
            }

            val eventData = response.data.toMapEventInfo()

            // Testing ke liye hardcode test URL inject karein
            eventData.copy(
                posterUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800"
            )
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

// Extension functions for DTO mappings

private fun EventMapRes.toMapMarker(): MapMarker {
    return MapMarker(
        id = "EVENT_$id",
        sourceId = id,
        type = MarkerType.EVENT,
        latitude = latitude ?: 0.0,
        longitude = longitude ?: 0.0,
        label = title,
        size = MarkerSize.MEDIUM
    )
}

private fun ShopRes.toMapMarker(): MapMarker {
    return MapMarker(
        id = "SHOP_$id",
        sourceId = id,
        type = MarkerType.SHOP,
        latitude = latitude ?: 0.0,
        longitude = longitude ?: 0.0,
        label = name,
        size = MarkerSize.MEDIUM
    )
}

private fun ShopRes.toMapShopInfo(): MapShopInfo {
    return MapShopInfo(
        id = id,
        name = name,
        category = category ?: type ?: "RETAIL",
        description = description,
        openingTime = openingTime,
        closingTime = closingTime,
        isOpen = isOpen ?: true,
        contactNumber = phone
    )
}