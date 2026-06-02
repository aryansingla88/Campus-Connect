package com.example.campusconnect.core.network

import com.example.campusconnect.feature.profile.data.remote.ProfileApi
import okhttp3.OkHttpClient
import okhttp3.logging.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import android.R.attr.level


object RetrofitClient {

    // Set by SessionManager after login
    // TODO: replace with a proper token store (DataStore / EncryptedSharedPrefs)
    private var authToken: String = ""

    fun setAuthToken(token: String) {
        authToken = token
    }

    fun clearAuthToken() {
        authToken = ""
    }

    // ---------------------------------------------------------------------------
    // Interceptors
    // ---------------------------------------------------------------------------

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader(ApiConfig.HEADER_AUTH,         "Bearer $authToken")
            .addHeader(ApiConfig.HEADER_CONTENT_TYPE, "application/json")
            .addHeader(ApiConfig.HEADER_ACCEPT,       "application/json")
            .build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // TODO: set to NONE in release builds via BuildConfig.DEBUG
        level = HttpLoggingInterceptor.Level.BODY
    }

    // ---------------------------------------------------------------------------
    // OkHttp client
    // ---------------------------------------------------------------------------

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(ApiConfig.TIMEOUT_SEC, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.TIMEOUT_SEC,    TimeUnit.SECONDS)
        .writeTimeout(ApiConfig.TIMEOUT_SEC,   TimeUnit.SECONDS)
        .build()

    // ---------------------------------------------------------------------------
    // Retrofit instance
    // ---------------------------------------------------------------------------

    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // ---------------------------------------------------------------------------
    // API instances — register new ones here as features are added
    // ---------------------------------------------------------------------------

    val profileApi: ProfileApi = retrofit.create(ProfileApi::class.java)

    // TODO: val authApi    : AuthApi    = retrofit.create(AuthApi::class.java)
    // TODO: val eventsApi  : EventsApi  = retrofit.create(EventsApi::class.java)
    // TODO: val postsApi   : PostsApi   = retrofit.create(PostsApi::class.java)
    // TODO: val mapApi     : MapApi     = retrofit.create(MapApi::class.java)
}