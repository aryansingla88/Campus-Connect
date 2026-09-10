package com.example.campusconnect.core.network

import com.example.campusconnect.BuildConfig
object ApiConfig {

    const val BASE_URL    = BuildConfig.API_BASE_URL
    const val TIMEOUT_SEC = 30L

    const val HEADER_AUTH         = "Authorization"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val HEADER_ACCEPT       = "Accept"
}