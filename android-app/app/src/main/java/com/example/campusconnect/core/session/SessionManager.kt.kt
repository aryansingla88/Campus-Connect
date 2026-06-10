package com.example.campusconnect.core.session

// TODO: replace with a proper token store (DataStore / EncryptedSharedPrefs)

object SessionManager {

    private var accessToken: String? = null

    fun saveToken(token: String) {
        accessToken = token
    }

    fun getToken(): String? {
        return accessToken
    }

    fun clearSession() {
        accessToken = null
    }

    fun isLoggedIn(): Boolean {
        return !accessToken.isNullOrBlank()
    }
}