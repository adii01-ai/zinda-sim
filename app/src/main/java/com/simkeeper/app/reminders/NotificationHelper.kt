package com.simkeeper.app.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.simkeeper.app.MainActivity
import com.simkeeper.app.R
import com.simkeeper.app.domain.Reason
import com.simkeeper.app.domain.ReminderKeys
import com.simkeeper.app.domain.SimState
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.ui.format.DateFormats
import com.simkeeper.app.ui.format.currentLocale
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Channels and the reminder notification (SCREENS.md #11). Calm copy, no "deactivated" before expiry. */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(CHANNEL_REMINDERS, context.getString(R.string.channel_reminders), NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description = context.getString(R.string.channel_reminders_body) },
                NotificationChannel(CHANNEL_URGENT, context.getString(R.string.channel_urgent), NotificationManager.IMPORTANCE_HIGH)
                    .apply { description = context.getString(R.string.channel_urgent_body) },
            ),
        )
    }

    /** POST_NOTIFICATIONS only exists on Android 13+; before that, the app-level switch decides. */
    fun canPost(): Boolean {
        val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        return granted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /** Posts "Bank SIM (Airtel): 2 days left" / "Recharge by 21 Oct to keep your OTPs working." */
    fun post(state: SimState, key: Int, languageTag: String?) {
        if (!canPost()) return
        val res = context.withAppLanguage(languageTag)
        val sim = state.sim
        val days = state.daysLeft.toInt()
        val urgent = key == ReminderKeys.GRACE || days <= 1
        val carrier = if (sim.carrier == Carrier.OTHER && !sim.carrierLabel.isNullOrBlank()) sim.carrierLabel else res.getString(carrierRes(sim.carrier))
        val title = res.getString(R.string.notif_title, sim.name, carrier, titleSuffix(res, days))
        val body = body(res, state)
        val notification = NotificationCompat.Builder(context, if (urgent) CHANNEL_URGENT else CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_sim_card)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(if (urgent) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(deepLink(sim.id, action = null))
            .addAction(0, res.getString(R.string.i_recharged), deepLink(sim.id, action = ACTION_RECHARGED))
            .addAction(0, res.getString(R.string.notif_remind_tomorrow), ActionReceiver.snoozeIntent(context, sim.id))
            .build()
        @Suppress("MissingPermission") // checked in canPost()
        NotificationManagerCompat.from(context).notify(notificationId(sim.id), notification)
    }

    fun cancel(simId: Long) = NotificationManagerCompat.from(context).cancel(notificationId(simId))

    private fun titleSuffix(res: Context, days: Int): String = when {
        days < 0 -> res.getString(R.string.notif_plan_ended)
        days == 0 -> res.getString(R.string.notif_last_day)
        else -> res.resources.getQuantityString(R.plurals.days_left, days, days)
    }

    private fun body(res: Context, state: SimState): String {
        val days = state.daysLeft
        val date = DateFormats.dayMonth(state.deadlines.effective, res.currentLocale)
        val noUse = state.deadlines.reason == Reason.NO_USE
        return when {
            days < 0 -> res.getString(R.string.notif_body_grace)
            noUse && days == 0L -> res.getString(R.string.notif_body_no_use_today)
            noUse -> res.getString(R.string.notif_body_no_use, date)
            days == 0L -> res.getString(R.string.notif_body_today)
            else -> res.getString(R.string.notif_body, date)
        }
    }

    private fun deepLink(simId: Long, action: String?): PendingIntent {
        val uri = if (action == null) "$DEEP_LINK_BASE/$simId" else "$DEEP_LINK_BASE/$simId?action=$action"
        // NEW_TASK + CLEAR_TASK: Navigation builds Home → SIM detail in one clean start.
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri), context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val requestCode = notificationId(simId) * 2 + if (action == null) 0 else 1
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun carrierRes(carrier: Carrier): Int = when (carrier) {
        Carrier.JIO -> R.string.carrier_jio
        Carrier.AIRTEL -> R.string.carrier_airtel
        Carrier.VI -> R.string.carrier_vi
        Carrier.BSNL -> R.string.carrier_bsnl
        Carrier.OTHER -> R.string.carrier_other
    }

    companion object {
        const val CHANNEL_REMINDERS = "recharge_reminders"
        const val CHANNEL_URGENT = "recharge_reminders_urgent"
        const val DEEP_LINK_BASE = "simkeeper://sim"
        const val ACTION_RECHARGED = "recharged"

        fun notificationId(simId: Long): Int = simId.toInt()
    }
}
