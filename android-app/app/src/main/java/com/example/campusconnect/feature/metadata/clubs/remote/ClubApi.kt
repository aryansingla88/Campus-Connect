package com.example.campusconnect.feature.metadata.clubs.remote

import com.example.campusconnect.core.network.ApiResponse
import retrofit2.Response
import retrofit2.http.GET

interface ClubApi {

    @GET("metadata/clubs")
    suspend fun getClubs():
            Response<ApiResponse<List<ClubResponse>>>
}
