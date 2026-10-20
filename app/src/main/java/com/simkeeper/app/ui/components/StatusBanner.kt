package com.simkeeper.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.domain.Status
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.theme.SimKeeperTheme

/** One line on the status container colour, with an icon. */
@Composable
fun StatusBanner(text: String, status: Status, modifier: Modifier = Modifier) {
    val colors = statusColors(status)
    val sizes = SimKeeperTheme.sizes
    val icon = if (status == Status.SAFE) R.drawable.ic_check_circle else R.drawable.ic_warning
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.container, SimKeeperTheme.shapes.banner)
            .padding(horizontal = sizes.bannerPaddingHorizontal, vertical = sizes.bannerPaddingVertical)
            .semantics(mergeDescendants = true) { heading() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.sm),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = colors.content,
            modifier = Modifier.size(sizes.iconSmall),
        )
        Text(text = text, style = SimKeeperTheme.typography.bodyMedium, color = colors.content)
    }
}

@PreviewLightDark
@Composable
private fun StatusBannerPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)) {
            val needs = pluralStringResource(R.plurals.home_banner_needs_recharge, 1, 1)
            StatusBanner(needs, Status.URGENT)
            StatusBanner(needs, Status.SOON)
            StatusBanner(stringResource(R.string.home_banner_all_safe), Status.SAFE)
        }
    }
}
