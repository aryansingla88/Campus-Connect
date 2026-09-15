package com.example.campusconnect

import android.app.Application
import android.util.Log
import com.example.campusconnect.core.session.SessionManager

class CampusConnectApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        SessionManager.initialize(this)

        Log.d(
            "JWT_TEST",
            "Application started. Token exists: ${
                !SessionManager.getToken().isNullOrBlank()
            }"
        )
    }
}