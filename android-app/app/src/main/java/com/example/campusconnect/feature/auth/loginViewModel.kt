package com.example.campusconnect.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.core.session.SessionManager
import com.example.campusconnect.feature.auth.data.remote.request.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    // mutable stateflow is a type of state flow
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _warning = MutableStateFlow("")
    val warning: StateFlow<String> = _warning

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    // whenever user types something then onUsernameChange is called and updates _username with that thing
    fun onUsernameChange(value: String) {
        _username.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun login() {

        val name = _username.value.trim()
        val pass = _password.value

        // Validation
        if (name.isEmpty()) {
            _warning.value = "Username cannot be empty"
            return
        }

        if (pass.isEmpty()) {
            _warning.value = "Password cannot be empty"
            return
        }

        viewModelScope.launch {

            _isLoading.value = true
            _warning.value = ""

            try {

                val response = RetrofitClient.authApi.login(
                    LoginRequest(
                        identifier = name,
                        password = pass
                    )
                )

                if (
                    response.isSuccessful &&
                    response.body()?.success == true &&
                    response.body()?.data != null
                ) {

                    val authResponse = response.body()!!.data!!

                    SessionManager.saveToken(
                        authResponse.token
                    )

                    _loginSuccess.value = true

                } else {

                    _warning.value =
                        response.body()?.message
                            ?: "Login failed"
                }

            } catch (e: Exception) {

                _warning.value =
                    e.message ?: "Unable to connect to server"

            } finally {

                _isLoading.value = false
            }
        }
    }
}