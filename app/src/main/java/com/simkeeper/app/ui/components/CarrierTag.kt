package com.simkeeper.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.ui.format.currentLocale
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.theme.SimKeeperTheme

/** Small uppercase carrier label on its tile colour. */
@Composable
fun CarrierTag(carrier: Carrier, carrierLabel: String?, modifier: Modifier = Modifier) {
    val colors = carrierColors(carrier)
    val sizes = SimKeeperTheme.sizes
    Text(
        text = carrierName(carrier, carrierLabel).uppercase(currentLocale()),
        style = SimKeeperTheme.typography.labelSmall,
        color = colors.content,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .background(colors.container, SimKeeperTheme.shapes.tag)
            .padding(horizontal = sizes.tagPaddingHorizontal, vertical = sizes.tagPaddingVertical),
    )
}

@PreviewLightDark
@Composable
private fun CarrierTagPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)) {
            Carrier.entries.forEach { CarrierTag(it, null) }
        }
    }
}
