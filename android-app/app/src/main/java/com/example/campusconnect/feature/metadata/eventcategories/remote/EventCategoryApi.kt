package com.example.campusconnect.feature.metadata.eventcategories.remote

import com.example.campusconnect.core.network.ApiResponse
import retrofit2.Response
import retrofit2.http.GET

interface EventCategoryApi {

    @GET("event-categories")
    suspend fun getEventCategories():
        Response<ApiResponse<List<EventCategoryResponse>>>
}
