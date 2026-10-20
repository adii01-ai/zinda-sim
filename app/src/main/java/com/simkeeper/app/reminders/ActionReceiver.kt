package com.simkeeper.app.reminders

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simkeeper.app.data.repo.NotificationLog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** "Remind me tomorrow": snooze 24 h, then one more reminder. */
@AndroidEntryPoint
class ActionReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationLog: NotificationLog

    @Inject lateinit var notificationHelper: NotificationHelper

    @Inject lateinit var scheduler: ReminderScheduler

    @Inject lateinit var clock: Clock

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SNOOZE) return
        val simId = intent.getLongExtra(EXTRA_SIM_ID, -1L)
        if (simId < 0) return
        notificationHelper.cancel(simId)
        val pending = goAsync()
        scope.launch {
            try {
                notificationLog.snooze(simId, LocalDate.now(clock).plusDays(1))
                scheduler.scheduleSnoozeCheck()
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private const val ACTION_SNOOZE = "com.simkeeper.app.action.SNOOZE"
        private const val EXTRA_SIM_ID = "simId"
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun snoozeIntent(context: Context, simId: Long): PendingIntent {
            val intent = Intent(context, ActionReceiver::class.java)
                .setAction(ACTION_SNOOZE)
                .putExtra(EXTRA_SIM_ID, simId)
            return PendingIntent.getBroadcast(
                context,
                NotificationHelper.notificationId(simId),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }
    }
}
