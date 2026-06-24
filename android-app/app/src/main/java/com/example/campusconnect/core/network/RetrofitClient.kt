package com.example.campusconnect.core.network

import com.example.campusconnect.core.session.SessionManager
import com.example.campusconnect.feature.events.data.remote.EventsApi
import com.example.campusconnect.feature.profile.data.remote.ProfileApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.campusconnect.feature.posts.data.remote.PostsApi

object RetrofitClient {

    // Interceptors ---------------------------------------------
    private val authInterceptor = Interceptor { chain ->

        val builder = chain.request().newBuilder()

        SessionManager.getToken()?.takeIf { it.isNotBlank() }?.let { token ->
            builder.addHeader(
                ApiConfig.HEADER_AUTH,
                "Bearer $token"
            )
        }

        builder.addHeader(
            ApiConfig.HEADER_CONTENT_TYPE,
            "application/json"
        )

        builder.addHeader(
            ApiConfig.HEADER_ACCEPT,
            "application/json"
        )

        chain.proceed(builder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttp client ---------------------------------------------

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(ApiConfig.TIMEOUT_SEC, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.TIMEOUT_SEC,    TimeUnit.SECONDS)
        .writeTimeout(ApiConfig.TIMEOUT_SEC,   TimeUnit.SECONDS)
        .build()


    // Retrofit instance ---------------------------------------------
    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    // API instances ---------------------------------------------

    val profileApi: ProfileApi = retrofit.create(ProfileApi::class.java)
    // val authApi    : AuthApi    = retrofit.create(AuthApi::class.java)
    val eventsApi  : EventsApi = retrofit.create(EventsApi::class.java)
    val postsApi   : PostsApi   = retrofit.create(PostsApi::class.java)
    // val mapApi     : MapApi     = retrofit.create(MapApi::class.java)
}