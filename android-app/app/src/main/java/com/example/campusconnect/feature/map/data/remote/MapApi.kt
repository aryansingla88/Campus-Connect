package com.example.campusconnect.feature.map.data.remote

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.map.data.remote.request.EventRegReq
import com.example.campusconnect.feature.map.data.remote.response.CategoryRes
import com.example.campusconnect.feature.map.data.remote.response.EventHostRes
import com.example.campusconnect.feature.map.data.remote.response.EventMapRes
import com.example.campusconnect.feature.map.data.remote.response.EventPreviewRes
import com.example.campusconnect.feature.map.data.remote.response.PoiRes
import com.example.campusconnect.feature.map.data.remote.response.ShopRes
import com.example.campusconnect.feature.map.data.remote.response.UserMapRes
import com.example.campusconnect.feature.map.data.remote.response.UserPreviewRes
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MapApi {

    // Domain List Endpoints -----------------------------------------------------

    @GET("presence")
    suspend fun getVisibleUsers(): ApiResponse<List<UserMapRes>>

    @GET("poi")
    suspend fun getPois(): ApiResponse<List<PoiRes>>

    @GET("events")
    suspend fun getEvents(): ApiResponse<List<EventMapRes>>

    @GET("shops")
    suspend fun getShops(): ApiResponse<List<ShopRes>>


    // User Marker & Preview Card Endpoints -------------------------------------

    @GET("presence/users/{userId}/preview")
    suspend fun getUserProfile(
        @Path("userId") userId: Int
    ): ApiResponse<UserPreviewRes>

    @POST("users/{userId}/connections/request")
    suspend fun sendConnectionRequest(
        @Path("userId") userId: String
    ): ApiResponse<Unit>


    // POI Marker Card ----------------------------------------------------------

    @GET("poi/{poiId}")
    suspend fun getPoiInfo(
        @Path("poiId") poiId: String
    ): ApiResponse<PoiRes>


    // Event Marker Card --------------------------------------------------------

    @GET("map/events/{eventId}/preview")
    suspend fun getEventPreview(
        @Path("eventId") eventId: Int
    ): ApiResponse<EventPreviewRes>

    @GET("events/{eventId}")
    suspend fun getEventInfo(
        @Path("eventId") eventId: String
    ): ApiResponse<EventMapRes>

    @GET("events/{eventId}/hosts")
    suspend fun getEventHosts(
        @Path("eventId") eventId: String
    ): ApiResponse<List<EventHostRes>>

    @POST("events/{eventId}/register")
    suspend fun registerEvent(
        @Path("eventId") eventId: String,
        @Body request: EventRegReq = EventRegReq()
    ): ApiResponse<Unit>

    @POST("events/{eventId}/reminders")
    suspend fun enableEventReminder(
        @Path("eventId") eventId: String
    ): ApiResponse<Unit>

    @DELETE("events/{eventId}/reminders")
    suspend fun disableEventReminder(
        @Path("eventId") eventId: String
    ): ApiResponse<Unit>


    // Categories ---------------------------------------------------------------

    @GET("event-categories")
    suspend fun getEventCategories(): ApiResponse<List<CategoryRes>>

    @GET("shop-categories")
    suspend fun getShopCategories(): ApiResponse<List<CategoryRes>>


    // Shop Marker Card ---------------------------------------------------------

    @GET("shops/{shopId}")
    suspend fun getShopInfo(
        @Path("shopId") shopId: String
    ): ApiResponse<ShopRes>
}