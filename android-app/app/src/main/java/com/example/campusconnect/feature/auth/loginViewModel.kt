package com.example.campusconnect.feature.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.campusconnect.backend.api.AuthApi
import com.example.campusconnect.backend.request.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _warning = MutableStateFlow("")
    val warning: StateFlow<String> = _warning

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    fun onUsernameChange(value: String) {
        _username.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun login() {

        val name = _username.value.trim()
        val pass = _password.value.trim()

        // Validation

        if (name.isEmpty()) {
            _warning.value = "Username cannot be empty"
            return
        }

        if (pass.isEmpty()) {
            _warning.value = "Password cannot be empty"
            return
        }

        // Fake request

        val request = LoginRequest(
            username = name,
            password = pass
        )

        // Fake backend response

        val response = AuthApi.loginUser(request)

        if (response.success) {

            val prefs = getApplication<Application>()
                .getSharedPreferences("CampusApp", Context.MODE_PRIVATE)

            prefs.edit()
                .putInt("user_id", response.user_id)
                .apply()

            _loginSuccess.value = true

        } else {
            _warning.value = response.message
        }
    }
}