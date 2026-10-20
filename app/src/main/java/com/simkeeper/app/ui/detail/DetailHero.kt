package com.simkeeper.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.domain.Reason
import com.simkeeper.app.domain.SimState
import com.simkeeper.app.domain.Status
import com.simkeeper.app.ui.components.DaysRing
import com.simkeeper.app.ui.components.RingSize
import com.simkeeper.app.ui.components.TextLink
import com.simkeeper.app.ui.format.DateFormats
import com.simkeeper.app.ui.format.currentLocale
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.preview.SampleData
import com.simkeeper.app.ui.theme.SimKeeperTheme

@Composable
fun DetailCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val shape = SimKeeperTheme.shapes.card
    val colors = CardDefaults.cardColors(containerColor = SimKeeperTheme.colors.surface)
    val elevation = CardDefaults.cardElevation(
        defaultElevation = if (SimKeeperTheme.isDark) SimKeeperTheme.sizes.noElevation else SimKeeperTheme.sizes.cardElevation,
    )
    val body: @Composable ColumnScope.() -> Unit = { Column(Modifier.padding(SimKeeperTheme.sizes.cardPadding)) { content() } }
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = shape, colors = colors, elevation = elevation, content = body)
    } else {
        Card(modifier = modifier.fillMaxWidth(), shape = shape, colors = colors, elevation = elevation, content = body)
    }
}

/** Ring 120 + "Recharge by Wed, 21 Oct" + one calm consequence line. */
@Composable
fun DetailHero(state: SimState, graceNote: String?) {
    val locale = currentLocale()
    val date = DateFormats.weekdayDayMonth(state.deadlines.effective, locale)
    val noUse = state.deadlines.reason == Reason.NO_USE
    val expired = state.status == Status.EXPIRED
    val headline = when {
        noUse -> stringResource(R.string.detail_use_by, date)
        expired -> stringResource(R.string.detail_ended_on, date)
        else -> stringResource(R.string.detail_recharge_by, date)
    }
    val consequence = when {
        noUse -> stringResource(R.string.detail_no_use_consequence)
        expired -> listOfNotNull(graceNote, stringResource(R.string.detail_expired_suffix)).joinToString(" ")
        else -> stringResource(R.string.detail_consequence)
    }
    val days = state.daysLeft.coerceAtLeast(0).toInt()
    val gap = Arrangement.spacedBy(SimKeeperTheme.spacing.lg)
    val ring = @Composable {
        DaysRing(
            days = state.daysLeft,
            progress = state.ringProgress,
            status = state.status,
            size = RingSize.DETAIL,
            contentDescription = pluralStringResource(R.plurals.ring_days_a11y, days, days),
        )
    }
    val text = @Composable {
        Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)) {
            Text(headline, style = SimKeeperTheme.typography.titleLarge, color = SimKeeperTheme.colors.onSurface)
            Text(consequence, style = SimKeeperTheme.typography.bodyMedium, color = SimKeeperTheme.colors.onSurfaceVariant)
        }
    }
    DetailCard {
        val merged = Modifier.semantics(mergeDescendants = true) { }
        if (SimKeeperTheme.isLargeFont) {
            Column(merged, verticalArrangement = gap) { ring(); text() }
        } else {
            Row(merged, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = gap) { ring(); text() }
        }
    }
}

/** "PLAN ENDS 21 Oct 2026 · 28-day plan" and "90-DAY NO-USE LIMIT 13 Jan 2027 · I used it today". */
@Composable
fun DeadlineTiles(state: SimState, usedUpdated: Boolean, onUsedToday: () -> Unit) {
    val gap = Arrangement.spacedBy(SimKeeperTheme.spacing.md)
    if (SimKeeperTheme.isLargeFont) {
        Column(verticalArrangement = gap) {
            PlanEndsTile(state)
            NoUseTile(state, usedUpdated, onUsedToday)
        }
    } else {
        Row(modifier = Modifier.height(IntrinsicSize.Min), horizontalArrangement = gap) {
            PlanEndsTile(state, Modifier.weight(1f).fillMaxHeight())
            NoUseTile(state, usedUpdated, onUsedToday, Modifier.weight(1f).fillMaxHeight())
        }
    }
}

@Composable
private fun PlanEndsTile(state: SimState, modifier: Modifier = Modifier) {
    DetailCard(modifier) {
        TileContent(
            label = stringResource(R.string.detail_plan_ends),
            value = DateFormats.date(state.deadlines.planEnd, currentLocale()),
        )
        Text(
            pluralStringResource(R.plurals.plan_length, state.sim.planDays, state.sim.planDays),
            style = SimKeeperTheme.typography.bodyMedium,
            color = SimKeeperTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun NoUseTile(state: SimState, usedUpdated: Boolean, onUsedToday: () -> Unit, modifier: Modifier = Modifier) {
    DetailCard(modifier) {
        TileContent(
            label = stringResource(R.string.detail_no_use_limit),
            value = DateFormats.date(state.deadlines.noUseEnd, currentLocale()),
        )
        if (usedUpdated) {
            Text(
                stringResource(R.string.detail_used_updated),
                style = SimKeeperTheme.typography.labelLarge,
                color = SimKeeperTheme.status.safe.content,
            )
        } else {
            TextLink(stringResource(R.string.detail_used_today), onClick = onUsedToday)
        }
    }
}

@Composable
private fun TileContent(label: String, value: String) {
    Column(Modifier.semantics(mergeDescendants = true) { }, verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.xs)) {
        Text(label.uppercase(currentLocale()), style = SimKeeperTheme.typography.labelSmall, color = SimKeeperTheme.colors.onSurfaceVariant)
        Text(value, style = SimKeeperTheme.typography.titleMedium, color = SimKeeperTheme.colors.onSurface)
    }
}

@PreviewLightDark
@Composable
private fun DetailHeroPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md)) {
            DetailHero(SampleData.bankState, graceNote = null)
            DeadlineTiles(SampleData.bankState, usedUpdated = false, onUsedToday = {})
            DetailHero(SampleData.expiredState, graceNote = "Airtel usually gives 15 more days after the 90-day period.")
        }
    }
}

@Preview(name = "Large font", fontScale = 2f)
@Composable
private fun DetailHeroLargeFontPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md)) {
            DetailHero(SampleData.bankState, graceNote = null)
            DeadlineTiles(SampleData.bankState, usedUpdated = false, onUsedToday = {})
        }
    }
}
