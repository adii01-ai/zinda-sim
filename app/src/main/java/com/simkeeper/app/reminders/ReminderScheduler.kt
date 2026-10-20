package com.simkeeper.app.reminders

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.simkeeper.app.data.repo.SettingsRepository
import com.simkeeper.app.domain.QUIET_END
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Daily check aimed at the user's reminder time (24 h period, 1 h flex), plus one-time checks
 * when data changes, after quiet hours and after a snooze. WorkManager survives reboots.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) {
    private val workManager get() = WorkManager.getInstance(context)

    /** (Re)aims the daily check. Call on app start and when the reminder time changes. */
    suspend fun schedule() {
        val time = settingsRepository.current().reminderTime
        val now = LocalDateTime.now(clock)
        var target = now.toLocalDate().atTime(time)
        if (!target.isAfter(now)) target = target.plusDays(1)
        // A periodic request with flex first runs after initialDelay + (period - flex),
        // so start the period (period - flex) earlier to land the flex window on the reminder time.
        val untilTarget = Duration.between(now, target)
        val initialDelay = untilTarget.minus(PERIOD.minus(FLEX)).let { if (it.isNegative) it.plus(PERIOD) else it }
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(PERIOD.toHours(), TimeUnit.HOURS, FLEX.toHours(), TimeUnit.HOURS)
            .setInitialDelay(initialDelay.toMinutes(), TimeUnit.MINUTES)
            .build()
        workManager.enqueueUniquePeriodicWork(DAILY_WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
        // The periodic window first opens a day later, so cover the very next reminder time too.
        scheduleAt(target, NEXT_WORK)
    }

    /** Data changed: check right away. */
    fun checkNow() {
        workManager.enqueueUniqueWork(NOW_WORK, ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<ReminderWorker>().build())
    }

    /** Something was due during quiet hours: check again at 8 AM. */
    fun scheduleAfterQuietHours() = scheduleAt(nextOccurrence(QUIET_END.hour), QUIET_WORK)

    /** "Remind me tomorrow": make sure a check runs tomorrow at the reminder time. */
    suspend fun scheduleSnoozeCheck() {
        val time = settingsRepository.current().reminderTime
        scheduleAt(LocalDateTime.now(clock).toLocalDate().plusDays(1).atTime(time), SNOOZE_WORK)
    }

    private fun nextOccurrence(hour: Int): LocalDateTime {
        val now = LocalDateTime.now(clock)
        val today = now.toLocalDate().atTime(hour, 0)
        return if (today.isAfter(now)) today else today.plusDays(1)
    }

    private fun scheduleAt(at: LocalDateTime, name: String) {
        val delay = Duration.between(LocalDateTime.now(clock), at).coerceAtLeast(Duration.ZERO)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>().setInitialDelay(delay.toMinutes(), TimeUnit.MINUTES).build()
        workManager.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request)
    }

    private companion object {
        const val DAILY_WORK = "daily-reminder-check"
        const val NOW_WORK = "reminder-check-now"
        const val QUIET_WORK = "reminder-check-after-quiet-hours"
        const val SNOOZE_WORK = "reminder-check-snooze"
        const val NEXT_WORK = "reminder-check-next"
        val PERIOD: Duration = Duration.ofHours(24)
        val FLEX: Duration = Duration.ofHours(1)
    }
}
