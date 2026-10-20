package com.simkeeper.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.theme.SimKeeperTheme

/** 44 dp letter tile in carrier colours. Never a logo. Decorative: the name is always beside it. */
@Composable
fun MonogramTile(carrier: Carrier, carrierLabel: String?, modifier: Modifier = Modifier) {
    val colors = carrierColors(carrier)
    Box(
        modifier = modifier
            .size(SimKeeperTheme.sizes.monogram)
            .background(colors.container, SimKeeperTheme.shapes.monogram)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = monogramLetter(carrier, carrierLabel),
            style = SimKeeperTheme.type.monogram,
            color = colors.content,
            maxLines = 1,
        )
    }
}

@PreviewLightDark
@Composable
private fun MonogramTilePreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)) {
            MonogramTile(Carrier.JIO, null)
            MonogramTile(Carrier.AIRTEL, null)
            MonogramTile(Carrier.VI, null)
            MonogramTile(Carrier.BSNL, null)
            MonogramTile(Carrier.OTHER, "MTNL")
        }
    }
}
