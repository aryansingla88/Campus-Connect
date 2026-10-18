package com.example.campusconnect

import android.app.Application
import android.util.Log
import com.example.campusconnect.core.session.SessionManager
import com.example.campusconnect.core.location.TrackingModePreferences

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

        Log.d(
            "JWT_TEST",
            "Application started. Token exists: ${
                !SessionManager.getToken().isNullOrBlank()
            }"
        )
    }
}
