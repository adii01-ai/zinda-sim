package com.simkeeper.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.simkeeper.app.R
import com.simkeeper.app.domain.model.Sim
import com.simkeeper.app.ui.components.CarrierTag
import com.simkeeper.app.ui.theme.SimKeeperTheme

/** Back · SIM name + carrier tag · edit · overflow ("Delete SIM" with confirm). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimDetailTopBar(sim: Sim?, onBack: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SimKeeperTheme.colors.background),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.action_back))
            }
        },
        title = {
            if (sim != null) {
                // The name is measured first; the tag gets what is left (and shrinks with large text).
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)) {
                    Text(
                        sim.name,
                        style = SimKeeperTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    CarrierTag(sim.carrier, sim.carrierLabel, Modifier.weight(1f, fill = false))
                }
            }
        },
        actions = {
            if (sim != null) {
                IconButton(onClick = onEdit) {
                    Icon(painterResource(R.drawable.ic_edit), contentDescription = stringResource(R.string.action_edit))
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(painterResource(R.drawable.ic_more_vert), contentDescription = stringResource(R.string.action_more))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.detail_delete)) },
                            leadingIcon = { Icon(painterResource(R.drawable.ic_delete), contentDescription = null) },
                            onClick = {
                                menuOpen = false
                                confirmDelete = true
                            },
                        )
                    }
                }
            }
        },
    )
    if (confirmDelete && sim != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.detail_delete_title, sim.name)) },
            text = { Text(stringResource(R.string.detail_delete_body)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text(stringResource(R.string.action_delete), color = SimKeeperTheme.status.urgent.content) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.action_cancel)) } },
            containerColor = SimKeeperTheme.colors.surface,
            shape = SimKeeperTheme.shapes.dialog,
        )
    }
}
