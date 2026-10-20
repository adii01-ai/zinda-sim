package com.simkeeper.app.domain

import com.google.common.truth.Truth.assertThat
import com.simkeeper.app.testing.TODAY
import org.junit.Test
import java.time.LocalTime

class ReminderDecisionTest {

    private val allOffsets = setOf(7, 3, 1, 0)
    private val tenAm: LocalTime = LocalTime.of(10, 0)

    private fun input(
        daysLeft: Long,
        enabled: Set<Int> = allOffsets,
        lastNotified: Map<Int, java.time.LocalDate> = emptyMap(),
        snoozedUntil: java.time.LocalDate? = null,
        now: LocalTime = tenAm,
        quietHours: Boolean = true,
    ) = ReminderInput(daysLeft, enabled, lastNotified, snoozedUntil, TODAY, now, quietHours)

    @Test
    fun firesOnEnabledOffsetsOnly() {
        listOf(7L, 3L, 1L, 0L).forEach { days ->
            assertThat(decideReminder(input(days))).isEqualTo(ReminderDecision.Post(days.toInt()))
        }
        listOf(8L, 6L, 5L, 4L, 2L, 61L).forEach { days ->
            assertThat(decideReminder(input(days))).isEqualTo(ReminderDecision.None)
        }
    }

    @Test
    fun disabledOffsetDoesNotFire() {
        assertThat(decideReminder(input(3, enabled = setOf(7, 1, 0)))).isEqualTo(ReminderDecision.None)
        assertThat(decideReminder(input(7, enabled = emptySet()))).isEqualTo(ReminderDecision.None)
    }

    @Test
    fun notTwiceOnTheSameDay() {
        assertThat(decideReminder(input(3, lastNotified = mapOf(3 to TODAY)))).isEqualTo(ReminderDecision.None)
        // Posted on an earlier day for the same offset (deadline moved): fires again.
        assertThat(decideReminder(input(3, lastNotified = mapOf(3 to TODAY.minusDays(5))))).isEqualTo(ReminderDecision.Post(3))
    }

    @Test
    fun respectsQuietHours() {
        assertThat(decideReminder(input(1, now = LocalTime.of(22, 0)))).isEqualTo(ReminderDecision.Defer)
        assertThat(decideReminder(input(1, now = LocalTime.of(23, 30)))).isEqualTo(ReminderDecision.Defer)
        assertThat(decideReminder(input(1, now = LocalTime.of(7, 59)))).isEqualTo(ReminderDecision.Defer)
        assertThat(decideReminder(input(1, now = LocalTime.of(8, 0)))).isEqualTo(ReminderDecision.Post(1))
        assertThat(decideReminder(input(1, now = LocalTime.of(21, 59)))).isEqualTo(ReminderDecision.Post(1))
    }

    @Test
    fun quietHoursOffPostsAtNight() {
        assertThat(decideReminder(input(1, now = LocalTime.of(23, 0), quietHours = false))).isEqualTo(ReminderDecision.Post(1))
    }

    @Test
    fun nothingDueAtNightIsNoneNotDefer() {
        assertThat(decideReminder(input(20, now = LocalTime.of(23, 0)))).isEqualTo(ReminderDecision.None)
    }

    @Test
    fun graceRepeatsEveryTwoDays() {
        val grace = ReminderKeys.GRACE
        assertThat(decideReminder(input(-1))).isEqualTo(ReminderDecision.Post(grace))
        assertThat(decideReminder(input(-2, lastNotified = mapOf(grace to TODAY.minusDays(1))))).isEqualTo(ReminderDecision.None)
        assertThat(decideReminder(input(-3, lastNotified = mapOf(grace to TODAY.minusDays(2))))).isEqualTo(ReminderDecision.Post(grace))
        assertThat(decideReminder(input(-3, lastNotified = mapOf(grace to TODAY)))).isEqualTo(ReminderDecision.None)
    }

    @Test
    fun snoozeWaitsThenFiresOnce() {
        val snoozed = ReminderKeys.SNOOZED
        assertThat(decideReminder(input(3, snoozedUntil = TODAY.plusDays(1)))).isEqualTo(ReminderDecision.None)
        assertThat(decideReminder(input(2, snoozedUntil = TODAY))).isEqualTo(ReminderDecision.Post(snoozed))
        assertThat(decideReminder(input(2, snoozedUntil = TODAY, lastNotified = mapOf(snoozed to TODAY)))).isEqualTo(ReminderDecision.None)
    }

    @Test
    fun snoozeIsDroppedOnceTheSimIsSafe() {
        assertThat(decideReminder(input(28, snoozedUntil = TODAY))).isEqualTo(ReminderDecision.None)
    }
}
