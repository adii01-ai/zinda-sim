package com.simkeeper.app.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.ui.components.carrierNameRes
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.preview.SampleData
import com.simkeeper.app.ui.theme.SimKeeperTheme

/** Carrier, name, family switch and owner name. */
@Composable
fun AddSimFields(
    state: AddSimUiState,
    onCarrier: (Carrier) -> Unit,
    onCarrierLabel: (String) -> Unit,
    onName: (String) -> Unit,
    onFamily: (Boolean) -> Unit,
    onOwner: (String) -> Unit,
) {
    val words = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
    Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md)) {
        CarrierDropdown(state.carrier, onCarrier)
        if (state.carrier == Carrier.OTHER) {
            OutlinedTextField(
                value = state.carrierLabel,
                onValueChange = onCarrierLabel,
                label = { Text(stringResource(R.string.add_sim_carrier_name)) },
                singleLine = true,
                keyboardOptions = words,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        OutlinedTextField(
            value = state.name,
            onValueChange = onName,
            label = { Text(stringResource(R.string.name_field_label)) },
            isError = state.showNameError,
            supportingText = if (state.showNameError) {
                { Text(stringResource(R.string.add_sim_name_error)) }
            } else {
                null
            },
            singleLine = true,
            keyboardOptions = words,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = SimKeeperTheme.sizes.touchTarget)
                .toggleable(value = state.isFamily, role = Role.Switch, onValueChange = onFamily),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md),
        ) {
            Text(
                text = stringResource(R.string.add_sim_family),
                style = SimKeeperTheme.typography.bodyLarge,
                color = SimKeeperTheme.colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = state.isFamily,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(checkedTrackColor = SimKeeperTheme.colors.primary),
            )
        }
        if (state.isFamily) {
            OutlinedTextField(
                value = state.ownerName,
                onValueChange = onOwner,
                label = { Text(stringResource(R.string.add_sim_owner)) },
                placeholder = { Text(stringResource(R.string.add_sim_owner_hint)) },
                singleLine = true,
                keyboardOptions = words,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarrierDropdown(selected: Carrier, onSelect: (Carrier) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = stringResource(carrierNameRes(selected)),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.add_sim_carrier)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Carrier.entries.forEach { carrier ->
                DropdownMenuItem(
                    text = { Text(stringResource(carrierNameRes(carrier))) },
                    onClick = {
                        onSelect(carrier)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AddSimFieldsPreview() {
    PreviewSurface {
        AddSimFields(
            state = AddSimUiState(isFamily = true, today = SampleData.today),
            onCarrier = {}, onCarrierLabel = {}, onName = {}, onFamily = {}, onOwner = {},
        )
    }
}
