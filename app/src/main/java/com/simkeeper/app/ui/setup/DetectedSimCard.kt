package com.simkeeper.app.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.data.telephony.DetectedSim
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.ui.components.MonogramTile
import com.simkeeper.app.ui.components.SelectChip
import com.simkeeper.app.ui.components.carrierName
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.theme.SimKeeperTheme

private const val DASH_ON = 10f
private const val DASH_OFF = 8f

fun nameRes(choice: NameChoice): Int = when (choice) {
    NameChoice.DAILY -> R.string.name_daily_sim
    NameChoice.BANK -> R.string.name_bank_sim
    NameChoice.WORK -> R.string.name_work
    NameChoice.CUSTOM -> R.string.name_custom
}

/** The name the SIM will be saved with. */
@Composable
fun resolvedName(sim: DetectedSimUi): String =
    if (sim.nameChoice == NameChoice.CUSTOM) sim.customName.trim() else stringResource(nameRes(sim.nameChoice))

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetectedSimCard(sim: DetectedSimUi, onChoice: (NameChoice) -> Unit, onCustomName: (String) -> Unit) {
    val spacing = SimKeeperTheme.spacing
    Card(
        shape = SimKeeperTheme.shapes.card,
        colors = CardDefaults.cardColors(containerColor = SimKeeperTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = SimKeeperTheme.sizes.cardElevation),
    ) {
        Column(Modifier.padding(SimKeeperTheme.sizes.cardPadding), verticalArrangement = Arrangement.spacedBy(spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                MonogramTile(sim.detected.carrier, sim.detected.carrierName)
                Column(Modifier.weight(1f)) {
                    Text(
                        carrierName(sim.detected.carrier, sim.detected.carrierName),
                        style = SimKeeperTheme.typography.titleMedium,
                        color = SimKeeperTheme.colors.onSurface,
                    )
                    Text(
                        stringResource(R.string.sim_slot, sim.detected.slot),
                        style = SimKeeperTheme.typography.bodyMedium,
                        color = SimKeeperTheme.colors.onSurfaceVariant,
                    )
                }
                AutoDetectedTag()
            }
            val gap = Arrangement.spacedBy(spacing.sm)
            FlowRow(horizontalArrangement = gap, verticalArrangement = gap) {
                NameChoice.entries.forEach { choice ->
                    SelectChip(stringResource(nameRes(choice)), selected = sim.nameChoice == choice, onClick = { onChoice(choice) })
                }
            }
            if (sim.nameChoice == NameChoice.CUSTOM) {
                OutlinedTextField(
                    value = sim.customName,
                    onValueChange = onCustomName,
                    label = { Text(stringResource(R.string.name_field_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun AutoDetectedTag() {
    Text(
        text = stringResource(R.string.auto_detected),
        style = SimKeeperTheme.typography.labelSmall,
        color = SimKeeperTheme.colors.primary,
        modifier = Modifier
            .background(SimKeeperTheme.colors.primaryContainer, SimKeeperTheme.shapes.tag)
            .padding(horizontal = SimKeeperTheme.sizes.tagPaddingHorizontal, vertical = SimKeeperTheme.sizes.tagPaddingVertical),
    )
}

/** Dashed outline button: "+ Track a family member's SIM". */
@Composable
fun DashedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val outline = SimKeeperTheme.colors.outline
    val strokeWidth = SimKeeperTheme.sizes.outline
    val radius = SimKeeperTheme.spacing.xl
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = SimKeeperTheme.sizes.buttonHeight)
            .drawBehind {
                drawRoundRect(
                    color = outline,
                    cornerRadius = CornerRadius(radius.toPx()),
                    style = Stroke(width = strokeWidth.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH_ON, DASH_OFF))),
                )
            }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = SimKeeperTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.sm, Alignment.CenterHorizontally),
    ) {
        Icon(painterResource(R.drawable.ic_add), contentDescription = null, tint = SimKeeperTheme.colors.primary, modifier = Modifier.size(SimKeeperTheme.sizes.iconSmall))
        Text(text, style = SimKeeperTheme.typography.labelLarge, color = SimKeeperTheme.colors.primary)
    }
}

@PreviewLightDark
@Composable
private fun DetectedSimCardPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md)) {
            DetectedSimCard(DetectedSimUi(DetectedSim("Jio 4G", Carrier.JIO, 1, 1), NameChoice.DAILY), {}, {})
            DetectedSimCard(DetectedSimUi(DetectedSim("Airtel", Carrier.AIRTEL, 2, 2), NameChoice.CUSTOM, "Bank"), {}, {})
            DashedButton(stringResource(R.string.track_family_sim), onClick = {})
        }
    }
}
