package com.simkeeper.app.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.simkeeper.app.ui.theme.SimKeeperTheme

/** Theme + background + screen padding for component previews. */
@Composable
fun PreviewSurface(padded: Boolean = true, content: @Composable () -> Unit) {
    SimKeeperTheme {
        Box(
            modifier = Modifier
                .background(SimKeeperTheme.colors.background)
                .then(if (padded) Modifier.padding(SimKeeperTheme.spacing.screen) else Modifier),
        ) { content() }
    }
}
