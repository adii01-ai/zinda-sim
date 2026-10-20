package com.simkeeper.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.domain.Status
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.ui.components.CarrierTag
import com.simkeeper.app.ui.components.DaysRing
import com.simkeeper.app.ui.components.MonogramTile
import com.simkeeper.app.ui.components.RingSize
import com.simkeeper.app.ui.components.statusColors
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.theme.SimKeeperTheme

private const val CARD_TILT = 8f

/** The little gold SIM chip drawn on each mini card. */
private const val SIM_CHIP_ALPHA = 0.6f

/**
 * Decorative only: hidden from TalkBack (the title and body carry the meaning).
 * Drawn at font scale 1 like a picture, so large text does not break the mini cards.
 */
@Composable
fun OnboardingIllustration(page: Int, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1f)) {
        IllustrationFrame(page, modifier)
    }
}

@Composable
private fun IllustrationFrame(page: Int, modifier: Modifier) {
    Box(
        modifier = modifier
            .size(SimKeeperTheme.sizes.illustration)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(SimKeeperTheme.sizes.illustration)
                .background(SimKeeperTheme.colors.primaryContainer, SimKeeperTheme.shapes.pill),
        )
        when (page) {
            0 -> TwoSimsIllustration()
            1 -> GlanceIllustration()
            else -> PrivacyIllustration()
        }
    }
}

@Composable
private fun TwoSimsIllustration() {
    Box(contentAlignment = Alignment.Center) {
        DaysRing(days = 61, progress = 61 / 84f, status = Status.SAFE, size = RingSize.DETAIL, label = "")
        Row(horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)) {
            MiniSimCard(Carrier.JIO, days = 61, status = Status.SAFE, modifier = Modifier.rotate(-CARD_TILT))
            MiniSimCard(Carrier.AIRTEL, days = 2, status = Status.URGENT, modifier = Modifier.rotate(CARD_TILT))
        }
    }
}

@Composable
private fun MiniSimCard(carrier: Carrier, days: Int, status: Status, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.width(SimKeeperTheme.sizes.ringCard + SimKeeperTheme.spacing.lg),
        shape = SimKeeperTheme.shapes.tile,
        colors = CardDefaults.cardColors(containerColor = SimKeeperTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = SimKeeperTheme.sizes.floatingElevation),
    ) {
        Column(Modifier.padding(SimKeeperTheme.spacing.md), verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)) {
            CarrierTag(carrier, null)
            Box(
                Modifier
                    .size(SimKeeperTheme.sizes.iconSmall)
                    .background(SimKeeperTheme.status.soon.content.copy(alpha = SIM_CHIP_ALPHA), SimKeeperTheme.shapes.tag),
            )
            Text(
                text = pluralStringResource(R.plurals.days_count, days, days),
                style = SimKeeperTheme.typography.labelLarge,
                color = statusColors(status).content,
            )
        }
    }
}

@Composable
private fun GlanceIllustration() {
    Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)) {
        GlanceRow(Carrier.JIO, 61, Status.SAFE)
        GlanceRow(Carrier.AIRTEL, 2, Status.URGENT)
        GlanceRow(Carrier.VI, 12, Status.SAFE)
    }
}

@Composable
private fun GlanceRow(carrier: Carrier, days: Int, status: Status) {
    Card(
        shape = SimKeeperTheme.shapes.tile,
        colors = CardDefaults.cardColors(containerColor = SimKeeperTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = SimKeeperTheme.sizes.cardElevation),
    ) {
        Row(
            modifier = Modifier.padding(SimKeeperTheme.spacing.sm).width(SimKeeperTheme.sizes.illustration / 2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md),
        ) {
            MonogramTile(carrier, null)
            Text(
                text = pluralStringResource(R.plurals.days_count, days, days),
                style = SimKeeperTheme.typography.labelLarge,
                color = statusColors(status).content,
            )
        }
    }
}

@Composable
private fun PrivacyIllustration() {
    Box(
        modifier = Modifier
            .size(SimKeeperTheme.sizes.ringDetail)
            .background(SimKeeperTheme.colors.primary, SimKeeperTheme.shapes.dialog),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_lock),
            contentDescription = null,
            tint = SimKeeperTheme.colors.onPrimary,
            modifier = Modifier.size(SimKeeperTheme.sizes.ringCard),
        )
    }
}

@PreviewLightDark
@Composable
private fun OnboardingIllustrationPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.lg)) {
            (0..2).forEach { OnboardingIllustration(page = it) }
        }
    }
}
