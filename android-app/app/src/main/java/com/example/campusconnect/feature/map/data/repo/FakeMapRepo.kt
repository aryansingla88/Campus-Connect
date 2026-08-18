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

    // Modified: Only 'search' parameter removed from signature & body
    override suspend fun getMarkers(
        type: MarkerType?
    ): Result<List<MapMarker>> {
        var markers = fakeMapService.getMarkers()

        // Shops sirf dedicated SHOP mode me dikhenge
        markers = if (type == MarkerType.SHOP) {
            markers.filter { it.type == MarkerType.SHOP }
        } else {
            markers.filter { it.type != MarkerType.SHOP }
        }

        if (type != null && type != MarkerType.SHOP) {
            markers = markers.filter { it.type == type }
        }

        return Result.success(markers)
    }

    // Preserved: User preview card data
    override suspend fun getUserProfile(
        userId: String
    ): Result<MapUserProfile> {
        return Result.success(
            fakeUserProfileService.getProfileByMarkerId(userId)
        )
    }

    // Preserved: Connection request action
    override suspend fun sendConnectionRequest(
        userId: String
    ): Result<Unit> {
        return Result.success(Unit)
    }

    // Preserved: POI bottom sheet details
    override suspend fun getPoiInfo(
        poiId: String,
        fallbackName: String
    ): Result<MapPoiInfo> {
        return Result.success(
            FakeMapPoiInfoService.getPoiInfo(
                poiId = poiId,
                fallbackName = if (fallbackName.isBlank()) "Campus POI" else fallbackName
            )
        )
    }

    // Preserved: Event bottom sheet details
    override suspend fun getEventInfo(eventId: String): Result<MapEventInfo> {
        return runCatching {
            FakeMapEventInfoService.getEventInfo(
                eventId = eventId,
                fallbackTitle = "Campus Event"
            )
        }
    }

    // Preserved: Event actions
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

    // Preserved: Shop bottom sheet details
    override suspend fun getShopInfo(
        shopId: String
    ): Result<MapShopInfo> {
        return Result.success(
            MapShopInfo(
                id = shopId,
                name = "Campus Shop",
                category = "SHOP",
                description = "Campus shop details will be loaded later.",
                openingTime = "09:00 AM",
                closingTime = "08:00 PM",
                isOpen = true,
                contactNumber = "+91 9876543210"
            )
        )
    }
}