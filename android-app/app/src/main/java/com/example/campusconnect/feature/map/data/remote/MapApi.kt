package com.example.campusconnect.feature.map.data.remote

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.map.data.remote.request.EventRegReq
import com.example.campusconnect.feature.map.data.remote.response.CategoryRes
import com.example.campusconnect.feature.map.data.remote.response.EventHostResponse
import com.example.campusconnect.feature.map.data.remote.response.EventResponse
import com.example.campusconnect.feature.map.data.remote.response.EventPreviewRes
import com.example.campusconnect.feature.map.data.remote.response.PoiRes
import com.example.campusconnect.feature.map.data.remote.response.ShopResponse
import com.example.campusconnect.feature.map.data.remote.response.UserMapRes
import com.example.campusconnect.feature.map.data.remote.response.UserPreviewRes
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MapApi {

    // Domain List Endpoints -----------------------------------------------------

    // Matches UserPresenceController @GetMapping("/presence")
    @GET("presence")
    suspend fun getVisibleUsers(): ApiResponse<List<UserMapRes>>

    // Matches PoiController @GetMapping("/poi")
    @GET("poi")
    suspend fun getPois(): ApiResponse<List<PoiRes>>

    // Matches EventController @GetMapping("/events")
    @GET("events")
    suspend fun getEvents(): ApiResponse<List<EventResponse>>

    // Mock/Fake Fallback (Backend mein shop module nahi hai)
    @GET("shops")
    suspend fun getShops(): ApiResponse<List<ShopResponse>>


    // User Marker & Preview Card Endpoints -------------------------------------

    // Matches UserPresenceController @GetMapping("/presence/users/{userId}/preview")
    @GET("presence/users/{userId}/preview")
    suspend fun getUserProfile(
        @Path("userId") userId: Int
    ): ApiResponse<UserPreviewRes>

    // Matches ConnectionController @PostMapping("/api/v1/users/{userId}/connections/request")
    @POST("api/v1/users/{userId}/connections/request")
    suspend fun sendConnectionRequest(
        @Path("userId") userId: Int
    ): ApiResponse<Unit>


    // POI Marker Card ----------------------------------------------------------

    // Matches PoiController @GetMapping("/poi/{id}")
    @GET("poi/{poiId}")
    suspend fun getPoiInfo(
        @Path("poiId") poiId: Int
    ): ApiResponse<PoiRes>


    // Event Marker Card --------------------------------------------------------

    // Matches MapEventController @GetMapping("/map/events/{eventId}/preview")
    @GET("map/events/{eventId}/preview")
    suspend fun getEventPreview(
        @Path("eventId") eventId: Int
    ): ApiResponse<EventPreviewRes>

    // Matches EventController @GetMapping("/events/{eventId}")
    @GET("events/{eventId}")
    suspend fun getEventInfo(
        @Path("eventId") eventId: Int
    ): ApiResponse<EventResponse>

    // Mock/Fake Fallback
    @GET("events/{eventId}/hosts")
    suspend fun getEventHosts(
        @Path("eventId") eventId: Int
    ): ApiResponse<List<EventHostResponse>>

    // Mock/Fake Fallback
    @POST("events/{eventId}/register")
    suspend fun registerEvent(
        @Path("eventId") eventId: Int,
        @Body request: EventRegReq = EventRegReq()
    ): ApiResponse<Unit>

    // Mock/Fake Fallback
    @POST("events/{eventId}/reminders")
    suspend fun enableEventReminder(
        @Path("eventId") eventId: Int
    ): ApiResponse<Unit>

    // Mock/Fake Fallback
    @DELETE("events/{eventId}/reminders")
    suspend fun disableEventReminder(
        @Path("eventId") eventId: Int
    ): ApiResponse<Unit>


    // Categories ---------------------------------------------------------------

    // Mock/Fake Fallback
    @GET("event-categories")
    suspend fun getEventCategories(): ApiResponse<List<CategoryRes>>

    // Mock/Fake Fallback (Backend mein shop module nahi hai)
    @GET("shop-categories")
    suspend fun getShopCategories(): ApiResponse<List<CategoryRes>>


    // Shop Marker Card ---------------------------------------------------------

    // Mock/Fake Fallback (Backend mein shop module nahi hai)
    @GET("shops/{shopId}")
    suspend fun getShopInfo(
        @Path("shopId") shopId: Int
    ): ApiResponse<ShopResponse>
}