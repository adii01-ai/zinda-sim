package com.simkeeper.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.simkeeper.app.R
import com.simkeeper.app.domain.SimState
import com.simkeeper.app.ui.theme.SimKeeperExtendedTypography
import com.simkeeper.app.ui.theme.SimKeeperTypography
import com.simkeeper.app.ui.theme.Sizes
import com.simkeeper.app.ui.theme.Spacing

private val spacing = Spacing()
private val sizes = Sizes()

/** Glance state key: which SIM this widget shows (set by [SingleSimConfigActivity]). */
val SimIdKey: Preferences.Key<Long> = longPreferencesKey("simId")

/** 2×2 single SIM: ring 104 + number + "days" + SIM name. */
class SingleSimWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val simId = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)[SimIdKey]
        val data = loadWidgetData(context)
        val sim = data.sims.firstOrNull { it.sim.id == simId }
        val night = context.isNightMode()
        val ringPx = (sizes.ringWidget.value * context.resources.displayMetrics.density).toInt()
        val strokePx = sizes.ringWidgetStroke.value * context.resources.displayMetrics.density
        val ring = sim?.let { ringBitmap(ringPx, strokePx, it.ringProgress, statusPair(it.status, night)) }
        provideContent {
            GlanceTheme(colors = WidgetColors) {
                SingleSimContent(sim, ring, data)
            }
        }
    }
}

@Composable
private fun SingleSimContent(state: SimState?, ring: android.graphics.Bitmap?, data: WidgetData) {
    val res = data.localized
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .cornerRadius(spacing.xxl)
            .padding(spacing.sm)
            .clickable(actionStartActivity(openAppIntent(res, state?.sim?.id))),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state == null || ring == null) {
            Text(res.getString(R.string.widget_empty), style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, textAlign = TextAlign.Center))
            return@Column
        }
        val light = statusPair(state.status, night = false)
        val dark = statusPair(state.status, night = true)
        Box(modifier = GlanceModifier.size(sizes.ringWidget), contentAlignment = Alignment.Center) {
            Image(provider = ImageProvider(ring), contentDescription = null, modifier = GlanceModifier.size(sizes.ringWidget))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    state.daysLeft.coerceAtLeast(0).toString(),
                    style = TextStyle(
                        color = ColorProvider(light.content, dark.content),
                        fontSize = SimKeeperExtendedTypography.previewNumber.fontSize,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    res.getString(R.string.widget_days),
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = SimKeeperTypography.labelSmall.fontSize),
                )
            }
        }
        Text(
            state.sim.name,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = SimKeeperTypography.bodyMedium.fontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
        )
    }
}
