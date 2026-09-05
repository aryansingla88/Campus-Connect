package com.example.campusconnect.feature.map.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.map.data.remote.MapApi
import com.example.campusconnect.feature.map.data.remote.request.EventRegReq
import com.example.campusconnect.feature.map.data.remote.response.EventMapRes
//import com.example.campusconnect.feature.map.data.remote.response.ShopRes
import com.example.campusconnect.feature.map.data.remote.response.toMarker
import com.example.campusconnect.feature.map.data.remote.response.toPoiInfo
import com.example.campusconnect.feature.map.data.remote.response.toMapUserProfile
import com.example.campusconnect.feature.map.data.remote.response.toMapEventInfo
import com.example.campusconnect.feature.map.mapengine.model.MapMarker
import com.example.campusconnect.feature.map.mapengine.model.MarkerSize
import com.example.campusconnect.feature.map.mapengine.model.MarkerType
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
                    temporaryShopMarkers()
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
        userId: Int
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
        poiId: Int,
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
        eventId: Int
    ): Result<MapEventInfo> {
        return runCatching {

            val response = api.getEventPreview(eventId)

            if (!response.success || response.data == null) {
                throw Exception(
                    response.message ?: "Unable to load event preview"
                )
            }

            response.data.toMapEventInfo()
        }
    }

    override suspend fun getShopInfo(
        shopId: Int
    ): Result<MapShopInfo> {

        return runCatching {

            temporaryShopInfo(shopId)
        }
    }

    override suspend fun sendConnectionRequest(
        userId: Int
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
        eventId: Int
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
        eventId: Int
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
        eventId: Int
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

//private fun ShopRes.toMapMarker(): MapMarker {
//    return MapMarker(
//        id = "SHOP_$id",
//        sourceId = id,
//        type = MarkerType.SHOP,
//        latitude = latitude ?: 0.0,
//        longitude = longitude ?: 0.0,
//        label = name,
//        size = MarkerSize.MEDIUM
//    )
//}
//
//private fun ShopRes.toMapShopInfo(): MapShopInfo {
//    return MapShopInfo(
//        id = id,
//        name = name,
//        category = category ?: type ?: "RETAIL",
//        description = description,
//        openingTime = openingTime,
//        closingTime = closingTime,
//        isOpen = isOpen ?: true,
//        contactNumber = phone
//    )
//}
/*
|--------------------------------------------------------------------------
| TEMPORARY SHOP DATA
|--------------------------------------------------------------------------
|
| TODO:
| Replace with backend API integration when Shop APIs become available.
|
| These temporary models preserve the same architecture:
|
| marker.id       -> String internal map ID
| marker.sourceId -> Int temporary/backend-compatible ID
|
*/

private fun temporaryShopMarkers(): List<MapMarker> {

    return listOf(

        MapMarker(
            id = "SHOP_1",
            sourceId = 1,
            type = MarkerType.SHOP,
            latitude = 29.945000,
            longitude = 76.816000,
            label = "Campus Cafe",
            size = MarkerSize.MEDIUM
        ),

        MapMarker(
            id = "SHOP_2",
            sourceId = 2,
            type = MarkerType.SHOP,
            latitude = 29.946000,
            longitude = 76.817000,
            label = "Stationery Store",
            size = MarkerSize.MEDIUM
        ),

        MapMarker(
            id = "SHOP_3",
            sourceId = 3,
            type = MarkerType.SHOP,
            latitude = 29.947000,
            longitude = 76.816500,
            label = "Campus Mart",
            size = MarkerSize.MEDIUM
        )
    )
}


private fun temporaryShopInfo(
    shopId: Int
): MapShopInfo {

    return when (shopId) {

        1 -> MapShopInfo(
            id = 1,
            name = "Campus Cafe",
            category = "FOOD",
            description = "Temporary shop data until backend integration is available.",
            openingTime = "09:00 AM",
            closingTime = "08:00 PM",
            isOpen = true,
            contactNumber = "+91 9876543210"
        )

        2 -> MapShopInfo(
            id = 2,
            name = "Stationery Store",
            category = "STATIONERY",
            description = "Temporary shop data until backend integration is available.",
            openingTime = "09:00 AM",
            closingTime = "06:00 PM",
            isOpen = true,
            contactNumber = "+91 9876543211"
        )

        3 -> MapShopInfo(
            id = 3,
            name = "Campus Mart",
            category = "GENERAL",
            description = "Temporary shop data until backend integration is available.",
            openingTime = "10:00 AM",
            closingTime = "09:00 PM",
            isOpen = true,
            contactNumber = "+91 9876543212"
        )

        else -> MapShopInfo(
            id = shopId,
            name = "Campus Shop",
            category = "GENERAL",
            description = "Temporary shop data until backend integration is available.",
            openingTime = null,
            closingTime = null,
            isOpen = true,
            contactNumber = null
        )
    }
}