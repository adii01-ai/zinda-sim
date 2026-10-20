package com.simkeeper.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.theme.SimKeeperTheme

/** 56 dp pill, primary background. Full width by default. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    enabled: Boolean = true,
    fillWidth: Boolean = true,
    @DrawableRes trailingIcon: Int? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = SimKeeperTheme.shapes.pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = SimKeeperTheme.colors.primary,
            contentColor = SimKeeperTheme.colors.onPrimary,
        ),
        contentPadding = PaddingValues(horizontal = SimKeeperTheme.spacing.xxl),
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = SimKeeperTheme.sizes.buttonHeight),
    ) {
        ButtonContent(text, icon, trailingIcon)
    }
}

/** 56 dp pill, 1.5 dp outline, primary text. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    fillWidth: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        shape = SimKeeperTheme.shapes.pill,
        border = BorderStroke(SimKeeperTheme.sizes.outline, SimKeeperTheme.colors.primary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = SimKeeperTheme.colors.primary),
        contentPadding = PaddingValues(horizontal = SimKeeperTheme.spacing.xxl),
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = SimKeeperTheme.sizes.buttonHeight),
    ) {
        ButtonContent(text, icon)
    }
}

/** Compact pill used in the SIM card footer. Keeps a 48 dp touch target. */
@Composable
fun SmallPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        shape = SimKeeperTheme.shapes.pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = SimKeeperTheme.colors.primary,
            contentColor = SimKeeperTheme.colors.onPrimary,
        ),
        contentPadding = PaddingValues(horizontal = SimKeeperTheme.spacing.lg),
        modifier = modifier.heightIn(min = SimKeeperTheme.sizes.chipHeight),
    ) {
        Text(text = text, style = SimKeeperTheme.type.chip, maxLines = 1)
    }
}

/** Text link: 15 sp bold, primary, 48 dp minimum touch target. */
@Composable
fun TextLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = SimKeeperTheme.colors.primary),
        modifier = modifier.defaultMinSize(minHeight = SimKeeperTheme.sizes.touchTarget),
    ) {
        Text(text = text, style = SimKeeperTheme.type.textLink, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ButtonContent(text: String, @DrawableRes icon: Int?, @DrawableRes trailingIcon: Int? = null) {
    if (icon != null) {
        ButtonIcon(icon)
        Spacer(Modifier.width(SimKeeperTheme.spacing.sm))
    }
    Text(text = text, style = SimKeeperTheme.typography.labelLarge, textAlign = TextAlign.Center)
    if (trailingIcon != null) {
        Spacer(Modifier.width(SimKeeperTheme.spacing.sm))
        ButtonIcon(trailingIcon)
    }
}

@Composable
private fun ButtonIcon(@DrawableRes icon: Int) {
    Icon(
        painter = painterResource(icon),
        contentDescription = null,
        modifier = Modifier.size(SimKeeperTheme.sizes.buttonIcon),
    )
}

@PreviewLightDark
@Composable
private fun ButtonsPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md)) {
            PrimaryButton(stringResource(R.string.action_continue), onClick = {})
            PrimaryButton(stringResource(R.string.i_recharged), onClick = {}, icon = R.drawable.ic_check)
            PrimaryButton(stringResource(R.string.action_next), onClick = {}, trailingIcon = R.drawable.ic_arrow_forward, fillWidth = false)
            SecondaryButton(stringResource(R.string.action_undo), onClick = {})
            SmallPrimaryButton(stringResource(R.string.i_recharged), onClick = {})
            TextLink(stringResource(R.string.add_sim_by_hand), onClick = {})
        }
    }
}
