package com.example.campusconnect.core.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {

    private const val PREF_NAME = "campus_connect_session"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_ACCOUNT_ROLE = "account_role"

    private lateinit var preferences: SharedPreferences
    private val _sessionVersion = MutableStateFlow(0L)
    val sessionVersion: StateFlow<Long> = _sessionVersion.asStateFlow()
    private val _accountRole = MutableStateFlow<AppRole?>(null)
    val accountRole: StateFlow<AppRole?> = _accountRole.asStateFlow()

    fun initialize(context: Context) {
        preferences = context.applicationContext.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
        _accountRole.value = AppRole.parse(preferences.getString(KEY_ACCOUNT_ROLE, null))
    }

    fun saveToken(token: String, role: AppRole) {
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .putString(KEY_ACCOUNT_ROLE, role.name)
            .apply()
        _accountRole.value = role
        _sessionVersion.value += 1
    }

    /** Refresh the cached role only from a successful authenticated /auth/me response. */
    fun updateAccountRole(role: AppRole) {
        checkInitialized()
        preferences.edit().putString(KEY_ACCOUNT_ROLE, role.name).apply()
        _accountRole.value = role
        _sessionVersion.value += 1
    }

    fun getToken(): String? {
        checkInitialized()
        return preferences.getString(KEY_ACCESS_TOKEN, null)
    }

    /** Reads the backend user ID from the subject of the saved authenticated JWT. */
    fun getAuthenticatedUserId(): Int? = runCatching {
        val payload = getToken()
            ?.split('.')
            ?.getOrNull(1)
            ?.let { Base64.decode(it, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP) }
            ?.toString(Charsets.UTF_8)
            ?: return null
        JsonParser()
            .parse(payload)
            .asJsonObject
            .get("sub")
            ?.asString
            ?.toIntOrNull()
    }.getOrNull()

    fun clearSession() {
        checkInitialized()
        preferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_ACCOUNT_ROLE)
            .apply()
        _accountRole.value = null
        _sessionVersion.value += 1
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
