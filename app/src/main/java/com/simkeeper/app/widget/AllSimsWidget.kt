package com.simkeeper.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.simkeeper.app.R
import com.simkeeper.app.domain.SimState
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.ui.components.carrierNameRes
import com.simkeeper.app.ui.format.DateFormats
import com.simkeeper.app.ui.format.currentLocale
import com.simkeeper.app.ui.theme.DarkStatusColors
import com.simkeeper.app.ui.theme.LightStatusColors
import com.simkeeper.app.ui.theme.SimKeeperExtendedTypography
import com.simkeeper.app.ui.theme.SimKeeperTypography
import com.simkeeper.app.ui.theme.Spacing

private const val MAX_TILES = 3
private val spacing = Spacing()

/** 4×2 "My SIMs": header + up to 3 tiles (days in status colour, carrier, SIM name). Tap → SIM detail. */
class AllSimsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = loadWidgetData(context)
        provideContent {
            GlanceTheme(colors = WidgetColors) { AllSimsContent(data) }
        }
    }
}

@Composable
private fun AllSimsContent(data: WidgetData) {
    val res = data.localized
    val needs = data.sims.count { it.needsRecharge }
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .cornerRadius(spacing.xxl)
            .padding(spacing.md)
            .clickable(actionStartActivity(openAppIntent(res, null))),
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                res.getString(R.string.widget_all_title),
                style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = SimKeeperTypography.titleMedium.fontSize, fontWeight = FontWeight.Bold),
                modifier = GlanceModifier.defaultWeight(),
            )
            if (needs > 0) {
                Text(
                    res.resources.getQuantityString(R.plurals.widget_needs_recharge, needs, needs),
                    style = TextStyle(
                        color = ColorProvider(LightStatusColors.urgent.content, DarkStatusColors.urgent.content),
                        fontSize = SimKeeperTypography.labelSmall.fontSize,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
        Spacer(GlanceModifier.height(spacing.sm))
        if (data.sims.isEmpty()) {
            Text(res.getString(R.string.widget_empty), style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant))
        } else {
            Row(modifier = GlanceModifier.fillMaxSize()) {
                data.sims.take(MAX_TILES).forEachIndexed { index, sim ->
                    if (index > 0) Spacer(GlanceModifier.width(spacing.sm))
                    SimTile(sim, res, GlanceModifier.defaultWeight().fillMaxHeight())
                }
            }
        }
    }
}

@Composable
private fun SimTile(state: SimState, res: Context, modifier: GlanceModifier) {
    val light = statusPair(state.status, night = false)
    val dark = statusPair(state.status, night = true)
    val content = ColorProvider(light.content, dark.content)
    val sim = state.sim
    val locale = res.currentLocale
    val carrier = if (sim.carrier == Carrier.OTHER && !sim.carrierLabel.isNullOrBlank()) sim.carrierLabel else res.getString(carrierNameRes(sim.carrier))
    val small = SimKeeperTypography.labelSmall.fontSize
    Column(
        modifier = modifier
            .background(ColorProvider(light.container, dark.container))
            .cornerRadius(spacing.lg)
            .padding(spacing.sm)
            .clickable(actionStartActivity(openAppIntent(res, sim.id))),
    ) {
        Text(
            res.getString(R.string.widget_tile_label, carrier, sim.name).uppercase(locale),
            style = TextStyle(color = content, fontSize = small, fontWeight = FontWeight.Bold),
            maxLines = 1,
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                state.daysLeft.coerceAtLeast(0).toString(),
                style = TextStyle(color = content, fontSize = SimKeeperExtendedTypography.previewNumber.fontSize, fontWeight = FontWeight.Bold),
            )
            Spacer(GlanceModifier.width(spacing.xs))
            Text(res.getString(R.string.widget_days), style = TextStyle(color = content, fontSize = small))
        }
        Text(
            res.getString(R.string.card_ends_line, DateFormats.dayMonth(state.deadlines.effective, locale)),
            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = small),
            maxLines = 1,
        )
    }
}
