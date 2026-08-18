package com.example.campusconnect.feature.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.campusconnect.feature.auth.data.remote.response.CourseResponse
import com.example.campusconnect.feature.auth.data.repo.ApiCourseRepository
import com.example.campusconnect.feature.auth.data.repo.ApiAuthRepository
import com.example.campusconnect.feature.auth.domain.repository.AuthRepository
import com.example.campusconnect.feature.auth.data.repo.CourseRepository
import com.example.campusconnect.feature.auth.data.remote.request.RegisterRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import com.example.campusconnect.feature.auth.data.google.GoogleEmailVerificationResult
import com.example.campusconnect.feature.auth.data.google.GoogleEmailVerifier

class RegisterViewModel(application: Application)
    : AndroidViewModel(application) {

    private val courseRepository: CourseRepository =
        ApiCourseRepository()

    private val authRepository: AuthRepository =
        ApiAuthRepository()

    init {
        loadCourses()
    }

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

    private val _googleIdToken =
        MutableStateFlow<String?>(null)

    val googleIdToken:
            StateFlow<String?> =
        _googleIdToken

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

    private val _courses =
        MutableStateFlow<List<CourseResponse>>(emptyList())

    val courses: StateFlow<List<CourseResponse>> =
        _courses

    private val _selectedCourse =
        MutableStateFlow<CourseResponse?>(null)

    val selectedCourse: StateFlow<CourseResponse?> =
        _selectedCourse

    // admission year
    private val _admissionYear = MutableStateFlow("")
    val admissionYear: StateFlow<String> = _admissionYear

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
    // course loading
    // =========================

    private fun loadCourses() {

        viewModelScope.launch {

            courseRepository
                .getCourses()
                .onSuccess { courses ->

                    _courses.value = courses
                }
                .onFailure { error ->

                    _messageEvent.emit(
                        error.message
                            ?: "Failed to load courses"
                    )
                }
        }
    }

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
        if (filtered.length <= 12) {
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

    fun onCourseChange(value: CourseResponse) {
        _selectedCourse.value = value
    }

    fun onAdmissionYearChange(value: String) {
        _admissionYear.value = value
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
        val selectedCourse = _selectedCourse.value
        val admissionYear = admissionYear.value.trim()
        val gender = _gender.value.trim()
        val dob = _dob.value.trim()

        if (!_emailVerified.value) {

            viewModelScope.launch {
                _messageEvent.emit(
                    "Verify your email with Google first"
                )
            }

            return
        }

        val googleIdToken =
            _googleIdToken.value

        if (googleIdToken.isNullOrBlank()) {

            viewModelScope.launch {
                _messageEvent.emit(
                    "Google verification is required"
                )
            }

            return
        }

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

        if (selectedCourse == null) {
            viewModelScope.launch {

                _messageEvent.emit(
                    "Select course"
                )
            }

            return
        }

        if (admissionYear.isEmpty()) {
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

        val backendDob = try {
            val parts = dob.split("/")

            if (parts.size != 3) {
                throw IllegalArgumentException("Invalid DOB format")
            }

            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()

            String.format(
                "%04d-%02d-%02d",
                year,
                month,
                day
            )

        } catch (e: Exception) {

            viewModelScope.launch {
                _messageEvent.emit("Invalid DOB")
            }

            return
        }


        val request = RegisterRequest(
            username = username,
            email = email,
            password = password,
            fullName = realName,
            courseId = selectedCourse.courseId,
            admissionYear = admissionYear.toInt(),
            gender = gender,
            dob = backendDob,
            rollNumber = rollNumber,
            googleIdToken = googleIdToken
        )

        viewModelScope.launch {

            authRepository
                .register(request)
                .onSuccess {
                    _registerSuccess.value = true

                    _messageEvent.emit(
                        "Registration successful"
                    )
                }
                .onFailure { error ->
                    _messageEvent.emit(
                        error.message
                            ?: "Registration failed"
                    )
                }
        }
    }


    fun verifyGoogleEmail(context: Context) {

        val rollNumber =
            _rollNumber.value.trim()

        if (rollNumber.isEmpty()) {

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

        viewModelScope.launch {

            when (
                val result =
                    GoogleEmailVerifier.verify(context)
            ) {

                is GoogleEmailVerificationResult.Success -> {

                    val expectedEmail =
                        "$rollNumber@nitkkr.ac.in"

                    if (
                        result.email.equals(
                            expectedEmail,
                            ignoreCase = true
                        )
                    ) {

                        _googleIdToken.value =
                            result.idToken

                        _emailVerified.value =
                            true

                        _messageEvent.emit(
                            "Email verified successfully"
                        )

                    } else {

                        _googleIdToken.value =
                            null

                        _emailVerified.value =
                            false

                        _messageEvent.emit(
                            "Google account does not match $expectedEmail"
                        )
                    }
                }

                is GoogleEmailVerificationResult.Failure -> {

                    _googleIdToken.value = null

                    _emailVerified.value = false

                    _messageEvent.emit(
                        result.message
                    )
                }

                GoogleEmailVerificationResult.Cancelled -> {

                    _messageEvent.emit(
                        "Google verification cancelled"
                    )
                }
            }
        }
    }
}