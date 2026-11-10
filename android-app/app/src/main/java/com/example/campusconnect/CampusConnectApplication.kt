package com.example.campusconnect

import android.app.Application
import android.util.Log
import com.airbnb.lottie.LottieCompositionFactory
import com.example.campusconnect.core.location.TrackingModePreferences
import com.example.campusconnect.core.session.SessionManager

class CampusConnectApplication : Application() {

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
}
