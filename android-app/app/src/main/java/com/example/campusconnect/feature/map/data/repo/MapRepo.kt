package com.example.campusconnect.feature.map.data.repo

import com.example.campusconnect.feature.map.model.MapMarker
import com.example.campusconnect.feature.map.mapengine.model.MarkerType
import com.example.campusconnect.feature.map.model.EventFilter
import com.example.campusconnect.feature.map.model.MapFilterOption
import com.example.campusconnect.feature.map.model.MapEventInfo
import com.example.campusconnect.feature.map.model.MapPoiInfo
import com.example.campusconnect.feature.map.model.MapShopInfo
import com.example.campusconnect.feature.map.model.MapUserProfile

interface MapRepo {

    suspend fun getMarkers(
        type: MarkerType? = null,
        eventCategoryId: Int? = null,
        poiCategory: String? = null,
        clubId: Int? = null
    ): Result<List<MapMarker>>

    suspend fun getMyConnectionUserIds(): Result<Set<Int>>

    suspend fun getMyJoinedClubs(): Result<List<MapFilterOption>>

    // Backend user ID
    suspend fun getUserProfile(
        userId: Int
    ): Result<MapUserProfile>

    // Backend POI ID
    suspend fun getPoiInfo(
        poiId: Int,
        fallbackName: String = ""
    ): Result<MapPoiInfo>

    // Backend event ID
    suspend fun getEventInfo(
        eventId: Int
    ): Result<MapEventInfo>

    // Temporary shop ID
    suspend fun getShopInfo(
        shopId: Int
    ): Result<MapShopInfo>

    // Backend user ID
    suspend fun sendConnectionRequest(
        userId: Int
    ): Result<Unit>


    // Backend event ID
    suspend fun enableEventReminder(
        eventId: Int
    ): Result<Unit>

    // Backend event ID
    suspend fun disableEventReminder(
        eventId: Int
    ): Result<Unit>

    // Event Categories / Filters
    suspend fun getEventCategories(): Result<List<EventFilter>>

    suspend fun getPoiCategories(): Result<List<MapFilterOption>>
}
