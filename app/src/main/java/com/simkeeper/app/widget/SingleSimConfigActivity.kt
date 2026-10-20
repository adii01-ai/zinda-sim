package com.simkeeper.app.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.simkeeper.app.R
import com.simkeeper.app.domain.SimState
import com.simkeeper.app.ui.components.MonogramTile
import com.simkeeper.app.ui.components.statusColors
import com.simkeeper.app.ui.components.statusLine
import com.simkeeper.app.ui.theme.SimKeeperTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

/** Picks which SIM a 2×2 widget shows. */
@AndroidEntryPoint
class SingleSimConfigActivity : ComponentActivity() {

    private val appWidgetId by lazy {
        intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED, resultIntent())
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        setContent { SimKeeperTheme { ConfigContent(onPick = ::onPick) } }
    }

    private suspend fun onPick(simId: Long) {
        val glanceId = GlanceAppWidgetManager(this).getGlanceIdBy(appWidgetId)
        updateAppWidgetState(this, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply { this[SimIdKey] = simId }
        }
        SingleSimWidget().update(this, glanceId)
        setResult(RESULT_OK, resultIntent())
        finish()
    }

    @Composable
    private fun PickRow(state: SimState, onClick: () -> Unit) {
        Card(
            onClick = onClick,
            shape = SimKeeperTheme.shapes.card,
            colors = CardDefaults.cardColors(containerColor = SimKeeperTheme.colors.surface),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(SimKeeperTheme.sizes.cardPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md),
            ) {
                MonogramTile(state.sim.carrier, state.sim.carrierLabel)
                Column(Modifier.weight(1f)) {
                    Text(state.sim.name, style = SimKeeperTheme.typography.titleMedium, color = SimKeeperTheme.colors.onSurface)
                    Text(statusLine(state), style = SimKeeperTheme.typography.bodyMedium, color = statusColors(state.status).content)
                }
            }
        }
    }

    private fun resultIntent() = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

    @Composable
    private fun ConfigContent(onPick: suspend (Long) -> Unit) {
        var sims by remember { mutableStateOf<List<SimState>>(emptyList()) }
        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            val entry = EntryPointAccessors.fromApplication(applicationContext, WidgetEntryPoint::class.java)
            sims = entry.computeDeadlines()(entry.simRepository().getAllSims())
        }
        val spacing = SimKeeperTheme.spacing
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SimKeeperTheme.colors.background)
                .safeDrawingPadding()
                .padding(horizontal = spacing.screen, vertical = spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            Text(stringResource(R.string.widget_config_title), style = SimKeeperTheme.typography.headlineMedium, color = SimKeeperTheme.colors.onSurface)
            if (sims.isEmpty()) {
                Text(stringResource(R.string.widget_empty), style = SimKeeperTheme.typography.bodyLarge, color = SimKeeperTheme.colors.onSurfaceVariant)
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                items(sims, key = { it.sim.id }) { state ->
                    PickRow(state, onClick = { scope.launch { onPick(state.sim.id) } })
                }
            }
        }
    }
}
