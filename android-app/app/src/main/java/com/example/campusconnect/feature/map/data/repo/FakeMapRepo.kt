package com.example.campusconnect.feature.map.data.repo

import com.example.campusconnect.feature.map.data.fake.FakeMapEventInfoService
import com.example.campusconnect.feature.map.data.fake.FakeMapPoiInfoService
import com.example.campusconnect.feature.map.data.fake.FakeMapService
import com.example.campusconnect.feature.map.data.fake.FakeMapUserProfileService
import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerType
import com.example.campusconnect.feature.map.model.MapEventInfo
import com.example.campusconnect.feature.map.model.MapPoiInfo
import com.example.campusconnect.feature.map.model.MapShopInfo
import com.example.campusconnect.feature.map.model.MapUserProfile

class FakeMapRepo : MapRepo {

    private val fakeMapService = FakeMapService()
    private val fakeUserProfileService = FakeMapUserProfileService()

    override suspend fun getMarkers(
        type: MarkerType?,
        search: String?
    ): Result<List<MapMarker>> {
        var markers = fakeMapService.getMarkers()

        // changed: shops default map me nahi dikhengi
        // shops sirf dedicated SHOP mode me aayengi
        markers = if (type == MarkerType.SHOP) {
            markers.filter { it.type == MarkerType.SHOP }
        } else {
            markers.filter { it.type != MarkerType.SHOP }
        }

        if (type != null && type != MarkerType.SHOP) {
            markers = markers.filter { it.type == type }
        }

        if (!search.isNullOrBlank()) {
            markers = markers.filter {
                it.label.contains(search, ignoreCase = true)
            }
        }

        return Result.success(markers)
    }

    override suspend fun searchMarkers(
        query: String,
        type: MarkerType?
    ): Result<List<MapMarker>> {
        return getMarkers(
            type = type,
            search = query
        )
    }

    override suspend fun getUserProfile(
        userId: String
    ): Result<MapUserProfile> {
        return Result.success(
            fakeUserProfileService.getProfileByMarkerId(userId)
        )
    }

    override suspend fun getPoiInfo(
        poiId: String
    ): Result<MapPoiInfo> {
        return Result.success(
            FakeMapPoiInfoService.getPoiInfo(
                poiId = poiId,
                fallbackName = "Campus POI"
            )
        )
    }

    override suspend fun getEventInfo(
        eventId: String
    ): Result<MapEventInfo> {
        return Result.success(
            FakeMapEventInfoService.getEventInfo(
                eventId = eventId,
                fallbackTitle = "Campus Event"
            )
        )
    }

    override suspend fun getShopInfo(
        shopId: String
    ): Result<MapShopInfo> {
        return Result.success(
            MapShopInfo(
                id = shopId,
                name = "Campus Shop",
                type = "shop",
                description = "Campus shop details will be loaded later.",
                openingTime = "09:00 AM",
                closingTime = "08:00 PM",
                isOpen = true,
                phone = null
            )
        )
    }

    override suspend fun sendConnectionRequest(
        userId: String
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun registerEvent(
        eventId: String
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun enableEventReminder(
        eventId: String
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun disableEventReminder(
        eventId: String
    ): Result<Unit> {
        return Result.success(Unit)
    }
}