package com.simkeeper.app.reminders

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simkeeper.app.data.repo.NotificationLog
import com.simkeeper.app.data.repo.SettingsRepository
import com.simkeeper.app.data.repo.SimRepository
import com.simkeeper.app.domain.ReminderDecision
import com.simkeeper.app.domain.ReminderInput
import com.simkeeper.app.domain.ReminderKeys
import com.simkeeper.app.domain.decideReminder
import com.simkeeper.app.domain.simStateOf
import com.simkeeper.app.widget.WidgetUpdater
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Clock
import java.time.LocalDateTime

/** Daily (and on-demand) check: posts due reminders and refreshes widgets. */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val simRepository: SimRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationLog: NotificationLog,
    private val notificationHelper: NotificationHelper,
    private val scheduler: ReminderScheduler,
    private val widgetUpdater: WidgetUpdater,
    private val clock: Clock,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = settingsRepository.current()
        val now = LocalDateTime.now(clock)
        val today = now.toLocalDate()
        val offsetsBySim = simRepository.enabledOffsetsBySim()
        var deferred = false

        simRepository.getAllSims().forEach { sim ->
            val state = simStateOf(sim, today)
            val keys = offsetsBySim[sim.id].orEmpty() + listOf(ReminderKeys.GRACE, ReminderKeys.SNOOZED)
            val snoozedUntil = notificationLog.snoozedUntil(sim.id)
            val input = ReminderInput(
                daysLeft = state.daysLeft,
                enabledOffsets = offsetsBySim[sim.id].orEmpty(),
                lastNotified = keys.mapNotNull { key -> notificationLog.lastNotified(sim.id, key)?.let { key to it } }.toMap(),
                snoozedUntil = snoozedUntil,
                today = today,
                now = now.toLocalTime(),
                quietHours = settings.quietHours,
            )
            when (val decision = decideReminder(input)) {
                is ReminderDecision.Post -> if (notificationHelper.canPost()) {
                    notificationHelper.post(state, decision.key, settings.language)
                    notificationLog.markNotified(sim.id, decision.key, today)
                    if (decision.key == ReminderKeys.SNOOZED) notificationLog.clearSnooze(sim.id)
                }
                ReminderDecision.Defer -> deferred = true
                ReminderDecision.None -> if (snoozedUntil != null && today >= snoozedUntil) notificationLog.clearSnooze(sim.id)
            }
        }

        if (deferred) scheduler.scheduleAfterQuietHours()
        widgetUpdater.updateAll()
        return Result.success()
    }
}
