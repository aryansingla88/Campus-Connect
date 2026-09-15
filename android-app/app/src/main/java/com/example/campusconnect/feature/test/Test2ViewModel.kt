package com.example.campusconnect.feature.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.auth.data.remote.response.CurrentUserResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Test2UiState(
    val isLoading: Boolean = true,
    val user: CurrentUserResponse? = null,
    val error: String? = null
)

class Test2ViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(Test2UiState())
    val uiState: StateFlow<Test2UiState> = _uiState

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.authApi.getCurrentUser()

                if (response.isSuccessful) {

                    _uiState.value = Test2UiState(
                        isLoading = false,
                        user = response.body()?.data
                    )

                } else {

                    _uiState.value = Test2UiState(
                        isLoading = false,
                        error = "HTTP ${response.code()}"
                    )
                }

            } catch (e: Exception) {

                _uiState.value = Test2UiState(
                    isLoading = false,
                    error = e.message ?: "Failed to load user"
                )
            }
        }
    }
}