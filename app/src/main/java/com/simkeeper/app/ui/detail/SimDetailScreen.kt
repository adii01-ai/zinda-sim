package com.simkeeper.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.domain.model.PlanOffer
import com.simkeeper.app.ui.components.PrimaryButton
import com.simkeeper.app.ui.components.UndoSnackbar
import com.simkeeper.app.ui.components.carrierName
import com.simkeeper.app.ui.preview.SampleData
import com.simkeeper.app.ui.theme.SimKeeperTheme
import java.time.YearMonth

class SimDetailActions(
    val onBack: () -> Unit,
    val onEdit: () -> Unit,
    val onDelete: () -> Unit,
    val onUsedToday: () -> Unit,
    val onToggleReminder: (Int) -> Unit,
    val onToggleHistory: () -> Unit,
    val onRecharged: () -> Unit,
)

@Composable
fun SimDetailScreen(
    state: SimDetailUiState,
    actions: SimDetailActions,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val simState = state.simState
    var showPlans by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        containerColor = SimKeeperTheme.colors.background,
        topBar = { SimDetailTopBar(simState?.sim, actions.onBack, actions.onEdit, actions.onDelete) },
        snackbarHost = { SnackbarHost(snackbarHostState) { UndoSnackbar(it) } },
        bottomBar = {
            if (simState != null) {
                PrimaryButton(
                    text = stringResource(R.string.i_recharged),
                    onClick = actions.onRecharged,
                    icon = R.drawable.ic_check,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = SimKeeperTheme.spacing.screen, vertical = SimKeeperTheme.spacing.md),
                )
            }
        },
    ) { padding ->
        if (simState == null) return@Scaffold
        val spacing = SimKeeperTheme.spacing
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screen, vertical = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            DetailHero(simState, state.extras.graceNote)
            DeadlineTiles(simState, state.local.usedUpdated, actions.onUsedToday)
            CheapestPlanCard(
                plan = state.extras.plans.firstOrNull(),
                pricesAsOf = state.extras.pricesAsOf,
                carrier = carrierName(simState.sim),
                onClick = { showPlans = true },
            )
            RemindMeSection(state.reminderOffsets, actions.onToggleReminder)
            HistorySection(state.recharges, state.local.showAllHistory, actions.onToggleHistory)
        }
        if (showPlans) {
            PlansSheet(carrierName(simState.sim), state.extras.plans, state.extras.pricesAsOf, onDismiss = { showPlans = false })
        }
    }
}

@PreviewLightDark
@Composable
private fun SimDetailScreenPreview() {
    SimKeeperTheme {
        SimDetailScreen(
            state = SimDetailUiState(
                loading = false,
                simState = SampleData.bankState,
                today = SampleData.today,
                recharges = SampleData.bankHistory,
                reminderOffsets = setOf(7, 3, 1, 0),
                extras = CarrierExtras(plans = listOf(PlanOffer(null, 28)), pricesAsOf = YearMonth.of(2026, 10)),
            ),
            actions = SimDetailActions({}, {}, {}, {}, {}, {}, {}),
        )
    }
}
