package com.simkeeper.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.theme.SimKeeperTheme

/** 64 dp tile for the 4-column plan grid: big number + "days" (or a text label such as "Custom"). */
@Composable
fun PlanChip(
    number: String,
    caption: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SimKeeperTheme.colors
    val sizes = SimKeeperTheme.sizes
    Surface(
        selected = selected,
        onClick = onClick,
        shape = SimKeeperTheme.shapes.tile,
        color = if (selected) colors.primaryContainer else colors.surface,
        contentColor = if (selected) colors.primary else colors.onSurface,
        border = BorderStroke(
            width = if (selected) sizes.outlineSelected else sizes.outline,
            color = if (selected) colors.primary else colors.outline,
        ),
        modifier = modifier
            .heightIn(min = sizes.planChipHeight)
            .semantics { role = Role.RadioButton },
    ) {
        Column(
            modifier = Modifier.heightIn(min = sizes.planChipHeight).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(number, style = SimKeeperTheme.type.chipNumber, maxLines = 1, textAlign = TextAlign.Center)
            Text(caption, style = SimKeeperTheme.typography.labelSmall, maxLines = 1, textAlign = TextAlign.Center)
        }
    }
}

@PreviewLightDark
@Composable
private fun PlanChipPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)) {
            val days = stringResource(R.string.validity_days)
            listOf(28, 56, 84, 365).forEach { d ->
                PlanChip(d.toString(), days, selected = d == 28, onClick = {}, modifier = Modifier.weight(1f))
            }
        }
    }
}
