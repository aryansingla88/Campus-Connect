package com.example.campusconnect.feature.splash

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SplashDestination {
    object Main : SplashDestination()
    object Login : SplashDestination()
}

/*
Normal ViewModel:

does NOT provide application context

AndroidViewModel:

does provide application context
 Thats why we use AndroidViewModel in below line*/
class SplashViewModel(application: Application) : AndroidViewModel(application) {
    // MutableStateFlow is like an observable state container
    //here MutableStateFlow<SplashDestination?> means the mutablestateflow here stores SplashDestination
    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination

    fun startSplash() {
        viewModelScope.launch {

            // total delay ≈ animation + text delay
            delay(2000)

            /*val prefs = getApplication<Application>()
                .getSharedPreferences("CampusApp", Context.MODE_PRIVATE)

            val userId = prefs.getInt("user_id", -1)*/

            _destination.value = SplashDestination.Login
        }
    }
}