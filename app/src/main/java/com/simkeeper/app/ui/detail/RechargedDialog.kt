package com.simkeeper.app.ui.detail

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simkeeper.app.R
import com.simkeeper.app.domain.Status
import com.simkeeper.app.domain.simStateOf
import com.simkeeper.app.ui.components.Confetti
import com.simkeeper.app.ui.components.DaysRing
import com.simkeeper.app.ui.components.PrimaryButton
import com.simkeeper.app.ui.components.RingSize
import com.simkeeper.app.ui.components.TextLink
import com.simkeeper.app.ui.preview.SampleData
import com.simkeeper.app.ui.theme.Motion
import com.simkeeper.app.ui.theme.SimKeeperTheme

@Composable
fun RechargedRoute(onClose: () -> Unit, viewModel: RechargedViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.closed) { if (state.closed) onClose() }
    BackHandler { viewModel.onDone() }
    RechargedScreen(
        state = state,
        onPlanDays = viewModel::onPlanDays,
        onAmount = viewModel::onAmount,
        onDone = viewModel::onDone,
        onUndo = viewModel::onUndo,
    )
}

@Composable
fun RechargedScreen(
    state: RechargedUiState,
    onPlanDays: (Int) -> Unit,
    onAmount: (String) -> Unit,
    onDone: () -> Unit,
    onUndo: () -> Unit,
) {
    val spacing = SimKeeperTheme.spacing
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SimKeeperTheme.colors.background),
    ) {
        val before = state.before
        val after = state.after
        if (before != null && after != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.screen, vertical = spacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.lg),
            ) {
                RechargeRing(beforeProgress = before.ringProgress, beforeStatus = before.status, daysLeft = after.daysLeft)
                RechargedSummary(state)
                RechargedFields(state, onPlanDays, onAmount)
                PrimaryButton(stringResource(R.string.action_done), onClick = onDone)
                TextLink(stringResource(R.string.action_undo), onClick = onUndo, modifier = Modifier.fillMaxWidth())
            }
            Confetti(modifier = Modifier.fillMaxSize(), originY = CONFETTI_ORIGIN)
        }
    }
}

/** Burst from the ring (top of the screen). */
private const val CONFETTI_ORIGIN = 0.12f

/** Ring animates from the old fill to full while the colour cross-fades to safe (500 ms) + CONFIRM haptic. */
@Composable
private fun RechargeRing(beforeProgress: Float, beforeStatus: Status, daysLeft: Long) {
    var filled by rememberSaveable { mutableStateOf(false) }
    val view = LocalView.current
    LaunchedEffect(Unit) {
        if (!filled) {
            filled = true
            val feedback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.CONFIRM
            } else {
                HapticFeedbackConstants.VIRTUAL_KEY
            }
            view.performHapticFeedback(feedback)
        }
    }
    DaysRing(
        days = daysLeft,
        progress = if (filled) 1f else beforeProgress,
        status = if (filled) Status.SAFE else beforeStatus,
        size = RingSize.DETAIL,
        animateOnAppear = false,
        fillDurationMs = Motion.RECHARGE_MS,
    )
}

@PreviewLightDark
@Composable
private fun RechargedScreenPreview() {
    SimKeeperTheme {
        RechargedScreen(
            state = RechargedUiState(
                before = SampleData.bankState,
                after = simStateOf(SampleData.bankSim.copy(lastRechargeDate = SampleData.today, lastUsedDate = SampleData.today), SampleData.today),
            ),
            onPlanDays = {}, onAmount = {}, onDone = {}, onUndo = {},
        )
    }
}
