package com.simkeeper.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.ui.components.UndoSnackbar
import com.simkeeper.app.ui.format.DateFormats
import com.simkeeper.app.ui.format.currentLocale
import com.simkeeper.app.ui.preview.SampleData
import com.simkeeper.app.ui.theme.SimKeeperTheme
import java.time.LocalDate

class HomeActions(
    val onOpenSim: (Long) -> Unit,
    val onRecharged: (Long) -> Unit,
    val onSettings: () -> Unit,
    val onAddSim: () -> Unit,
    val onOpenBatterySettings: () -> Unit,
    val onDismissBatteryGuide: () -> Unit,
)

@Composable
fun HomeScreen(state: HomeUiState, actions: HomeActions, snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }) {
    // Rings and cards animate only on the first visit, not when coming back from another screen.
    var entered by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.loading) { if (!state.loading) entered = true }
    val animate = !entered
    Scaffold(
        containerColor = SimKeeperTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) { UndoSnackbar(it) } },
        floatingActionButton = { if (!state.isEmpty) AddSimFab(actions.onAddSim) },
    ) { padding ->
        val spacing = SimKeeperTheme.spacing
        val direction = LocalLayoutDirection.current
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = padding.calculateStartPadding(direction) + spacing.screen,
                end = padding.calculateEndPadding(direction) + spacing.screen,
                top = padding.calculateTopPadding() + spacing.lg,
                bottom = padding.calculateBottomPadding() + SimKeeperTheme.sizes.fabBottomClearance,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            item(key = "header") { HomeHeader(state.today, actions.onSettings) }
            if (state.isEmpty) {
                item(key = "empty") { EmptyState(onAddSim = actions.onAddSim) }
            } else if (!state.loading) {
                homeContent(state, actions, animate)
            }
        }
    }
}

@Composable
private fun HomeHeader(today: LocalDate?, onSettings: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            if (today != null) {
                Text(
                    text = DateFormats.weekdayDayMonth(today, currentLocale()),
                    style = SimKeeperTheme.typography.bodyMedium,
                    color = SimKeeperTheme.colors.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.home_title),
                style = SimKeeperTheme.typography.headlineMedium,
                color = SimKeeperTheme.colors.onSurface,
                modifier = Modifier.semantics { heading() },
            )
        }
        IconButton(onClick = onSettings) {
            Icon(
                painter = painterResource(R.drawable.ic_settings),
                contentDescription = stringResource(R.string.action_settings),
                tint = SimKeeperTheme.colors.onSurface,
            )
        }
    }
}

@Composable
private fun AddSimFab(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = SimKeeperTheme.colors.inverseSurface,
        contentColor = SimKeeperTheme.colors.inverseOnSurface,
        shape = SimKeeperTheme.shapes.pill,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = SimKeeperTheme.sizes.floatingElevation),
        icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(SimKeeperTheme.sizes.iconSmall)) },
        text = { Text(stringResource(R.string.home_add_sim), style = SimKeeperTheme.typography.labelLarge) },
    )
}

@PreviewLightDark
@Composable
private fun HomeScreenPreview() {
    SimKeeperTheme {
        HomeScreen(
            state = HomeUiState(
                loading = false,
                today = SampleData.today,
                phoneSims = listOf(SampleData.bankState, SampleData.dailyState),
                familySims = listOf(SampleData.mummyState),
                plans = emptyMap(),
            ),
            actions = HomeActions({}, {}, {}, {}, {}, {}),
        )
    }
}

@PreviewLightDark
@Composable
private fun HomeEmptyPreview() {
    SimKeeperTheme {
        HomeScreen(state = HomeUiState(loading = false, today = SampleData.today), actions = HomeActions({}, {}, {}, {}, {}, {}))
    }
}
