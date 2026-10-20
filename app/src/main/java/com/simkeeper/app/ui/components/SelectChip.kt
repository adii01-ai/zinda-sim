package com.simkeeper.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.theme.SimKeeperTheme

/** How a chip announces itself: one of a group, or an independent on/off toggle. */
enum class ChipMode { SINGLE, TOGGLE }

/**
 * 40 dp pill (48 dp touch target). Off: 1.5 dp outline on surface.
 * On: 2 dp primary border, primaryContainer background, primary text.
 */
@Composable
fun SelectChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mode: ChipMode = ChipMode.SINGLE,
    @DrawableRes icon: Int? = null,
) {
    val colors = SimKeeperTheme.colors
    val sizes = SimKeeperTheme.sizes
    val shape = SimKeeperTheme.shapes.pill
    val container = if (selected) colors.primaryContainer else colors.surface
    val content = if (selected) colors.primary else colors.onSurface
    val border = BorderStroke(
        width = if (selected) sizes.outlineSelected else sizes.outline,
        color = if (selected) colors.primary else colors.outline,
    )
    val chipModifier = modifier.heightIn(min = sizes.chipHeight)
    val label: @Composable () -> Unit = { ChipLabel(text, icon) }
    when (mode) {
        ChipMode.SINGLE -> Surface(
            selected = selected,
            onClick = onClick,
            shape = shape,
            color = container,
            contentColor = content,
            border = border,
            modifier = chipModifier.semantics { role = Role.RadioButton },
            content = label,
        )
        ChipMode.TOGGLE -> Surface(
            checked = selected,
            onCheckedChange = { onClick() },
            shape = shape,
            color = container,
            contentColor = content,
            border = border,
            modifier = chipModifier.semantics { role = Role.Checkbox },
            content = label,
        )
    }
}

@Composable
private fun ChipLabel(text: String, @DrawableRes icon: Int?) {
    Row(
        modifier = Modifier
            .heightIn(min = SimKeeperTheme.sizes.chipHeight)
            .padding(horizontal = SimKeeperTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.xs, Alignment.CenterHorizontally),
    ) {
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(SimKeeperTheme.sizes.iconTiny))
        }
        Text(text = text, style = SimKeeperTheme.type.chip, maxLines = 1)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@PreviewLightDark
@Composable
private fun SelectChipPreview() {
    PreviewSurface {
        val gap = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)
        FlowRow(horizontalArrangement = gap, verticalArrangement = gap) {
            SelectChip(stringResource(R.string.today), selected = false, onClick = {})
            SelectChip(stringResource(R.string.validity_3_weeks_ago), selected = true, onClick = {})
            SelectChip(stringResource(R.string.validity_pick_date), selected = false, onClick = {}, icon = R.drawable.ic_calendar_month)
            SelectChip(stringResource(R.string.remind_7_days), selected = true, onClick = {}, mode = ChipMode.TOGGLE)
        }
    }
}
