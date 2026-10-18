package com.example.campusconnect.feature.map.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.map.data.remote.MapApi
import com.example.campusconnect.feature.map.data.PoiRepository
import com.example.campusconnect.feature.map.data.remote.request.UpdatePresenceRequest
import com.example.campusconnect.feature.map.data.mapper.MapEventMapper
import com.example.campusconnect.feature.map.data.mapper.MapPoiMapper
import com.example.campusconnect.feature.map.data.mapper.MapUserMapper
import com.example.campusconnect.feature.map.model.EventFilter
import com.example.campusconnect.feature.map.model.MapEventInfo
import com.example.campusconnect.feature.map.model.MapMarker
import com.example.campusconnect.feature.map.model.MapPoiInfo
import com.example.campusconnect.feature.map.model.MapShopInfo
import com.example.campusconnect.feature.map.model.MapUserProfile
import com.example.campusconnect.feature.map.model.MapFilterOption
import com.example.campusconnect.feature.map.mapengine.model.MarkerType
import com.example.campusconnect.feature.map.model.ShopMarkerData
import com.example.campusconnect.feature.metadata.courses.CourseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ApiMapRepo(
    private val api: MapApi = RetrofitClient.mapApi,
    private val courseRepository: CourseRepository,
    private val poiRepository: PoiRepository
) : MapRepo {

    override suspend fun getMarkers(
        type: MarkerType?,
        eventCategoryId: Int?,
        poiCategory: String?
    ): Result<List<MapMarker>> = runCatching {
        when (type) {
            MarkerType.USER -> {
                val response = api.getVisibleUsers()
                requireData(response.success, response.message, response.data, "Unable to load users")
                    .map { MapUserMapper.toMarker(it) }
            }
            MarkerType.POI -> {
                poiRepository.getPois(poiCategory).getOrThrow()
                    .mapNotNull(MapPoiMapper::toMarker)
            }
            MarkerType.EVENT -> {
                val response = api.getEventMarkers(eventCategoryId)
                requireData(response.success, response.message, response.data, "Unable to load events")
                    .mapNotNull(MapEventMapper::toMarker)
            }
            MarkerType.SHOP -> temporaryShopMarkers()
            null -> loadAllMarkers()
        }
    }

    override suspend fun updateMyPresence(
        latitude: Double,
        longitude: Double,
        bearerToken: String
    ): Result<Unit> = runCatching {
        val response = api.updateMyPresence(
            request = UpdatePresenceRequest(latitude = latitude, longitude = longitude),
            authorization = "Bearer $bearerToken"
        )
        if (!response.success) error(response.message ?: "Unable to update presence")
    }

    private suspend fun loadAllMarkers(): List<MapMarker> = coroutineScope {
        val users = async {
            runCatching { api.getVisibleUsers() }.getOrNull()
                ?.takeIf { it.success }
                ?.data.orEmpty()
                .map { MapUserMapper.toMarker(it) }
        }
        val pois = async {
                poiRepository.getPois().getOrDefault(emptyList())
                .mapNotNull(MapPoiMapper::toMarker)
        }
        val events = async {
            runCatching { api.getEventMarkers() }.getOrNull()
                ?.takeIf { it.success }
                ?.data.orEmpty()
                .mapNotNull(MapEventMapper::toMarker)
        }
        users.await() + pois.await() + events.await()
    }

    override suspend fun getUserProfile(userId: Int): Result<MapUserProfile> = runCatching {
        val response = api.getUserProfile(userId)
        val user = requireData(response.success, response.message, response.data, "Unable to load user profile")
        val course = user.courseId?.let { courseId -> courseRepository.getCourseById(courseId) }
        MapUserMapper.toProfile(user, course)
    }

    override suspend fun getPoiInfo(poiId: Int, fallbackName: String): Result<MapPoiInfo> = runCatching {
        val response = api.getPoiInfo(poiId)
        val poi = response.data
        if (!response.success || poi == null) {
            MapPoiInfo(id = poiId, name = fallbackName.ifBlank { "Campus POI" }, category = "GENERAL")
        } else MapPoiMapper.toInfo(poi)
    }

    override suspend fun getEventInfo(eventId: Int): Result<MapEventInfo> = runCatching {
        val response = api.getEventPreview(eventId)
        val event = requireData(response.success, response.message, response.data, "Unable to load event preview")
        MapEventMapper.toInfo(event)
    }

    override suspend fun getShopInfo(shopId: Int): Result<MapShopInfo> = runCatching {
        temporaryShopInfo(shopId)
    }

    override suspend fun sendConnectionRequest(userId: Int): Result<Unit> = runCatching {
        val response = api.sendConnectionRequest(userId)
        if (!response.success) error(response.message ?: "Unable to send connection request")
    }

    override suspend fun enableEventReminder(eventId: Int): Result<Unit> = runCatching {
        val response = api.enableEventReminder(eventId)
        if (!response.success) error(response.message ?: "Unable to enable reminder")
    }

    override suspend fun disableEventReminder(eventId: Int): Result<Unit> = runCatching {
        val response = api.disableEventReminder(eventId)
        if (!response.success) error(response.message ?: "Unable to disable reminder")
    }

    override suspend fun getEventCategories(): Result<List<EventFilter>> = runCatching {
        val response = api.getEventCategories()
        requireData(response.success, response.message, response.data, "Unable to load event categories")
            .map(MapEventMapper::toFilter)
    }

    override suspend fun getPoiCategories(): Result<List<MapFilterOption>> =
        poiRepository.getCategories().map { categories ->
            categories.map { category ->
                MapFilterOption(
                    category,
                    category.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
                )
            }
        }

    private fun temporaryShopMarkers(): List<MapMarker> = listOf(
        ShopMarkerData(sourceId = 1, latitude = 29.945000, longitude = 76.816000, name = "Campus Cafe"),
        ShopMarkerData(sourceId = 2, latitude = 29.946000, longitude = 76.817000, name = "Stationery Store"),
        ShopMarkerData(sourceId = 3, latitude = 29.947000, longitude = 76.816500, name = "Campus Mart")
    )

    private fun temporaryShopInfo(shopId: Int): MapShopInfo = when (shopId) {
        1 -> MapShopInfo(1, "Campus Cafe", "FOOD", "Temporary shop data until backend integration is available.", "09:00 AM", "08:00 PM", true, "+91 9876543210")
        2 -> MapShopInfo(2, "Stationery Store", "STATIONERY", "Temporary shop data until backend integration is available.", "09:00 AM", "06:00 PM", true, "+91 9876543211")
        3 -> MapShopInfo(3, "Campus Mart", "GENERAL", "Temporary shop data until backend integration is available.", "10:00 AM", "09:00 PM", true, "+91 9876543212")
        else -> MapShopInfo(shopId, "Campus Shop", "GENERAL", "Temporary shop data until backend integration is available.", null, null, true, null)
    }

    private fun <T : Any> requireData(
        success: Boolean,
        message: String?,
        data: T?,
        fallbackMessage: String
    ): T {
        if (!success || data == null) error(message ?: fallbackMessage)
        return data
    }
}
