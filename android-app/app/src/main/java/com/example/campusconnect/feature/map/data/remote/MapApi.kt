package com.example.campusconnect.feature.map.data.remote

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.map.data.remote.request.EventRegReq
import com.example.campusconnect.feature.map.data.remote.response.CategoryRes
import com.example.campusconnect.feature.map.data.remote.response.EventHostRes
import com.example.campusconnect.feature.map.data.remote.response.EventMapRes
import com.example.campusconnect.feature.map.data.remote.response.MarkerRes
import com.example.campusconnect.feature.map.data.remote.response.PoiRes
import com.example.campusconnect.feature.map.data.remote.response.ShopRes
import com.example.campusconnect.feature.map.data.remote.response.UserMapRes
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MapApi {

    // Map markers -------------------------------------------------------------

    @GET("map/markers")
    suspend fun getMarkers(
        @Query("type") type: String? = null,
        @Query("search") search: String? = null
    ): ApiResponse<List<MarkerRes>>

    @GET("map/search")
    suspend fun searchMarkers(
        @Query("q") query: String,
        @Query("type") type: String? = null
    ): ApiResponse<List<MarkerRes>>


    // User marker card -------------------------------------------------------------

    @GET("users/{userId}")
    suspend fun getUserProfile(
        @Path("userId") userId: String
    ): ApiResponse<UserMapRes>

    @POST("users/{userId}/connections/request")
    suspend fun sendConnectionRequest(
        @Path("userId") userId: String
    ): ApiResponse<Unit>


    // POI marker card -------------------------------------------------------------

    @GET("poi/{poiId}")
    suspend fun getPoiInfo(
        @Path("poiId") poiId: String
    ): ApiResponse<PoiRes>


    // Event marker card -------------------------------------------------------------

    @GET("events/{eventId}")
    suspend fun getEventInfo(
        @Path("eventId") eventId: String
    ): ApiResponse<EventMapRes>

    @GET("events/{eventId}/hosts")
    suspend fun getEventHosts(
        @Path("eventId") eventId: String
    ): ApiResponse<List<EventHostRes>>

    @POST("events/{eventId}/registrations")
    suspend fun registerEvent(
        @Path("eventId") eventId: String,
        @Body request: EventRegReq
    ): ApiResponse<Unit>

    @POST("events/{eventId}/reminders")
    suspend fun enableEventReminder(
        @Path("eventId") eventId: String
    ): ApiResponse<Unit>

    @DELETE("events/{eventId}/reminders")
    suspend fun disableEventReminder(
        @Path("eventId") eventId: String
    ): ApiResponse<Unit>


    // Categories -------------------------------------------------------------

    @GET("event-categories")
    suspend fun getEventCategories(): ApiResponse<List<CategoryRes>>

    @GET("shop-categories")
    suspend fun getShopCategories(): ApiResponse<List<CategoryRes>>


    // Shop marker card -------------------------------------------------------------
    // Shops should be loaded only in dedicated SHOP mode using:
    // GET map/markers?type=SHOP

    @GET("shops/{shopId}")
    suspend fun getShopInfo(
        @Path("shopId") shopId: String
    ): ApiResponse<ShopRes>
}