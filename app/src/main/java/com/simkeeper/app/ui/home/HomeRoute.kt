package com.simkeeper.app.ui.home

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simkeeper.app.ui.components.RechargeUndoEffect

@Composable
fun HomeRoute(
    onOpenSim: (Long) -> Unit,
    onRecharged: (Long) -> Unit,
    onSettings: () -> Unit,
    onAddSim: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pendingUndo by viewModel.pendingUndo.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }
    RechargeUndoEffect(pendingUndo, snackbarHostState, onUndo = viewModel::onUndo, onShown = viewModel::onUndoShown)
    NotificationPermissionPrompt(show = state.askNotifications, onAsked = viewModel::onNotificationsAsked)

    HomeScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        actions = HomeActions(
            onOpenSim = onOpenSim,
            onRecharged = onRecharged,
            onSettings = onSettings,
            onAddSim = onAddSim,
            onOpenBatterySettings = {
                // The app's own settings page: Battery → Unrestricted / No restrictions.
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                context.startActivity(intent)
            },
            onDismissBatteryGuide = viewModel::onDismissBatteryGuide,
        ),
    )
}
