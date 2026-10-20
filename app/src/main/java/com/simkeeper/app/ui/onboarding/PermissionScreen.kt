package com.simkeeper.app.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.core.content.ContextCompat
import com.simkeeper.app.R
import com.simkeeper.app.ui.components.PrimaryButton
import com.simkeeper.app.ui.components.TextLink
import com.simkeeper.app.ui.theme.SimKeeperTheme

/**
 * Asks for READ_PHONE_STATE once, only when the user taps "Allow".
 * Denied goes to the manual add flow and is never asked again automatically.
 */
@Composable
fun PermissionRoute(onGranted: () -> Unit, onManual: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) onGranted() else onManual()
    }
    PermissionScreen(
        onAllow = {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED
            if (granted) onGranted() else launcher.launch(Manifest.permission.READ_PHONE_STATE)
        },
        onManual = onManual,
    )
}

@Composable
fun PermissionScreen(onAllow: () -> Unit, onManual: () -> Unit) {
    val spacing = SimKeeperTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SimKeeperTheme.colors.background)
            .safeDrawingPadding()
            .padding(horizontal = spacing.screen, vertical = spacing.lg),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = spacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            Text(
                text = stringResource(R.string.permission_title),
                style = SimKeeperTheme.typography.headlineMedium,
                color = SimKeeperTheme.colors.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.permission_body),
                style = SimKeeperTheme.typography.bodyLarge,
                color = SimKeeperTheme.colors.onSurfaceVariant,
            )
            PermissionInfoCard()
        }
        Spacer(Modifier.padding(top = spacing.lg))
        PrimaryButton(text = stringResource(R.string.action_allow), onClick = onAllow)
        TextLink(
            text = stringResource(R.string.permission_manual),
            onClick = onManual,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PermissionInfoCard() {
    Card(
        shape = SimKeeperTheme.shapes.card,
        colors = CardDefaults.cardColors(containerColor = SimKeeperTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = SimKeeperTheme.sizes.cardElevation),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SimKeeperTheme.sizes.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(SimKeeperTheme.sizes.monogram)
                    .background(SimKeeperTheme.colors.primaryContainer, SimKeeperTheme.shapes.monogram),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lock),
                    contentDescription = null,
                    tint = SimKeeperTheme.colors.primary,
                    modifier = Modifier.size(SimKeeperTheme.sizes.icon),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.xs)) {
                Text(
                    text = stringResource(R.string.permission_card_title),
                    style = SimKeeperTheme.typography.titleMedium,
                    color = SimKeeperTheme.colors.onSurface,
                )
                Text(
                    text = stringResource(R.string.permission_card_body),
                    style = SimKeeperTheme.typography.bodyMedium,
                    color = SimKeeperTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PermissionScreenPreview() {
    SimKeeperTheme { PermissionScreen(onAllow = {}, onManual = {}) }
}
