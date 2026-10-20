package com.simkeeper.app.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.simkeeper.app.R
import com.simkeeper.app.domain.SimState
import com.simkeeper.app.ui.components.SectionLabel
import com.simkeeper.app.ui.components.SimCard
import com.simkeeper.app.ui.components.StatusBanner
import com.simkeeper.app.ui.theme.SimKeeperTheme

/** Banner, battery guide, "YOUR SIMS" and "FAMILY" sections. */
fun LazyListScope.homeContent(state: HomeUiState, actions: HomeActions, animate: Boolean) {
    item(key = "banner") {
        val text = if (state.needCount > 0) {
            pluralStringResource(R.plurals.home_banner_needs_recharge, state.needCount, state.needCount)
        } else {
            stringResource(R.string.home_banner_all_safe)
        }
        StatusBanner(text = text, status = state.bannerStatus)
    }
    if (state.showBatteryGuide) {
        item(key = "battery") {
            BatteryGuideCard(state.manufacturer, actions.onOpenBatterySettings, actions.onDismissBatteryGuide)
        }
    }
    simSection("sims", R.string.home_section_sims, state.phoneSims, state, actions, animate, firstIndex = 0)
    simSection("family", R.string.home_section_family, state.familySims, state, actions, animate, firstIndex = state.phoneSims.size)
}

private fun LazyListScope.simSection(
    key: String,
    title: Int,
    sims: List<SimState>,
    state: HomeUiState,
    actions: HomeActions,
    animate: Boolean,
    firstIndex: Int,
) {
    if (sims.isEmpty()) return
    item(key = "label-$key") {
        SectionLabel(stringResource(title), modifier = Modifier.padding(top = SimKeeperTheme.spacing.sm))
    }
    itemsIndexed(sims, key = { _, s -> s.sim.id }) { index, simState ->
        StaggeredItem(index = firstIndex + index, animate = animate) {
            SimCard(
                state = simState,
                cheapestPlan = state.plans[simState.sim.carrier],
                onClick = { actions.onOpenSim(simState.sim.id) },
                onRecharged = { actions.onRecharged(simState.sim.id) },
                animateRing = animate,
            )
        }
    }
}
