package com.example.campusconnect.feature.splash

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import com.example.campusconnect.core.session.SessionManager
import com.example.campusconnect.feature.metadata.courses.CourseRepositoryProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SplashDestination {
    object Home : SplashDestination()
    object Login : SplashDestination()
}

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val _destination =
        MutableStateFlow<SplashDestination?>(null)

    val destination: StateFlow<SplashDestination?> =
        _destination

    fun startSplash() {

        viewModelScope.launch {

            val courseRepository =
                CourseRepositoryProvider.getRepository(
                    getApplication<Application>().applicationContext
                )

            courseRepository.ensureCoursesCached()

            // Keep splash/loading time
            delay(2000)

            // Check persisted JWT
            _destination.value =
                if (SessionManager.isLoggedIn()) {
                    SplashDestination.Home
                } else {
                    SplashDestination.Login
                }
        }
    }
}