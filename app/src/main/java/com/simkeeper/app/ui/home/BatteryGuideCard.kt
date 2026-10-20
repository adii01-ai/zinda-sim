package com.simkeeper.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.ui.components.TextLink
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.theme.SimKeeperTheme

/** One-time, dismissible guide for phones that stop background work (Xiaomi, Oppo, Vivo, Realme, OnePlus). */
@Composable
fun BatteryGuideCard(manufacturer: String, onOpenSettings: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = SimKeeperTheme.spacing
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = SimKeeperTheme.shapes.card,
        colors = CardDefaults.cardColors(containerColor = SimKeeperTheme.colors.primaryContainer),
    ) {
        Column(Modifier.padding(SimKeeperTheme.sizes.cardPadding), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                Icon(
                    painterResource(R.drawable.ic_battery_alert),
                    contentDescription = null,
                    tint = SimKeeperTheme.colors.onPrimaryContainer,
                    modifier = Modifier.size(SimKeeperTheme.sizes.iconSmall),
                )
                Text(
                    stringResource(R.string.battery_title),
                    style = SimKeeperTheme.typography.titleMedium,
                    color = SimKeeperTheme.colors.onPrimaryContainer,
                )
            }
            Text(
                stringResource(R.string.battery_body, manufacturer.replaceFirstChar { it.uppercase() }),
                style = SimKeeperTheme.typography.bodyMedium,
                color = SimKeeperTheme.colors.onPrimaryContainer,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                TextLink(stringResource(R.string.battery_action), onClick = onOpenSettings)
                TextLink(stringResource(R.string.battery_dismiss), onClick = onDismiss)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun BatteryGuideCardPreview() {
    PreviewSurface { BatteryGuideCard("Xiaomi", onOpenSettings = {}, onDismiss = {}) }
}
