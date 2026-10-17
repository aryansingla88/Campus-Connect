package com.example.campusconnect.core.session

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREF_NAME = "campus_connect_session"
    private const val KEY_ACCESS_TOKEN = "access_token"

    private lateinit var preferences: SharedPreferences

    fun initialize(context: Context) {
        preferences = context.applicationContext.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
    }

    fun saveToken(token: String) {
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .apply()
    }

    fun getToken(): String? {
        checkInitialized()
        return preferences.getString(KEY_ACCESS_TOKEN, null)
    }

    fun clearSession() {
        checkInitialized()
        preferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrBlank()
    }

    private fun checkInitialized() {
        check(::preferences.isInitialized) {
            "SessionManager.initialize(context) must be called first"
        }
    }
}