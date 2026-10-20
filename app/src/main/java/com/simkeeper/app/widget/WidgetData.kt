package com.simkeeper.app.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.glance.appwidget.updateAll
import com.simkeeper.app.MainActivity
import com.simkeeper.app.data.repo.SettingsRepository
import com.simkeeper.app.data.repo.SimRepository
import com.simkeeper.app.domain.SimState
import com.simkeeper.app.domain.Status
import com.simkeeper.app.domain.usecase.ComputeDeadlinesUseCase
import com.simkeeper.app.reminders.NotificationHelper
import com.simkeeper.app.reminders.withAppLanguage
import com.simkeeper.app.ui.theme.ColorPair
import com.simkeeper.app.ui.theme.DarkStatusColors
import com.simkeeper.app.ui.theme.LightStatusColors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun simRepository(): SimRepository
    fun computeDeadlines(): ComputeDeadlinesUseCase
    fun settingsRepository(): SettingsRepository
}

/** What a widget needs: SIM states sorted by days left, and a context in the app language. */
class WidgetData(val sims: List<SimState>, val localized: Context)

suspend fun loadWidgetData(context: Context): WidgetData {
    val entry = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
    val sims = entry.computeDeadlines()(entry.simRepository().getAllSims())
    val language = entry.settingsRepository().current().language
    return WidgetData(sims, context.withAppLanguage(language))
}

/** Opens SIM detail (or Home when [simId] is null). */
fun openAppIntent(context: Context, simId: Long?): Intent {
    val uri = if (simId != null) "${NotificationHelper.DEEP_LINK_BASE}/$simId" else null
    return if (uri != null) {
        Intent(Intent.ACTION_VIEW, Uri.parse(uri), context, MainActivity::class.java)
    } else {
        Intent(context, MainActivity::class.java)
    }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
}

fun Context.isNightMode(): Boolean =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

/** Urgency colours for widgets: light and dark pairs (never dynamic). */
fun statusPair(status: Status, night: Boolean): ColorPair {
    val colors = if (night) DarkStatusColors else LightStatusColors
    return when (status) {
        Status.SAFE -> colors.safe
        Status.SOON -> colors.soon
        Status.URGENT -> colors.urgent
        Status.EXPIRED -> colors.expired
    }
}

/** Refreshes every widget. Called after data changes and by the daily worker. */
@Singleton
class WidgetUpdater @Inject constructor(@ApplicationContext private val context: Context) {
    suspend fun updateAll() {
        AllSimsWidget().updateAll(context)
        SingleSimWidget().updateAll(context)
    }
}
