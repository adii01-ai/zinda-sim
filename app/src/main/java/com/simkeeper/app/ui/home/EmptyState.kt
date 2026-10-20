package com.simkeeper.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.ui.components.PrimaryButton
import com.simkeeper.app.ui.onboarding.OnboardingIllustration
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.theme.SimKeeperTheme

@Composable
fun EmptyState(onAddSim: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = SimKeeperTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        OnboardingIllustration(page = 0)
        Text(
            text = stringResource(R.string.empty_title),
            style = SimKeeperTheme.typography.titleLarge,
            color = SimKeeperTheme.colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.empty_body),
            style = SimKeeperTheme.typography.bodyLarge,
            color = SimKeeperTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        PrimaryButton(text = stringResource(R.string.empty_action), onClick = onAddSim, icon = R.drawable.ic_add)
    }
}

@PreviewLightDark
@Composable
private fun EmptyStatePreview() {
    PreviewSurface { EmptyState(onAddSim = {}) }
}
