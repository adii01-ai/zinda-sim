package com.simkeeper.app.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.theme.SimKeeperTheme

private const val DISABLED_ALPHA = 0.5f

/** White card that groups rows, with dividers between them. */
@Composable
fun SettingsGroup(content: @Composable () -> Unit) {
    Card(
        shape = SimKeeperTheme.shapes.card,
        colors = CardDefaults.cardColors(containerColor = SimKeeperTheme.colors.surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (SimKeeperTheme.isDark) SimKeeperTheme.sizes.noElevation else SimKeeperTheme.sizes.cardElevation,
        ),
    ) { Column { content() } }
}

@Composable
fun SettingsDivider() = HorizontalDivider(color = SimKeeperTheme.colors.outline, modifier = Modifier.padding(horizontal = SimKeeperTheme.spacing.lg))

/** Title (+ optional body) on the left, value or chevron on the right. */
@Composable
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    value: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    @DrawableRes leadingIcon: Int? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = SimKeeperTheme.sizes.touchTarget + SimKeeperTheme.spacing.sm)
            .then(if (onClick != null && enabled) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .padding(horizontal = SimKeeperTheme.spacing.lg, vertical = SimKeeperTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md),
    ) {
        if (leadingIcon != null) {
            Icon(painterResource(leadingIcon), contentDescription = null, tint = SimKeeperTheme.colors.onSurfaceVariant)
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = SimKeeperTheme.typography.bodyLarge, color = SimKeeperTheme.colors.onSurface)
            if (body != null) Text(body, style = SimKeeperTheme.typography.bodyMedium, color = SimKeeperTheme.colors.onSurfaceVariant)
        }
        if (value != null) Text(value, style = SimKeeperTheme.typography.labelLarge, color = SimKeeperTheme.colors.primary)
        if (trailing != null) trailing()
    }
}

@Composable
fun ChevronIcon() = Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = SimKeeperTheme.colors.onSurfaceVariant)

@Composable
fun SwitchRow(title: String, body: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    SettingsRow(
        title = title,
        body = body,
        modifier = Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(checkedTrackColor = SimKeeperTheme.colors.primary),
            )
        },
    )
}

/** Primary-coloured share card: "Check your parents' SIMs". */
@Composable
fun ShareCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = SimKeeperTheme.shapes.card,
        colors = CardDefaults.cardColors(containerColor = SimKeeperTheme.colors.primary, contentColor = SimKeeperTheme.colors.onPrimary),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(SimKeeperTheme.sizes.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md),
        ) {
            IconTile(R.drawable.ic_group)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_share_title), style = SimKeeperTheme.typography.titleMedium)
                Text(stringResource(R.string.settings_share_body), style = SimKeeperTheme.typography.bodyMedium)
            }
            Icon(painterResource(R.drawable.ic_share), contentDescription = null)
        }
    }
}

@Composable
private fun IconTile(@DrawableRes icon: Int) {
    Box(
        modifier = Modifier
            .size(SimKeeperTheme.sizes.monogram)
            .background(SimKeeperTheme.colors.onPrimary.copy(alpha = TILE_ALPHA), SimKeeperTheme.shapes.monogram),
        contentAlignment = Alignment.Center,
    ) { Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(SimKeeperTheme.sizes.icon)) }
}

private const val TILE_ALPHA = 0.16f

@PreviewLightDark
@Composable
private fun SettingsRowsPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md)) {
            ShareCard(onClick = {})
            SettingsGroup {
                SettingsRow(stringResource(R.string.settings_reminder_time), value = "10:00 AM", onClick = {})
                SettingsDivider()
                SwitchRow(stringResource(R.string.settings_quiet_hours), stringResource(R.string.settings_quiet_hours_body), true, {})
                SettingsDivider()
                SettingsRow(stringResource(R.string.settings_backup), body = stringResource(R.string.settings_coming_soon), enabled = false)
            }
        }
    }
}
