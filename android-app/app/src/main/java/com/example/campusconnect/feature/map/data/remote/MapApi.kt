package com.example.campusconnect.feature.map.data.remote

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.map.data.remote.response.EventCategoryResponse
import com.example.campusconnect.feature.map.data.remote.response.EventMarkerResponse
import com.example.campusconnect.feature.map.data.remote.response.EventPreviewResponse
import com.example.campusconnect.feature.map.data.remote.response.PoiResponse
//import com.example.campusconnect.feature.map.data.remote.response.ShopResponse
import com.example.campusconnect.feature.map.data.remote.response.UserMapRes
import com.example.campusconnect.feature.map.data.remote.response.UserPreviewResponse
import com.example.campusconnect.feature.map.data.remote.request.UpdatePresenceRequest
import com.example.campusconnect.feature.map.data.remote.response.PresenceResponse
import retrofit2.http.*

interface MapApi {

    // Domain List Endpoints -----------------------------------------------------

    @GET("map/presence")
    suspend fun getVisibleUsers(): ApiResponse<List<UserMapRes>>

    @GET("map/poi")
    suspend fun getPois(): ApiResponse<List<PoiResponse>>

    @GET("map/events")
    suspend fun getEventMarkers(): ApiResponse<List<EventMarkerResponse>>

   // @GET("shops") //todo
   // suspend fun getShops(): ApiResponse<List<ShopResponse>>


    // User Marker & Preview Card Endpoints -------------------------------------

    @GET("map/presence/users/{userId}/preview")
    suspend fun getUserProfile(
        @Path("userId") userId: Int
    ): ApiResponse<UserPreviewResponse>

    @POST("users/{userId}/connections/request")
    suspend fun sendConnectionRequest(
        @Path("userId") userId: Int
    ): ApiResponse<Unit>


    // POI Marker Card ----------------------------------------------------------

    @GET("map/poi/{poiId}")
    suspend fun getPoiInfo(
        @Path("poiId") poiId: Int
    ): ApiResponse<PoiResponse>


    // Event Marker Card --------------------------------------------------------

    @GET("map/events/{eventId}/preview")
    suspend fun getEventPreview(
        @Path("eventId") eventId: Int
    ): ApiResponse<EventPreviewResponse>


    @POST("events/{eventId}/reminders")
    suspend fun enableEventReminder(
        @Path("eventId") eventId: Int
    ): ApiResponse<Unit>

    @DELETE("events/{eventId}/reminders")
    suspend fun disableEventReminder(
        @Path("eventId") eventId: Int
    ): ApiResponse<Unit>


    // Categories ---------------------------------------------------------------

    @GET("event-categories")
    suspend fun getEventCategories(): ApiResponse<List<EventCategoryResponse>>

    @GET("shop-categories")
    suspend fun getShopCategories(): ApiResponse<List<EventCategoryResponse>>

    // ---------------------------------------------------------
// Presence APIs
// ---------------------------------------------------------

    @PATCH("map/presence/me")
    suspend fun updateMyPresence(
        @Body request: UpdatePresenceRequest
    ): ApiResponse<PresenceResponse>

    @GET("map/presence/me")
    suspend fun getMyPresence(): ApiResponse<PresenceResponse>


    // Shop ------------------------------------------------

    /*
     * TODO:
     * Shop backend APIs are not available yet.
     *
     * When backend support is added, expected APIs can be added here:
     *
     * @GET("shops")
     * suspend fun getShops(): ApiResponse<List<ShopResponse>>
     *
     * @GET("shops/{shopId}")
     * suspend fun getShopInfo(
     *     @Path("shopId") shopId: Int
     * ): ApiResponse<ShopResponse>
     */
}