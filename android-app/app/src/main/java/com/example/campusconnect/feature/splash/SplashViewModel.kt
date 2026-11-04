package com.example.campusconnect.feature.splash

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import com.example.campusconnect.core.session.SessionManager
import com.example.campusconnect.core.session.AppRole
import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.metadata.courses.CourseRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

sealed class SplashDestination {
    object Home : SplashDestination()
    object Login : SplashDestination()
    object OfflineFallback : SplashDestination()
}

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private var splashStarted = false

    private val _destination =
        MutableStateFlow<SplashDestination?>(null)

    val destination: StateFlow<SplashDestination?> =
        _destination

    fun startSplash() {
        if (splashStarted) return
        splashStarted = true

        viewModelScope.launch {
            if (!SessionManager.isLoggedIn()) {
                _destination.value = SplashDestination.Login
                return@launch
            }

            val response = try {
                RetrofitClient.authApi.getCurrentUser()
            } catch (_: IOException) {
                _destination.value = SplashDestination.OfflineFallback
                return@launch
            } catch (_: Exception) {
                _destination.value = SplashDestination.Login
                return@launch
            }

            if (!response.isSuccessful && response.code() >= 500) {
                _destination.value = SplashDestination.OfflineFallback
                return@launch
            }

            val restoredRole = if (response.isSuccessful && response.body()?.success == true) {
                AppRole.parse(response.body()?.data?.role)
            } else {
                null
            }

            if (restoredRole == null) {
                SessionManager.clearSession()
                _destination.value = SplashDestination.Login
            } else {
                SessionManager.updateAccountRole(restoredRole)
                _destination.value = SplashDestination.Home
            }
        }

        // Refresh public course metadata without holding up the startup route.
        viewModelScope.launch {
            runCatching {
                CourseRepositoryProvider.getRepository(
                    getApplication<Application>().applicationContext
                ).ensureCoursesCached()
            }
        }
    }
}
