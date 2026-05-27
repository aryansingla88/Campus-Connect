package com.example.campusconnect.feature.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.campusconnect.feature.auth.FakeAuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application)
    : AndroidViewModel(application) {

    // username
    /*
    Everything has a MutableStateFlow and a StateFlow because:
    private writable state+public read-only state
    Both point to SAME object.
     */
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    // email

    private val _rollNumber = MutableStateFlow("")
    val rollNumber: StateFlow<String> = _rollNumber
    private val _emailVerified =
        MutableStateFlow(false)

    val emailVerified:
            StateFlow<Boolean> =
        _emailVerified

    // password

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    // confirm password

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    // real name

    private val _realName = MutableStateFlow("")
    val realName: StateFlow<String> = _realName

    // course

    private val _course = MutableStateFlow("")
    val course: StateFlow<String> = _course

    // year

    private val _year = MutableStateFlow("")
    val year: StateFlow<String> = _year

    // gender

    private val _gender = MutableStateFlow("")
    val gender: StateFlow<String> = _gender

    // dob

    private val _dob = MutableStateFlow("")
    val dob: StateFlow<String> = _dob
    //warning
    private val _messageEvent =
        MutableSharedFlow<String>()

    val messageEvent =
        _messageEvent.asSharedFlow()

    // registration status

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess


    // =========================
    // field update functions
    // =========================

    fun onUsernameChange(value: String) {

        val filtered = value.filter {

            it.isLetterOrDigit() || it == '_'
        }

        if (filtered.length <= 20) {

            _username.value = filtered
        }
    }

    fun onRollNumberChange(value: String) {

        val filtered = value.filter {
            it.isDigit()
        }
        if(filtered.length<=12){
        _rollNumber.value = filtered
        }
    }

    fun onPasswordChange(value: String) {

        if (value.length <= 16) {

            _password.value = value
        }
    }

    fun onConfirmPasswordChange(value: String) {

        if (value.length <= 16) {

            _confirmPassword.value = value
        }
    }

    fun onRealNameChange(value: String) {

        val filtered = value

            .filter {

                it.isLetter() || it == ' '
            }

            .lowercase()

            .take(30)

        _realName.value = filtered
    }

    fun onCourseChange(value: String) {
        _course.value = value
    }

    fun onYearChange(value: String) {
        _year.value = value
    }

    fun onGenderChange(value: String) {
        _gender.value = value
    }

    fun onDobChange(value: String) {
        _dob.value = value
    }

    // =========================
    // register function
    // =========================

    fun register() {
        /*
        trim()
        is a String member function that:
        “removes spaces/newlines from the beginning and end of a string.”
         */

        val username = _username.value.trim()
        val rollNumber =
            _rollNumber.value.trim()

        val email =
            "$rollNumber@nitkkr.ac.in"
        val password = _password.value.trim()
        val confirmPassword = _confirmPassword.value.trim()

        val realName = _realName.value.trim()
        val course = _course.value.trim()
        val year = _year.value.trim()
        val gender = _gender.value.trim()
        val dob = _dob.value.trim()

        // empty checks

        val usernameRegex =
            Regex("^[A-Za-z0-9_]{6,20}$")

        if (!usernameRegex.matches(username)) {

            viewModelScope.launch {

                _messageEvent.emit(
                    "Username must be 6-20 chars with letters, numbers or underscore"
                )
            }

            return
        }

        if (rollNumber.length !in 6..12) {

            viewModelScope.launch {

                _messageEvent.emit(
                    "Roll number must be 6-12 digits"
                )
            }

            return
        }

        if (password.isEmpty()) {
            viewModelScope.launch {

                _messageEvent.emit(
                    "Password cannot be empty"
                )
            }
            return
        }
        val passwordRegex = Regex(
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,16}$"
        )
        if (!passwordRegex.matches(password)) {
            viewModelScope.launch {

                _messageEvent.emit(
                    "Password must contain uppercase, lowercase, number and special character. Should have 8-16 length."
                )
            }

            return
        }

        if (password.length !in 8..16) {
            viewModelScope.launch {

                _messageEvent.emit(
                    "Password must be 8-16 characters"
                )
            }
            return
        }

        if (password != confirmPassword) {
            viewModelScope.launch {

                _messageEvent.emit(
                    "Passwords do not match"
                )
            }
            return
        }

        if (realName.isEmpty()) {
            viewModelScope.launch {

                _messageEvent.emit(
                    "Enter your real name"
                )
            }
            return
        }

        if (course.isEmpty()) {
            viewModelScope.launch {

                _messageEvent.emit(
                    "Enter course"
                )
            }
            return
        }

        if (year.isEmpty()) {
            viewModelScope.launch {

                _messageEvent.emit(
                    "Enter year"
                )
            }
            return
        }

        if (gender.isEmpty()) {
            viewModelScope.launch {

                _messageEvent.emit(
                    "Enter gender"
                )
            }
            return
        }

        if (dob.isEmpty()) {
            viewModelScope.launch {

                _messageEvent.emit(
                    "Enter DOB"
                )
            }
            return
        }


        // temporary local storage

        val prefs = getApplication<Application>()
            .getSharedPreferences(
                "CampusApp",
                Context.MODE_PRIVATE
            )

        prefs.edit()

            .putString("registered_username", username)

            .putString("registered_email", email)

            .putString("registered_password", password)

            .putString("real_name", realName)

            .putString("course", course)

            .putString("year", year)

            .putString("gender", gender)

            .putString("dob", dob)

            .apply()

        // registration success

        _registerSuccess.value = true
    }
    fun sendOtp() {

        val rollNumber =
            _rollNumber.value.trim()

        if (rollNumber.isEmpty()) {
            // clear warning

            viewModelScope.launch {

                _messageEvent.emit(
                    "Enter roll number"
                )
            }

            return
        }
        if (rollNumber.length !in 6..12) {

            viewModelScope.launch {

                _messageEvent.emit(
                    "Roll number must be 6-12 digits"
                )
            }


            return
        }

        val email =
            "$rollNumber@nitkkr.ac.in"

        val success =
            FakeAuthRepository.sendOtp(email)

        if (success) {
            viewModelScope.launch {

                _messageEvent.emit(
                    "OTP sent successfully"
                )
            }
        }
    }
    fun verifyOtp(

        otp: String

    ): VerifyOtpResult {

        val email =

            "${_rollNumber.value}@nitkkr.ac.in"

        val result =

            FakeAuthRepository.verifyOtp(
                email,
                otp
            )

        if (result is VerifyOtpResult.Success) {

            _emailVerified.value = true

            viewModelScope.launch {

                _messageEvent.emit(
                    "Email verified successfully"
                )
            }
        }

        return result
    }
}