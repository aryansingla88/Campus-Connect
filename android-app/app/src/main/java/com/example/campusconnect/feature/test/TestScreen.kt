package com.example.campusconnect.feature.test

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campusconnect.core.network.RetrofitClient

@Composable
fun TestScreen(
    onPosts: () -> Unit,
    onEvents: () -> Unit,
    onMap: () -> Unit,
    onProfile: () -> Unit,
    onSplash: (() -> Unit)? = null,
    onFormBuilder: () -> Unit,
    onLogout: (() -> Unit)? = null
) {

    var currentUser by remember {
        mutableStateOf<com.example.campusconnect.feature.auth.data.remote.response.CurrentUserResponse?>(null)
    }

    var userLoading by remember {
        mutableStateOf(false)
    }

    var userError by remember {
        mutableStateOf<String?>(null)
    }

    // test2 only
    LaunchedEffect(onLogout) {

        if (onLogout != null) {

            userLoading = true

            try {
                val response = RetrofitClient.authApi.getCurrentUser()

                if (response.isSuccessful) {

                    currentUser = response.body()?.data
                    userError = null

                } else {

                    userError = "Failed to load user: HTTP ${response.code()}"
                }

            } catch (e: Exception) {

                userError = e.message ?: "Failed to load user"

            } finally {

                userLoading = false
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(16.dp)
    ) {

        Button(onClick = onPosts) {
            Text("Posts")
        }

        Button(onClick = onEvents) {
            Text("Events")
        }

        Button(onClick = onMap) {
            Text("Map")
        }

        Button(onClick = onProfile) {
            Text("Profile")
        }

        Button(onClick = onFormBuilder) {
            Text("Form Builder")
        }

        if (onLogout != null) {
            Button(onClick = onLogout) {
                Text("Logout")
            }
        }

        if (onSplash != null) {
            Button(onClick = onSplash) {
                Text("APP FLOW")
            }
        }

        // ---------------------------------------------------------
        // /auth/me RESULT - TEST2 ONLY
        // ---------------------------------------------------------

        if (onLogout != null) {

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "CURRENT USER",
                style = MaterialTheme.typography.titleMedium
            )

            when {

                userLoading -> {
                    CircularProgressIndicator()
                }

                userError != null -> {
                    Text(
                        text = userError!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                currentUser != null -> {

                    Text("ID: ${currentUser!!.id}")
                    Text("Username: ${currentUser!!.username}")
                    Text("Email: ${currentUser!!.email}")
                    Text("Role: ${currentUser!!.role}")
                }
            }
        }
    }
}