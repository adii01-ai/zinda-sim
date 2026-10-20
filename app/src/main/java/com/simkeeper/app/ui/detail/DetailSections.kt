package com.simkeeper.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.data.repo.REMINDER_OFFSETS
import com.simkeeper.app.domain.model.PlanOffer
import com.simkeeper.app.domain.model.Recharge
import com.simkeeper.app.ui.components.ChipMode
import com.simkeeper.app.ui.components.SelectChip
import com.simkeeper.app.ui.components.TextLink
import com.simkeeper.app.ui.components.planPriceText
import com.simkeeper.app.ui.components.rupeesText
import com.simkeeper.app.ui.format.DateFormats
import com.simkeeper.app.ui.format.currentLocale
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.preview.SampleData
import com.simkeeper.app.ui.theme.SimKeeperTheme
import java.time.YearMonth

private const val HISTORY_PREVIEW = 3

@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        style = SimKeeperTheme.typography.titleMedium,
        color = SimKeeperTheme.colors.onSurface,
        modifier = Modifier.semantics { heading() },
    )
}

/** "₹[price] · 28 days", "Prices as of October 2026", carrier name. Opens the plans sheet. No buy button. */
@Composable
fun CheapestPlanCard(plan: PlanOffer?, pricesAsOf: YearMonth?, carrier: String, onClick: () -> Unit) {
    DetailCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).semantics(mergeDescendants = true) { }, verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.xs)) {
                Text(
                    stringResource(R.string.detail_cheapest_plan).uppercase(currentLocale()),
                    style = SimKeeperTheme.typography.labelSmall,
                    color = SimKeeperTheme.colors.onSurfaceVariant,
                )
                if (plan != null) {
                    Text(planPriceText(plan), style = SimKeeperTheme.typography.titleMedium, color = SimKeeperTheme.colors.onSurface)
                }
                // "Prices as of" only makes sense next to a price.
                val asOf = pricesAsOf?.takeIf { plan?.rupees != null }
                    ?.let { stringResource(R.string.detail_prices_as_of, DateFormats.monthYear(it, currentLocale())) }
                val hint = if (plan?.rupees == null) stringResource(R.string.detail_check_carrier_app) else null
                Text(
                    listOfNotNull(asOf, carrier, hint).joinToString(stringResource(R.string.separator_dot)),
                    style = SimKeeperTheme.typography.bodyMedium,
                    color = SimKeeperTheme.colors.onSurfaceVariant,
                )
            }
            Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = SimKeeperTheme.colors.onSurfaceVariant)
        }
    }
}

fun offsetLabel(offset: Int): Int = when (offset) {
    7 -> R.string.remind_7_days
    3 -> R.string.remind_3_days
    1 -> R.string.remind_1_day
    else -> R.string.remind_on_day
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RemindMeSection(enabled: Set<Int>, onToggle: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md)) {
        SectionTitle(stringResource(R.string.detail_remind_me))
        val gap = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)
        FlowRow(horizontalArrangement = gap, verticalArrangement = gap) {
            REMINDER_OFFSETS.forEach { offset ->
                SelectChip(
                    text = stringResource(offsetLabel(offset)),
                    selected = offset in enabled,
                    onClick = { onToggle(offset) },
                    mode = ChipMode.TOGGLE,
                )
            }
        }
    }
}

@Composable
fun HistorySection(recharges: List<Recharge>, showAll: Boolean, onToggle: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md)) {
        SectionTitle(stringResource(R.string.detail_history))
        DetailCard {
            if (recharges.isEmpty()) {
                Text(stringResource(R.string.detail_history_empty), style = SimKeeperTheme.typography.bodyMedium, color = SimKeeperTheme.colors.onSurfaceVariant)
            }
            val visible = if (showAll) recharges else recharges.take(HISTORY_PREVIEW)
            visible.forEachIndexed { index, recharge ->
                if (index > 0) HorizontalDivider(color = SimKeeperTheme.colors.outline)
                HistoryRow(recharge)
            }
            if (recharges.size > HISTORY_PREVIEW) {
                TextLink(stringResource(if (showAll) R.string.detail_show_less else R.string.detail_see_all), onClick = onToggle)
            }
        }
    }
}

@Composable
private fun HistoryRow(recharge: Recharge) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = SimKeeperTheme.sizes.touchTarget)
            .padding(vertical = SimKeeperTheme.spacing.sm)
            .semantics(mergeDescendants = true) { },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(DateFormats.date(recharge.date, currentLocale()), style = SimKeeperTheme.typography.labelLarge, color = SimKeeperTheme.colors.onSurface)
            Text(
                pluralStringResource(R.plurals.plan_length, recharge.planDays, recharge.planDays),
                style = SimKeeperTheme.typography.bodyMedium,
                color = SimKeeperTheme.colors.onSurfaceVariant,
            )
        }
        Text(
            text = recharge.amountRupees?.let { rupeesText(it) } ?: stringResource(R.string.no_amount),
            style = SimKeeperTheme.typography.labelLarge,
            color = SimKeeperTheme.colors.onSurface,
        )
    }
}

@PreviewLightDark
@Composable
private fun DetailSectionsPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.lg)) {
            CheapestPlanCard(PlanOffer(null, 28), YearMonth.of(2026, 10), "Airtel", onClick = {})
            RemindMeSection(setOf(7, 3, 1, 0), onToggle = {})
            HistorySection(SampleData.bankHistory, showAll = false, onToggle = {})
        }
    }
}
