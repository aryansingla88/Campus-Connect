package com.example.campusconnect.feature.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.campusconnect.backend.api.AuthApi
import com.example.campusconnect.backend.request.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel(application: Application) : AndroidViewModel(application) {
// mutable stateflow is a type of state flow
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _warning = MutableStateFlow("")
    val warning: StateFlow<String> = _warning

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess
// whenever user types something then onUsernameChange is called and updates _username with that thing
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
        val savedUsername="pratham"
        val savedPassword="1234"

        if (name.isEmpty()) {
            _warning.value = "Username cannot be empty"
            return
        }

        if (pass.isEmpty()) {
            _warning.value = "Password cannot be empty"
            return
        }

        if (
            name==savedUsername &&
            pass == savedPassword
        ){
            val prefs=getApplication<Application>()
                .getSharedPreferences("CmpusApp",Context.MODE_PRIVATE)

            prefs.edit()
                .putInt("user_id",1)
                .apply()

            _loginSuccess.value = true
        } else {
            _warning.value = "Invalid username or password"
        }
    }
}