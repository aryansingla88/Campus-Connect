package com.example.campusconnect.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.auth.data.remote.request.RequestPasswordResetRequest
import com.example.campusconnect.feature.auth.data.remote.request.ResetPasswordRequest
import com.example.campusconnect.feature.auth.data.remote.request.VerifyPasswordResetOtpRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PasswordResetViewModel : ViewModel() {

    enum class ResetStep {
        EMAIL,
        OTP,
        NEW_PASSWORD,
        SUCCESS,
        CLOSED
    }

    private val _currentStep =
        MutableStateFlow(ResetStep.CLOSED)

    val currentStep: StateFlow<ResetStep> =
        _currentStep


    private val _email =
        MutableStateFlow("")

    val email: StateFlow<String> =
        _email


    private val _otp =
        MutableStateFlow("")

    val otp: StateFlow<String> =
        _otp


    private val _newPassword =
        MutableStateFlow("")

    val newPassword: StateFlow<String> =
        _newPassword


    private val _confirmPassword =
        MutableStateFlow("")

    val confirmPassword: StateFlow<String> =
        _confirmPassword


    private val _resetToken =
        MutableStateFlow<String?>(null)


    private val _warning =
        MutableStateFlow("")

    val warning: StateFlow<String> =
        _warning


    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading


    fun startPasswordReset() {

        clearAll()

        _currentStep.value =
            ResetStep.EMAIL
    }


    fun closePasswordReset() {

        clearAll()

        _currentStep.value =
            ResetStep.CLOSED
    }


    fun onEmailChange(value: String) {

        _email.value = value
    }


    fun onOtpChange(value: String) {

        if (value.length <= 6 && value.all { it.isDigit() }) {
            _otp.value = value
        }
    }


    fun onNewPasswordChange(value: String) {

        _newPassword.value = value
    }


    fun onConfirmPasswordChange(value: String) {

        _confirmPassword.value = value
    }


    fun requestOtp() {

        val enteredEmail =
            _email.value.trim()


        if (enteredEmail.isEmpty()) {

            _warning.value =
                "Email cannot be empty"

            return
        }


        viewModelScope.launch {

            _isLoading.value = true
            _warning.value = ""

            try {

                val response =
                    RetrofitClient.authApi
                        .requestPasswordReset(

                            RequestPasswordResetRequest(
                                email = enteredEmail
                            )
                        )


                if (
                    response.isSuccessful &&
                    response.body()?.success == true
                ) {

                    _currentStep.value =
                        ResetStep.OTP

                } else {

                    _warning.value =
                        response.body()?.message
                            ?: "Failed to send OTP"
                }

            } catch (e: Exception) {

                _warning.value =
                    e.message
                        ?: "Unable to connect to server"

            } finally {

                _isLoading.value = false
            }
        }
    }


    fun verifyOtp() {

        val enteredOtp =
            _otp.value


        if (enteredOtp.length != 6) {

            _warning.value =
                "OTP must contain 6 digits"

            return
        }


        viewModelScope.launch {

            _isLoading.value = true
            _warning.value = ""

            try {

                val response =
                    RetrofitClient.authApi
                        .verifyPasswordResetOtp(

                            VerifyPasswordResetOtpRequest(
                                email = _email.value.trim(),
                                otp = enteredOtp
                            )
                        )


                if (
                    response.isSuccessful &&
                    response.body()?.success == true &&
                    response.body()?.data != null
                ) {

                    _resetToken.value =
                        response.body()!!
                            .data!!
                            .resetToken


                    _currentStep.value =
                        ResetStep.NEW_PASSWORD

                } else {

                    _warning.value =
                        response.body()?.message
                            ?: "OTP verification failed"
                }

            } catch (e: Exception) {

                _warning.value =
                    e.message
                        ?: "Unable to connect to server"

            } finally {

                _isLoading.value = false
            }
        }
    }


    fun resetPassword() {

        val password =
            _newPassword.value

        val confirmPassword =
            _confirmPassword.value

        val token =
            _resetToken.value


        if (password.length < 8) {

            _warning.value =
                "Password must be at least 8 characters long"

            return
        }


        if (password != confirmPassword) {

            _warning.value =
                "Passwords do not match"

            return
        }


        if (token == null) {

            _warning.value =
                "Invalid password reset session"

            return
        }


        viewModelScope.launch {

            _isLoading.value = true
            _warning.value = ""

            try {

                val response =
                    RetrofitClient.authApi
                        .resetPassword(

                            ResetPasswordRequest(
                                resetToken = token,
                                newPassword = password
                            )
                        )


                if (
                    response.isSuccessful &&
                    response.body()?.success == true
                ) {

                    _currentStep.value =
                        ResetStep.SUCCESS

                } else {

                    _warning.value =
                        response.body()?.message
                            ?: "Failed to reset password"
                }

            } catch (e: Exception) {

                _warning.value =
                    e.message
                        ?: "Unable to connect to server"

            } finally {

                _isLoading.value = false
            }
        }
    }


    fun clearWarning() {

        _warning.value = ""
    }


    private fun clearAll() {

        _email.value = ""
        _otp.value = ""
        _newPassword.value = ""
        _confirmPassword.value = ""
        _resetToken.value = null
        _warning.value = ""
        _isLoading.value = false
    }
}