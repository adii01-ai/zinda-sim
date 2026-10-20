package com.simkeeper.app.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simkeeper.app.R
import com.simkeeper.app.data.telephony.DetectedSim
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.ui.components.PrimaryButton
import com.simkeeper.app.ui.components.TextLink
import com.simkeeper.app.ui.preview.SampleData
import com.simkeeper.app.ui.theme.SimKeeperTheme

@Composable
fun SimsDetectedRoute(
    onBack: () -> Unit,
    onAddFamily: () -> Unit,
    onAddByHand: () -> Unit,
    onFinished: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.finished) { if (state.finished) onFinished() }
    val names = state.sims.map { resolvedName(it) }
    SimsDetectedScreen(
        state = state,
        onBack = onBack,
        onChoice = viewModel::onNameChoice,
        onCustomName = viewModel::onCustomName,
        onAddFamily = onAddFamily,
        onAddByHand = onAddByHand,
        onContinue = { viewModel.onContinue(names) },
    )
    val index = state.sheetIndex
    if (index != null) {
        val sim = state.sims[index].detected
        SetValiditySheet(
            header = ValidityHeader(names[index], sim.carrier, sim.carrierName, sim.slot, step = index + 1, stepCount = state.sims.size),
            form = state.form,
            today = state.today,
            saveLabel = stringResource(if (index == state.sims.lastIndex) R.string.action_save_and_finish else R.string.action_save),
            onChange = viewModel::onFormChange,
            onSave = viewModel::onSave,
            onDismiss = viewModel::onSheetDismiss,
        )
    }
}

@Composable
fun SimsDetectedScreen(
    state: SetupUiState,
    onBack: () -> Unit,
    onChoice: (Int, NameChoice) -> Unit,
    onCustomName: (Int, String) -> Unit,
    onAddFamily: () -> Unit,
    onAddByHand: () -> Unit,
    onContinue: () -> Unit,
) {
    val spacing = SimKeeperTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SimKeeperTheme.colors.background)
            .safeDrawingPadding(),
    ) {
        IconButton(onClick = onBack) {
            Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.action_back))
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = spacing.screen, vertical = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            item { DetectedHeader(state) }
            itemsIndexed(state.sims.drop(state.savedCount), key = { _, s -> s.detected.subscriptionId }) { i, sim ->
                val index = i + state.savedCount
                DetectedSimCard(sim, onChoice = { onChoice(index, it) }, onCustomName = { onCustomName(index, it) })
            }
            item { DashedButton(stringResource(R.string.track_family_sim), onClick = onAddFamily) }
        }
        Column(Modifier.padding(horizontal = spacing.screen, vertical = spacing.sm)) {
            PrimaryButton(stringResource(R.string.action_continue), onClick = onContinue, enabled = state.canContinue)
            TextLink(stringResource(R.string.add_sim_by_hand), onClick = onAddByHand, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun DetectedHeader(state: SetupUiState) {
    val spacing = SimKeeperTheme.spacing
    val count = state.sims.size
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm), modifier = Modifier.padding(bottom = spacing.sm)) {
        if (!state.loading && count > 0) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Icon(
                    painterResource(R.drawable.ic_check_circle),
                    contentDescription = null,
                    tint = SimKeeperTheme.status.safe.content,
                    modifier = Modifier.size(SimKeeperTheme.sizes.iconSmall),
                )
                Text(
                    pluralStringResource(R.plurals.sims_detected, count, count),
                    style = SimKeeperTheme.typography.bodyMedium,
                    color = SimKeeperTheme.status.safe.content,
                )
            }
        }
        val empty = !state.loading && count == 0
        Text(
            text = stringResource(if (empty) R.string.detected_none_title else R.string.detected_title),
            style = SimKeeperTheme.typography.headlineMedium,
            color = SimKeeperTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(if (empty) R.string.detected_none_body else R.string.detected_subtitle),
            style = SimKeeperTheme.typography.bodyLarge,
            color = SimKeeperTheme.colors.onSurfaceVariant,
        )
    }
}

@PreviewLightDark
@Composable
private fun SimsDetectedScreenPreview() {
    SimKeeperTheme {
        SimsDetectedScreen(
            state = SetupUiState(
                loading = false,
                today = SampleData.today,
                sims = listOf(
                    DetectedSimUi(DetectedSim("Jio 4G", Carrier.JIO, 1, 1), NameChoice.DAILY),
                    DetectedSimUi(DetectedSim("Airtel", Carrier.AIRTEL, 2, 2), NameChoice.BANK),
                ),
            ),
            onBack = {}, onChoice = { _, _ -> }, onCustomName = { _, _ -> }, onAddFamily = {}, onAddByHand = {}, onContinue = {},
        )
    }
}
