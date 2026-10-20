package com.simkeeper.app.ui.detail

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simkeeper.app.R
import com.simkeeper.app.ui.components.RechargeUndoEffect
import com.simkeeper.app.ui.setup.SetValiditySheet
import com.simkeeper.app.ui.setup.ValidityHeader

@Composable
fun SimDetailRoute(
    onBack: () -> Unit,
    onRecharged: (Long) -> Unit,
    viewModel: SimDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pendingUndo by viewModel.pendingUndo.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }
    LaunchedEffect(state.local.deleted, state.loading) { if (!state.loading && state.local.deleted) onBack() }
    LaunchedEffect(state.local.openRecharged, state.simState != null) {
        // Notification action "I recharged" opens the confirmation once.
        if (state.local.openRecharged && state.simState != null) {
            viewModel.onRechargedOpened()
            onRecharged(viewModel.simId)
        }
    }
    RechargeUndoEffect(pendingUndo, snackbarHostState, onUndo = viewModel::onUndo, onShown = viewModel::onUndoShown)

    SimDetailScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        actions = SimDetailActions(
            onBack = onBack,
            onEdit = viewModel::onEdit,
            onDelete = viewModel::onDelete,
            onUsedToday = viewModel::onUsedToday,
            onToggleReminder = viewModel::onToggleReminder,
            onToggleHistory = viewModel::onToggleHistory,
            onRecharged = { onRecharged(viewModel.simId) },
        ),
    )

    val form = state.local.editForm
    val sim = state.simState?.sim
    val today = state.today
    if (form != null && sim != null && today != null) {
        SetValiditySheet(
            header = ValidityHeader(sim.name, sim.carrier, sim.carrierLabel, sim.slot),
            form = form,
            today = today,
            saveLabel = stringResource(R.string.action_save),
            onChange = viewModel::onEditChange,
            onSave = viewModel::onEditSave,
            onDismiss = viewModel::onEditDismiss,
        )
    }
}
