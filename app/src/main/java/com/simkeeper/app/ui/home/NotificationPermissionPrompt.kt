package com.simkeeper.app.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.simkeeper.app.R
import com.simkeeper.app.ui.theme.SimKeeperTheme

/**
 * Android 13+: once the first SIM is saved, explain in one line why reminders need
 * notifications, then show the system dialog. Asked once; never again automatically.
 */
@Composable
fun NotificationPermissionPrompt(show: Boolean, onAsked: () -> Unit) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || !show) return
    val context = LocalContext.current
    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { onAsked() }
    if (granted) {
        LaunchedEffect(Unit) { onAsked() }
        return
    }
    AlertDialog(
        onDismissRequest = onAsked,
        icon = { Icon(painterResource(R.drawable.ic_notifications), contentDescription = null, tint = SimKeeperTheme.colors.primary) },
        text = { Text(stringResource(R.string.notifications_rationale), style = SimKeeperTheme.typography.bodyLarge) },
        confirmButton = {
            TextButton(onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                Text(stringResource(R.string.action_allow), style = SimKeeperTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onAsked) {
                Text(stringResource(R.string.action_not_now), style = SimKeeperTheme.typography.labelLarge)
            }
        },
        containerColor = SimKeeperTheme.colors.surface,
        shape = SimKeeperTheme.shapes.dialog,
    )
}
