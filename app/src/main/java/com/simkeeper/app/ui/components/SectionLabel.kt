package com.simkeeper.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.ui.format.currentLocale
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.theme.SimKeeperTheme

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(currentLocale()),
        style = SimKeeperTheme.typography.labelMedium,
        color = SimKeeperTheme.colors.onSurfaceVariant,
        modifier = modifier.semantics { heading() },
    )
}

@PreviewLightDark
@Composable
private fun SectionLabelPreview() {
    PreviewSurface { SectionLabel(stringResource(R.string.home_section_sims)) }
}
