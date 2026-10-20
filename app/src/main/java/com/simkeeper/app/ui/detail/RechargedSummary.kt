package com.simkeeper.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.simkeeper.app.R
import com.simkeeper.app.ui.components.SelectChip
import com.simkeeper.app.ui.components.carrierName
import com.simkeeper.app.ui.format.DateFormats
import com.simkeeper.app.ui.format.currentLocale
import com.simkeeper.app.ui.theme.SimKeeperTheme

/** "Recharged!" · "Your Airtel SIM is safe for 28 more days." · "New plan ends on 16 Nov 2026". */
@Composable
fun RechargedSummary(state: RechargedUiState) {
    val after = state.after ?: return
    val days = after.daysLeft.coerceAtLeast(0).toInt()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.sm),
        modifier = Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
    ) {
        Text(
            stringResource(R.string.recharged_title),
            style = SimKeeperTheme.typography.headlineLarge,
            color = SimKeeperTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            pluralStringResource(R.plurals.recharged_body, days, carrierName(after.sim), days),
            style = SimKeeperTheme.typography.bodyLarge,
            color = SimKeeperTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SimKeeperTheme.status.safe.container, SimKeeperTheme.shapes.banner)
            .padding(SimKeeperTheme.spacing.lg)
            .semantics(mergeDescendants = true) { },
    ) {
        Text(stringResource(R.string.recharged_new_end), style = SimKeeperTheme.typography.bodyMedium, color = SimKeeperTheme.colors.onSurfaceVariant)
        Text(
            DateFormats.date(after.deadlines.planEnd, currentLocale()),
            style = SimKeeperTheme.typography.titleLarge,
            color = SimKeeperTheme.colors.onSurface,
        )
    }
}

/** Optional: amount paid (₹) and plan length chips (prefilled with the last plan). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RechargedFields(state: RechargedUiState, onPlanDays: (Int) -> Unit, onAmount: (String) -> Unit) {
    val planDays = state.after?.sim?.planDays ?: return
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md)) {
        OutlinedTextField(
            value = state.amountText,
            onValueChange = onAmount,
            label = { Text(stringResource(R.string.recharged_amount)) },
            prefix = { Text(stringResource(R.string.recharged_amount_prefix)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(stringResource(R.string.recharged_plan_length), style = SimKeeperTheme.typography.titleMedium, color = SimKeeperTheme.colors.onSurface)
        val gap = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)
        FlowRow(horizontalArrangement = gap, verticalArrangement = gap) {
            state.planChoices.forEach { days ->
                SelectChip(
                    text = pluralStringResource(R.plurals.days_count, days, days),
                    selected = days == planDays,
                    onClick = { onPlanDays(days) },
                )
            }
        }
    }
}
