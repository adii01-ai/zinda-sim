package com.simkeeper.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.domain.deadlinesOf
import com.simkeeper.app.domain.usecase.RechargeReceipt
import com.simkeeper.app.ui.format.DateFormats
import com.simkeeper.app.ui.format.currentLocale
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.theme.DarkStatusColors
import com.simkeeper.app.ui.theme.LightStatusColors
import com.simkeeper.app.ui.theme.SimKeeperTheme

/** inverseSurface, 18 dp radius, check icon in the safe colour, "Undo" action. */
@Composable
fun UndoSnackbar(data: SnackbarData) {
    // The snackbar sits on the inverse surface, so the check uses the other theme's safe colour.
    val check = if (SimKeeperTheme.isDark) LightStatusColors.safe.content else DarkStatusColors.safe.content
    Snackbar(
        modifier = Modifier.padding(SimKeeperTheme.spacing.md),
        shape = SimKeeperTheme.shapes.snackbar,
        containerColor = SimKeeperTheme.colors.inverseSurface,
        contentColor = SimKeeperTheme.colors.inverseOnSurface,
        action = data.visuals.actionLabel?.let { label ->
            {
                TextButton(onClick = { data.performAction() }) {
                    Text(label, style = SimKeeperTheme.typography.labelLarge, color = SimKeeperTheme.colors.inversePrimary)
                }
            }
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)) {
            Icon(painterResource(R.drawable.ic_check_circle), contentDescription = null, tint = check, modifier = Modifier.size(SimKeeperTheme.sizes.iconSmall))
            Text(data.visuals.message, style = SimKeeperTheme.typography.bodyMedium)
        }
    }
}

/** Shows "Bank SIM is safe till 16 Nov · Undo" once for a pending recharge. */
@Composable
fun RechargeUndoEffect(
    pending: RechargeReceipt?,
    hostState: SnackbarHostState,
    onUndo: (RechargeReceipt) -> Unit,
    onShown: (RechargeReceipt) -> Unit,
) {
    if (pending == null) return
    val safeTill = DateFormats.dayMonth(deadlinesOf(pending.updated).effective, currentLocale())
    val message = stringResource(R.string.home_safe_till, pending.updated.name, safeTill)
    val undo = stringResource(R.string.action_undo)
    LaunchedEffect(pending) {
        val result = hostState.showSnackbar(message = message, actionLabel = undo, duration = SnackbarDuration.Long)
        if (result == SnackbarResult.ActionPerformed) onUndo(pending)
        onShown(pending)
    }
}

@PreviewLightDark
@Composable
private fun UndoSnackbarPreview() {
    PreviewSurface {
        Snackbar(
            shape = SimKeeperTheme.shapes.snackbar,
            containerColor = SimKeeperTheme.colors.inverseSurface,
            contentColor = SimKeeperTheme.colors.inverseOnSurface,
        ) { Text(stringResource(R.string.home_safe_till, "Bank SIM", "16 Nov")) }
    }
}
