package com.example.campusconnect.feature.profile.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.campusconnect.feature.profile.data.fake.FakeProfileService

class ViewProfileViewModel(userId: String) : BaseProfileViewModel() {

    override var profile by mutableStateOf(FakeProfileService.getProfile(userId))

    companion object {
        fun factory(userId: String) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ViewProfileViewModel(userId) as T
            }
        }
    }
}