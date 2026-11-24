package com.example.campusconnect.feature.metadata.hostels.remote

import com.example.campusconnect.core.network.ApiResponse
import retrofit2.Response
import retrofit2.http.GET

interface HostelApi {
    @GET("metadata/hostels")
    suspend fun getHostels(): Response<ApiResponse<List<HostelResponse>>>
}
