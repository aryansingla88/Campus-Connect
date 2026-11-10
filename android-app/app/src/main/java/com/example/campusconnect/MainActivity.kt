package com.example.campusconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.campusconnect.core.ui.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called before super.onCreate() so Android's starting window
        // uses the branded splash theme instead of briefly showing a white window.
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            AppNavHost()
        }
    }
}
