package com.example.campusconnect.feature.map.data.repo

import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerType
import com.example.campusconnect.feature.map.model.MapEventInfo
import com.example.campusconnect.feature.map.model.MapPoiInfo
import com.example.campusconnect.feature.map.model.MapShopInfo
import com.example.campusconnect.feature.map.model.MapUserProfile

interface MapRepo {

    suspend fun getMarkers(
        type: MarkerType? = null,
        search: String? = null
    ): Result<List<MapMarker>>

    suspend fun searchMarkers(
        query: String,
        type: MarkerType? = null
    ): Result<List<MapMarker>>

    suspend fun getUserProfile(
        userId: String
    ): Result<MapUserProfile>

    suspend fun getPoiInfo(
        poiId: String
    ): Result<MapPoiInfo>

    suspend fun getEventInfo(
        eventId: String
    ): Result<MapEventInfo>

    suspend fun getShopInfo(
        shopId: String
    ): Result<MapShopInfo>

    suspend fun sendConnectionRequest(
        userId: String
    ): Result<Unit>

    suspend fun registerEvent(
        eventId: String
    ): Result<Unit>

    suspend fun enableEventReminder(
        eventId: String
    ): Result<Unit>

    suspend fun disableEventReminder(
        eventId: String
    ): Result<Unit>
}