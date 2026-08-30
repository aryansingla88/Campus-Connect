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
        type: MarkerType?
    ): Result<List<MapMarker>> {
        var markers = fakeMapService.getMarkers()

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

    override suspend fun getUserProfile(
        userId: Int
    ): Result<MapUserProfile> {
        return Result.success(
            fakeUserProfileService.getProfileByMarkerId(userId)
        )
    }

    override suspend fun sendConnectionRequest(
        userId: Int
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getPoiInfo(
        poiId: Int,
        fallbackName: String
    ): Result<MapPoiInfo> {
        return Result.success(
            FakeMapPoiInfoService.getPoiInfo(
                poiId = poiId,
                fallbackName = fallbackName.ifBlank { "Campus POI" }
            )
        )
    }

    override suspend fun getEventInfo(eventId: Int): Result<MapEventInfo> {
        return runCatching {
            FakeMapEventInfoService.getEventInfo(
                eventId = eventId,
                fallbackTitle = "Campus Event"
            )
        }
    }

    override suspend fun registerEvent(
        eventId: Int
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun enableEventReminder(
        eventId: Int
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun disableEventReminder(
        eventId: Int
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getShopInfo(
        shopId: Int
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