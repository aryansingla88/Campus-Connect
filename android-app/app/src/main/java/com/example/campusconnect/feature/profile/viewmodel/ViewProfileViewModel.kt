package com.example.campusconnect.feature.profile.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.campusconnect.feature.profile.model.PublicUserProfile
import com.example.campusconnect.feature.profile.model.ProfileStats

class ViewProfileViewModel(private val userId: String) : BaseProfileViewModel() {
    override var profile by mutableStateOf(PublicUserProfile())

    var stats by mutableStateOf(ProfileStats())

    init {
        loadData()
        viewModelScope.launch {

            repository.getProfile(userId)
                .getOrNull()
                ?.let {
                    profile = it
                }

            repository.getUserStats(userId)
                .getOrNull()
                ?.let {
                    stats = it
                }
        }
    }

    companion object {
        fun factory(userId: String) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {

                @Suppress("UNCHECKED_CAST")
                return ViewProfileViewModel(userId) as T
            }
        }
    }
}