package com.simkeeper.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.domain.model.PlanOffer
import com.simkeeper.app.ui.components.planPriceText
import com.simkeeper.app.ui.format.DateFormats
import com.simkeeper.app.ui.format.currentLocale
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.theme.SimKeeperTheme
import java.time.YearMonth

/** Up to 3 cheapest plans from the bundled JSON. Information only: no buy button. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansSheet(carrier: String, plans: List<PlanOffer>, pricesAsOf: YearMonth?, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = SimKeeperTheme.shapes.bottomSheet,
        containerColor = SimKeeperTheme.colors.surface,
    ) {
        PlansSheetBody(carrier, plans, pricesAsOf)
    }
}

@Composable
private fun PlansSheetBody(carrier: String, plans: List<PlanOffer>, pricesAsOf: YearMonth?) {
    val spacing = SimKeeperTheme.spacing
    Column(
        modifier = Modifier
            .padding(horizontal = spacing.screen)
            .padding(bottom = spacing.xxl)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Text(
            stringResource(R.string.detail_plans_title, carrier),
            style = SimKeeperTheme.typography.titleLarge,
            color = SimKeeperTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        plans.forEachIndexed { index, plan ->
            if (index > 0) HorizontalDivider(color = SimKeeperTheme.colors.outline)
            Text(planPriceText(plan), style = SimKeeperTheme.typography.labelLarge, color = SimKeeperTheme.colors.onSurface)
        }
        val anyPrice = plans.any { it.rupees != null }
        if (!anyPrice) {
            Text(stringResource(R.string.detail_check_carrier_app), style = SimKeeperTheme.typography.bodyMedium, color = SimKeeperTheme.colors.onSurfaceVariant)
        }
        pricesAsOf?.takeIf { anyPrice }?.let {
            Text(
                stringResource(R.string.detail_prices_as_of, DateFormats.monthYear(it, currentLocale())),
                style = SimKeeperTheme.typography.bodyMedium,
                color = SimKeeperTheme.colors.onSurfaceVariant,
            )
        }
        Text(stringResource(R.string.detail_plans_body), style = SimKeeperTheme.typography.bodyMedium, color = SimKeeperTheme.colors.onSurfaceVariant)
    }
}

@PreviewLightDark
@Composable
private fun PlansSheetPreview() {
    PreviewSurface { PlansSheetBody("Airtel", listOf(PlanOffer(null, 28)), YearMonth.of(2026, 10)) }
}
