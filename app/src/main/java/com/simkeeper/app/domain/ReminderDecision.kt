package com.simkeeper.app.domain

import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/** Quiet hours: no reminders from 10 PM to 8 AM. */
val QUIET_START: LocalTime = LocalTime.of(22, 0)
val QUIET_END: LocalTime = LocalTime.of(8, 0)

/** During grace (after the deadline) remind every 2 days until the user acts. */
const val GRACE_REPEAT_DAYS = 2L

/** Reminders are only useful in the last week; a snoozed reminder for a safe SIM is dropped. */
const val SNOOZE_MAX_DAYS_LEFT = 7L

/** Keys stored in the notification log besides the offsets 7, 3, 1, 0. */
object ReminderKeys {
    const val GRACE = -1
    const val SNOOZED = -2
}

data class ReminderInput(
    val daysLeft: Long,
    /** Subset of 7, 3, 1, 0 the user left switched on for this SIM. */
    val enabledOffsets: Set<Int>,
    /** Date each key was last posted (offsets, [ReminderKeys.GRACE], [ReminderKeys.SNOOZED]). */
    val lastNotified: Map<Int, LocalDate>,
    /** "Remind me tomorrow": nothing before this date, then one reminder on it. */
    val snoozedUntil: LocalDate?,
    val today: LocalDate,
    val now: LocalTime,
    val quietHours: Boolean,
)

sealed interface ReminderDecision {
    data object None : ReminderDecision

    /** Something is due but it is quiet hours: try again at 8 AM. */
    data object Defer : ReminderDecision

    /** Post now and log it under [key]. */
    data class Post(val key: Int) : ReminderDecision
}

fun isQuietTime(now: LocalTime): Boolean = now >= QUIET_START || now < QUIET_END

/** Pure rule used by the daily worker. */
fun decideReminder(input: ReminderInput): ReminderDecision {
    val key = dueKey(input) ?: return ReminderDecision.None
    return if (input.quietHours && isQuietTime(input.now)) ReminderDecision.Defer else ReminderDecision.Post(key)
}

private fun dueKey(input: ReminderInput): Int? {
    val snooze = input.snoozedUntil
    if (snooze != null) {
        if (input.today < snooze) return null
        val alreadyToday = input.lastNotified[ReminderKeys.SNOOZED] == input.today
        return ReminderKeys.SNOOZED.takeIf { !alreadyToday && input.daysLeft <= SNOOZE_MAX_DAYS_LEFT }
    }
    if (input.daysLeft < 0) {
        val last = input.lastNotified[ReminderKeys.GRACE]
        val due = last == null || ChronoUnit.DAYS.between(last, input.today) >= GRACE_REPEAT_DAYS
        return ReminderKeys.GRACE.takeIf { due }
    }
    val offset = input.daysLeft.toInt()
    if (input.daysLeft > Int.MAX_VALUE || offset !in input.enabledOffsets) return null
    return offset.takeIf { input.lastNotified[offset] != input.today }
}
