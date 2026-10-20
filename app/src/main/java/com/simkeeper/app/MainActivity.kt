package com.simkeeper.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simkeeper.app.navigation.AppNavHost
import com.simkeeper.app.ui.theme.SimKeeperTheme
import com.simkeeper.app.ui.theme.isAppInDarkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { viewModel.state.value == null }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val start by viewModel.state.collectAsStateWithLifecycle()
            val state = start ?: return@setContent
            val dark = isAppInDarkTheme(state.themeMode)
            // Status and navigation bar icons follow the app theme, not only the system one.
            DisposableEffect(dark) {
                val style = if (dark) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose { }
            }
            SimKeeperTheme(themeMode = state.themeMode, dynamicColor = state.dynamicColor) {
                AppNavHost(onboardingDone = state.onboardingDone)
            }
        }
    }
}
