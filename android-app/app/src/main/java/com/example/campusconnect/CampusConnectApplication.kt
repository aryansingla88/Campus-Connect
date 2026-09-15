package com.example.campusconnect

import android.app.Application
import android.util.Log
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

        Log.d(
            "JWT_TEST",
            "Application started. Token exists: ${
                !SessionManager.getToken().isNullOrBlank()
            }"
        )
    }
}