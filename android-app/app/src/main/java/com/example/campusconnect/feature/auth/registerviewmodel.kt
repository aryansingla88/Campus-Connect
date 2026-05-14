package com.example.campusconnect.feature.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

    // warning message

    private val _warning = MutableStateFlow("")
    val warning: StateFlow<String> = _warning

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

        _rollNumber.value = filtered
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

            _warning.value =
                "Username must be 6-20 chars with letters, numbers or underscore"

            return
        }

        if (rollNumber.isEmpty()) {

            _warning.value =
                "Enter roll number"

            return
        }

        if (password.isEmpty()) {
            _warning.value = "Password cannot be empty"
            return
        }
        val passwordRegex = Regex(
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,16}$"
        )
        if (!passwordRegex.matches(password)) {

            _warning.value =
                "Username must be 6-20 chars with letters, numbers or underscore"

            return
        }

        if (password.length !in 8..16) {
            _warning.value =
                "Password must be 8-16 characters"
            return
        }

        if (password != confirmPassword) {
            _warning.value = "Passwords do not match"
            return
        }

        if (realName.isEmpty()) {
            _warning.value = "Enter your real name"
            return
        }

        if (course.isEmpty()) {
            _warning.value = "Enter course"
            return
        }

        if (year.isEmpty()) {
            _warning.value = "Enter year"
            return
        }

        if (gender.isEmpty()) {
            _warning.value = "Enter gender"
            return
        }

        if (dob.isEmpty()) {
            _warning.value = "Enter DOB"
            return
        }

        // clear warning

        _warning.value = ""

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
}