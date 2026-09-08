package com.example.campusconnect.feature.map.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.map.data.remote.MapApi
import com.example.campusconnect.feature.map.data.remote.response.EventMarkerResponse
//import com.example.campusconnect.feature.map.data.remote.response.ShopResponse
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
import com.example.campusconnect.feature.metadata.courses.CourseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import android.util.Log
class ApiMapRepo(
    private val api: MapApi = RetrofitClient.mapApi,
    private val courseRepository: CourseRepository
) : MapRepo {

    override suspend fun getMarkers(
        type: MarkerType?
    ): Result<List<MapMarker>> {
        return runCatching {

            Log.d("MAP_API", "========================================")
            Log.d("MAP_API", "getMarkers STARTED | filter = $type")

            when (type) {

                MarkerType.USER -> {

                    Log.d("USER_MARKER", "========================================")
                    Log.d("USER_MARKER", "USER MARKERS REQUESTED")
                    Log.d("USER_MARKER", "Calling API: GET map/presence")

                    val response = try {
                        api.getVisibleUsers()
                    } catch (e: Exception) {
                        Log.e(
                            "USER_MARKER",
                            "map/presence NETWORK/API ERROR",
                            e
                        )
                        throw e
                    }

                    Log.d(
                        "USER_MARKER",
                        "API RESPONSE | success=${response.success}, message=${response.message}"
                    )

                    Log.d(
                        "USER_MARKER",
                        "RAW USER COUNT = ${response.data?.size ?: 0}"
                    )

                    if (!response.success || response.data == null) {

                        Log.e(
                            "USER_MARKER",
                            "API FAILED | message=${response.message}"
                        )

                        throw Exception(
                            response.message ?: "Unable to load users"
                        )
                    }

                    response.data.forEachIndexed { index, user ->

                        Log.d(
                            "USER_MARKER",
                            """
            USER [$index]
            id=${user.userId}
            name=${user.username}
            latitude=${user.latitude}
            longitude=${user.longitude}
            gender=${user.gender}
            """.trimIndent()
                        )
                    }

                    val markers = response.data.mapIndexed { index, user ->

                        try {

                            val marker = user.toMarker()

                            Log.d(
                                "USER_MARKER",
                                """
            MAPPED MARKER [$index]
            id=${marker.id}
            sourceId=${marker.sourceId}
            type=${marker.type}
            latitude=${marker.latitude}
            longitude=${marker.longitude}
            label=${marker.label}
            gender=${marker.gender}
            insideCampus=${marker.insideCampus}
            """.trimIndent()
                            )

                            marker

                        } catch (e: Exception) {

                            Log.e(
                                "USER_MARKER",
                                "FAILED TO MAP USER [$index]",
                                e
                            )

                            throw e
                        }
                    }

                    Log.d(
                        "USER_MARKER",
                        "FINAL USER MARKER COUNT = ${markers.size}"
                    )

                    Log.d("USER_MARKER", "========================================")

                    markers
                }


                MarkerType.POI -> {

                    Log.d("MAP_API", "Calling API: map/poi")

                    val response = api.getPois()

                    Log.d(
                        "MAP_API",
                        "map/poi response | success=${response.success}, dataSize=${response.data?.size}, message=${response.message}"
                    )

                    if (!response.success || response.data == null) {

                        Log.e(
                            "MAP_API",
                            "map/poi FAILED"
                        )

                        throw Exception(
                            response.message ?: "Unable to load POIs"
                        )
                    }

                    Log.d(
                        "MAP_API",
                        "map/poi SUCCESS | mapping ${response.data.size} POIs"
                    )

                    response.data.map {
                        it.toMarker()
                    }
                }


                MarkerType.EVENT -> {

                    Log.d("MAP_API", "Calling API: map/events")

                    val response = api.getEventMarkers()

                    Log.d(
                        "MAP_API",
                        "map/events response | success=${response.success}, dataSize=${response.data?.size}, message=${response.message}"
                    )

                    if (!response.success || response.data == null) {

                        Log.e(
                            "MAP_API",
                            "map/events FAILED"
                        )

                        throw Exception(
                            response.message ?: "Unable to load events"
                        )
                    }

                    Log.d(
                        "MAP_API",
                        "map/events SUCCESS | mapping ${response.data.size} events"
                    )

                    response.data.map {
                        it.toMapMarker()
                    }
                }


                MarkerType.SHOP -> {

                    Log.d(
                        "MAP_API",
                        "Using TEMPORARY SHOP markers"
                    )

                    temporaryShopMarkers()
                }


                null -> coroutineScope {

                    Log.d(
                        "MAP_API",
                        "ALL MARKERS requested - starting parallel API calls"
                    )


                    val usersDeferred = async {

                        Log.d(
                            "USER_MARKER",
                            "========================================"
                        )

                        Log.d(
                            "USER_MARKER",
                            "INITIAL MAP LOAD - Calling API: map/presence"
                        )

                        val result = runCatching {
                            api.getVisibleUsers()
                        }

                        if (result.isFailure) {

                            Log.e(
                                "USER_MARKER",
                                "map/presence NETWORK/API ERROR",
                                result.exceptionOrNull()
                            )
                        }

                        val response = result.getOrNull()

                        Log.d(
                            "USER_MARKER",
                            "API RESPONSE | success=${response?.success}, message=${response?.message}"
                        )

                        Log.d(
                            "USER_MARKER",
                            "RAW USER COUNT = ${response?.data?.size ?: 0}"
                        )

                        response?.data?.forEachIndexed { index, user ->

                            Log.d(
                                "USER_MARKER",
                                """
            RAW USER [$index]
            id=${user.userId}
            username=${user.username}
            latitude=${user.latitude}
            longitude=${user.longitude}
            gender=${user.gender}
            insideCampus=${user.insideCampus}
            """.trimIndent()
                            )
                        }

                        val markers = response?.data?.mapIndexed { index, user ->

                            try {

                                val marker = user.toMarker()

                                Log.d(
                                    "USER_MARKER",
                                    """
                MAPPED USER MARKER [$index]
                id=${marker.id}
                sourceId=${marker.sourceId}
                type=${marker.type}
                latitude=${marker.latitude}
                longitude=${marker.longitude}
                label=${marker.label}
                gender=${marker.gender}
                """.trimIndent()
                                )

                                marker

                            } catch (e: Exception) {

                                Log.e(
                                    "USER_MARKER",
                                    "FAILED TO MAP USER [$index]",
                                    e
                                )

                                throw e
                            }

                        } ?: emptyList()

                        Log.d(
                            "USER_MARKER",
                            "FINAL USER MARKER COUNT = ${markers.size}"
                        )

                        Log.d(
                            "USER_MARKER",
                            "========================================"
                        )

                        markers
                    }


                    val poisDeferred = async {

                        Log.d(
                            "MAP_API",
                            "Calling API: map/poi"
                        )

                        val result = runCatching {
                            api.getPois()
                        }

                        if (result.isFailure) {

                            Log.e(
                                "MAP_API",
                                "map/poi NETWORK/API ERROR",
                                result.exceptionOrNull()
                            )
                        }

                        val response = result.getOrNull()

                        Log.d(
                            "MAP_API",
                            "map/poi response | success=${response?.success}, dataSize=${response?.data?.size}, message=${response?.message}"
                        )

                        response?.data?.map {
                            it.toMarker()
                        } ?: emptyList()
                    }


                    val eventsDeferred = async {

                        Log.d(
                            "MAP_API",
                            "Calling API: map/events"
                        )

                        val result = runCatching {
                            api.getEventMarkers()
                        }

                        if (result.isFailure) {

                            Log.e(
                                "MAP_API",
                                "map/events NETWORK/API ERROR",
                                result.exceptionOrNull()
                            )
                        }

                        val response = result.getOrNull()

                        Log.d(
                            "MAP_API",
                            "map/events response | success=${response?.success}, dataSize=${response?.data?.size}, message=${response?.message}"
                        )

                        response?.data?.map {
                            it.toMapMarker()
                        } ?: emptyList()
                    }


                    val users = usersDeferred.await()
                    val pois = poisDeferred.await()
                    val events = eventsDeferred.await()


                    Log.d(
                        "MAP_API",
                        "RESULTS | users=${users.size}, pois=${pois.size}, events=${events.size}"
                    )


                    val finalMarkers = users + pois + events


                    Log.d(
                        "MAP_API",
                        "FINAL TOTAL MARKERS = ${finalMarkers.size}"
                    )

                    Log.d(
                        "MAP_API",
                        "========================================"
                    )

                    finalMarkers
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

            val userData = response.data

            val courseData = userData.courseId?.let { courseId ->
                courseRepository.getCourseById(courseId)
            }

            userData.toMapUserProfile(courseData)
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

private fun EventMarkerResponse.toMapMarker(): MapMarker {
    return MapMarker(
        id = "EVENT_$id",
        sourceId = id,
        type = MarkerType.EVENT,
        latitude = latitude ?: 0.0,
        longitude = longitude ?: 0.0,
        label = title,
        priority = priority ?: 0,
        size = MarkerSize.MEDIUM
    )
}

//private fun ShopResponse.toMapMarker(): MapMarker {
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
//private fun ShopResponse.toMapShopInfo(): MapShopInfo {
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