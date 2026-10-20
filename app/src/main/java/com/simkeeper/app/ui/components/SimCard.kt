package com.simkeeper.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.domain.SimState
import com.simkeeper.app.domain.model.PlanOffer
import com.simkeeper.app.domain.model.Sim
import com.simkeeper.app.ui.format.DateFormats
import com.simkeeper.app.ui.format.currentLocale
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.preview.SampleData
import com.simkeeper.app.ui.theme.SimKeeperTheme

/**
 * Home card: ring · name + carrier tag · "Slot 2 · ends 21 Oct 2026" · status line · chevron.
 * SIMs that need a recharge add a footer with the cheapest plan and "I recharged".
 * The whole card is one TalkBack node; "I recharged" is a custom action on it.
 */
@Composable
fun SimCard(
    state: SimState,
    cheapestPlan: PlanOffer?,
    onClick: () -> Unit,
    onRecharged: () -> Unit,
    modifier: Modifier = Modifier,
    animateRing: Boolean = true,
) {
    val description = simCardDescription(state)
    val openLabel = stringResource(R.string.action_open_details)
    val rechargedLabel = stringResource(R.string.i_recharged)
    val showFooter = state.needsRecharge
    Card(
        onClick = onClick,
        shape = SimKeeperTheme.shapes.card,
        colors = CardDefaults.cardColors(containerColor = SimKeeperTheme.colors.surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (SimKeeperTheme.isDark) SimKeeperTheme.sizes.noElevation else SimKeeperTheme.sizes.cardElevation,
        ),
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = description
                onClick(label = openLabel) { onClick(); true }
                if (showFooter) {
                    customActions = listOf(CustomAccessibilityAction(rechargedLabel) { onRecharged(); true })
                }
            },
    ) {
        Column(modifier = Modifier.padding(SimKeeperTheme.sizes.cardPadding)) {
            SimCardMain(state, animateRing)
            if (showFooter) {
                HorizontalDivider(
                    color = SimKeeperTheme.colors.outline,
                    modifier = Modifier.padding(vertical = SimKeeperTheme.spacing.md),
                )
                SimCardFooter(cheapestPlan, onRecharged)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SimCardMain(state: SimState, animateRing: Boolean) {
    val sim = state.sim
    val status = statusColors(state.status)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.lg)) {
        DaysRing(
            days = state.daysLeft,
            progress = state.ringProgress,
            status = state.status,
            size = RingSize.CARD,
            animateOnAppear = animateRing,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.xxs)) {
            // Flow, not Row: with large text the tag wraps under the name instead of squeezing it.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.xxs),
            ) {
                Text(
                    text = sim.name,
                    style = SimKeeperTheme.typography.titleMedium,
                    color = SimKeeperTheme.colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
                CarrierTag(sim.carrier, sim.carrierLabel, Modifier.align(Alignment.CenterVertically))
            }
            Text(
                text = simSubtitle(sim, state),
                style = SimKeeperTheme.typography.bodyMedium,
                color = SimKeeperTheme.colors.onSurfaceVariant,
            )
            Text(text = statusLine(state), style = SimKeeperTheme.typography.bodyMedium, color = status.content)
        }
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = SimKeeperTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(SimKeeperTheme.sizes.icon),
        )
    }
}

/** "Slot 2 · ends 21 Oct 2026", "Family SIM · ends 31 Oct 2026" or "Ends 21 Oct 2026". */
@Composable
fun simSubtitle(sim: Sim, state: SimState): String {
    val ends = DateFormats.date(state.deadlines.effective, currentLocale())
    return when {
        sim.isFamily -> stringResource(R.string.card_family_line, ends)
        sim.slot != null -> stringResource(R.string.card_slot_line, sim.slot, ends)
        else -> stringResource(R.string.card_ends_line, ends)
    }
}

@Composable
private fun SimCardFooter(plan: PlanOffer?, onRecharged: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.card_cheapest_plan),
                style = SimKeeperTheme.typography.bodyMedium,
                color = SimKeeperTheme.colors.onSurfaceVariant,
            )
            Text(
                text = if (plan != null) planPriceText(plan) else stringResource(R.string.detail_check_carrier_app),
                style = SimKeeperTheme.typography.labelLarge,
                color = SimKeeperTheme.colors.onSurface,
            )
        }
        SmallPrimaryButton(text = stringResource(R.string.i_recharged), onClick = onRecharged)
    }
}

/** "₹149 · 28 days", or "₹— · 28 days" until a verified price is bundled. */
@Composable
fun planPriceText(plan: PlanOffer): String = stringResource(
    R.string.card_plan_price,
    rupeesText(plan.rupees),
    pluralStringResource(R.plurals.days_count, plan.days, plan.days),
)

@Composable
fun rupeesText(rupees: Int?): String =
    if (rupees == null) stringResource(R.string.money_unknown) else stringResource(R.string.money_rupees, rupees)

@PreviewLightDark
@Composable
private fun SimCardPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md)) {
            val plan = PlanOffer(rupees = null, days = 28)
            SimCard(SampleData.bankState, plan, onClick = {}, onRecharged = {}, animateRing = false)
            SimCard(SampleData.dailyState, plan, onClick = {}, onRecharged = {}, animateRing = false)
            SimCard(SampleData.mummyState, plan, onClick = {}, onRecharged = {}, animateRing = false)
        }
    }
}

@PreviewLightDark
@Composable
private fun SimCardStatesPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md)) {
            SimCard(SampleData.soonState, PlanOffer(rupees = 199, days = 28), onClick = {}, onRecharged = {}, animateRing = false)
            SimCard(SampleData.expiredState, PlanOffer(rupees = null, days = 28), onClick = {}, onRecharged = {}, animateRing = false)
        }
    }
}
