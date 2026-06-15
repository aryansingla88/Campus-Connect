package com.example.campusconnect.feature.map.data.repo

import com.example.campusconnect.feature.map.data.fake.FakeMapEventInfoService
import com.example.campusconnect.feature.map.data.fake.FakeMapPoiInfoService
import com.example.campusconnect.feature.map.data.fake.FakeMapService
import com.example.campusconnect.feature.map.data.fake.FakeMapUserProfileService
import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.model.MapEventInfo
import com.example.campusconnect.feature.map.model.MapPoiInfo
import com.example.campusconnect.feature.map.model.MapUserProfile

class FakeMapRepo : MapRepo {

    private val fakeMapService = FakeMapService()
    private val fakeUserProfileService = FakeMapUserProfileService()

    override suspend fun getMarkers(): Result<List<MapMarker>> {
        return Result.success(
            fakeMapService.getMarkers()
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
}