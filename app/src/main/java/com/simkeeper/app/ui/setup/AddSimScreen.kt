package com.simkeeper.app.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simkeeper.app.R
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.ui.components.PrimaryButton
import com.simkeeper.app.ui.preview.SampleData
import com.simkeeper.app.ui.theme.SimKeeperTheme

@Composable
fun AddSimRoute(onBack: () -> Unit, onSaved: () -> Unit, viewModel: AddSimViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }
    AddSimScreen(
        state = state,
        actions = AddSimActions(
            onBack = onBack,
            onCarrier = viewModel::onCarrier,
            onCarrierLabel = viewModel::onCarrierLabel,
            onName = viewModel::onName,
            onFamily = viewModel::onFamily,
            onOwner = viewModel::onOwner,
            onFormChange = viewModel::onFormChange,
            onSave = viewModel::onSave,
        ),
    )
}

class AddSimActions(
    val onBack: () -> Unit,
    val onCarrier: (Carrier) -> Unit,
    val onCarrierLabel: (String) -> Unit,
    val onName: (String) -> Unit,
    val onFamily: (Boolean) -> Unit,
    val onOwner: (String) -> Unit,
    val onFormChange: (ValidityForm) -> Unit,
    val onSave: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSimScreen(state: AddSimUiState, actions: AddSimActions) {
    val spacing = SimKeeperTheme.spacing
    Scaffold(
        containerColor = SimKeeperTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_sim_title), style = SimKeeperTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SimKeeperTheme.colors.background),
            )
        },
        bottomBar = {
            PrimaryButton(
                text = stringResource(R.string.add_sim_save),
                onClick = actions.onSave,
                enabled = state.canSave,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = spacing.screen, vertical = spacing.md),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screen, vertical = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.xl),
        ) {
            AddSimFields(state, actions.onCarrier, actions.onCarrierLabel, actions.onName, actions.onFamily, actions.onOwner)
            SetValidityContent(form = state.form, today = state.today, onChange = actions.onFormChange)
        }
    }
}

@PreviewLightDark
@Composable
private fun AddSimScreenPreview() {
    SimKeeperTheme {
        AddSimScreen(
            state = AddSimUiState(today = SampleData.today),
            actions = AddSimActions({}, {}, {}, {}, {}, {}, {}, {}),
        )
    }
}
