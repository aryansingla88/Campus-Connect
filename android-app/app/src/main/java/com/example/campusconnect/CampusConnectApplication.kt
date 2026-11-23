package com.example.campusconnect

import android.app.Application
import android.util.Log
import com.airbnb.lottie.LottieCompositionFactory
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.campusconnect.core.location.TrackingModePreferences
import com.example.campusconnect.core.network.ApiConfig
import com.example.campusconnect.core.session.SessionManager
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient

class CampusConnectApplication : Application(), ImageLoaderFactory {

    companion object {
        lateinit var instance: CampusConnectApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        SessionManager.initialize(this)
        TrackingModePreferences.initialize(this)

        // Start parsing the exact raw resource used by Compose while app startup runs.
        // Lottie's raw-resource composition cache lets rememberLottieComposition reuse it.
        LottieCompositionFactory
            .fromRawRes(this, R.raw.campus_splash)
            .addListener {
                Log.d("SPLASH_PRELOAD", "Campus splash composition is ready")
            }
            .addFailureListener { error ->
                Log.e("SPLASH_PRELOAD", "Campus splash preload failed", error)
            }

        Log.d(
            "JWT_TEST",
            "Application started. Token exists: ${!SessionManager.getToken().isNullOrBlank()}"
        )
    }

    override fun newImageLoader(): ImageLoader {
        val backendBaseUrl = ApiConfig.BASE_URL.toHttpUrlOrNull()
        val imageHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val requestUrl = request.url
                val targetsBackend = backendBaseUrl?.let { baseUrl ->
                    requestUrl.scheme == baseUrl.scheme &&
                        requestUrl.host == baseUrl.host &&
                        requestUrl.port == baseUrl.port
                } == true

                val token = if (
                    targetsBackend && request.header(ApiConfig.HEADER_AUTH) == null
                ) {
                    SessionManager.getToken()?.takeIf(String::isNotBlank)
                } else {
                    null
                }

                val authenticatedRequest = token?.let {
                    request.newBuilder()
                        .header(ApiConfig.HEADER_AUTH, "Bearer $it")
                        .build()
                } ?: request

                chain.proceed(authenticatedRequest)
            }
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(imageHttpClient)
            .build()
    }
}
