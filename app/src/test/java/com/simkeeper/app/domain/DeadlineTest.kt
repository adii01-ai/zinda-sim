package com.simkeeper.app.domain

import com.google.common.truth.Truth.assertThat
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.domain.model.Sim
import org.junit.Test
import java.time.LocalDate

class DeadlineTest {

    private val today = LocalDate.of(2026, 10, 19)

    private fun sim(
        planDays: Int,
        lastRecharge: LocalDate,
        lastUsed: LocalDate = today,
        exactEnd: LocalDate? = null,
    ) = Sim(
        id = 1,
        name = "Test",
        carrier = Carrier.AIRTEL,
        planDays = planDays,
        lastRechargeDate = lastRecharge,
        exactEndDate = exactEnd,
        lastUsedDate = lastUsed,
        createdAt = 0,
    )

    // Worked examples from DATA_MODEL.md (today = 2026-10-19).

    @Test
    fun bankSim() {
        val d = deadlinesOf(sim(28, LocalDate.of(2026, 9, 23), lastUsed = LocalDate.of(2026, 10, 15)))
        assertThat(d.planEnd).isEqualTo(LocalDate.of(2026, 10, 21))
        assertThat(d.noUseEnd).isEqualTo(LocalDate.of(2027, 1, 13))
        assertThat(daysLeft(d.effective, today)).isEqualTo(2)
        assertThat(status(daysLeft(d.effective, today))).isEqualTo(Status.URGENT)
        assertThat(d.reason).isEqualTo(Reason.PLAN)
    }

    @Test
    fun dailySim() {
        val d = deadlinesOf(sim(84, LocalDate.of(2026, 9, 26)))
        assertThat(d.planEnd).isEqualTo(LocalDate.of(2026, 12, 19))
        assertThat(d.noUseEnd).isEqualTo(LocalDate.of(2027, 1, 17))
        assertThat(daysLeft(d.effective, today)).isEqualTo(61)
        assertThat(status(61)).isEqualTo(Status.SAFE)
    }

    @Test
    fun mummysPhone() {
        val d = deadlinesOf(sim(28, LocalDate.of(2026, 10, 3)))
        assertThat(d.planEnd).isEqualTo(LocalDate.of(2026, 10, 31))
        assertThat(d.noUseEnd).isEqualTo(LocalDate.of(2027, 1, 17))
        assertThat(daysLeft(d.effective, today)).isEqualTo(12)
        assertThat(status(12)).isEqualTo(Status.SAFE)
    }

    @Test
    fun longPlanUnused_noUseLimitWins() {
        val d = deadlinesOf(sim(365, LocalDate.of(2026, 6, 1), lastUsed = LocalDate.of(2026, 8, 1)))
        assertThat(d.planEnd).isEqualTo(LocalDate.of(2027, 6, 1))
        assertThat(d.noUseEnd).isEqualTo(LocalDate.of(2026, 10, 30))
        assertThat(d.effective).isEqualTo(LocalDate.of(2026, 10, 30))
        assertThat(daysLeft(d.effective, today)).isEqualTo(11)
        assertThat(status(11)).isEqualTo(Status.SAFE)
        assertThat(d.reason).isEqualTo(Reason.NO_USE)
    }

    @Test
    fun expired() {
        val d = deadlinesOf(sim(28, LocalDate.of(2026, 9, 1), lastUsed = LocalDate.of(2026, 9, 1)))
        assertThat(d.planEnd).isEqualTo(LocalDate.of(2026, 9, 29))
        assertThat(daysLeft(d.effective, today)).isEqualTo(-20)
        assertThat(status(-20)).isEqualTo(Status.EXPIRED)
    }

    @Test
    fun justRecharged() {
        val d = deadlinesOf(sim(28, today))
        assertThat(d.planEnd).isEqualTo(LocalDate.of(2026, 11, 16))
        assertThat(d.noUseEnd).isEqualTo(LocalDate.of(2027, 1, 17))
        assertThat(daysLeft(d.effective, today)).isEqualTo(28)
        assertThat(status(28)).isEqualTo(Status.SAFE)
    }

    @Test
    fun exactEndDateOverridesPlanLength() {
        val end = LocalDate.of(2026, 11, 2)
        val d = deadlinesOf(sim(28, LocalDate.of(2026, 9, 1), exactEnd = end))
        assertThat(d.planEnd).isEqualTo(end)
        assertThat(daysLeft(d.effective, today)).isEqualTo(14)
    }

    @Test
    fun equalDeadlines_reasonIsPlan() {
        val d = Deadlines(planEnd = today, noUseEnd = today)
        assertThat(d.reason).isEqualTo(Reason.PLAN)
    }

    // Status boundaries.

    @Test
    fun statusBoundaries() {
        assertThat(status(8)).isEqualTo(Status.SAFE)
        assertThat(status(7)).isEqualTo(Status.SOON)
        assertThat(status(3)).isEqualTo(Status.SOON)
        assertThat(status(2)).isEqualTo(Status.URGENT)
        assertThat(status(0)).isEqualTo(Status.URGENT)
        assertThat(status(-1)).isEqualTo(Status.EXPIRED)
    }

    @Test
    fun leapYear() {
        val leapToday = LocalDate.of(2028, 2, 28)
        val d = deadlinesOf(sim(28, LocalDate.of(2028, 2, 1), lastUsed = leapToday))
        assertThat(d.planEnd).isEqualTo(LocalDate.of(2028, 2, 29))
        assertThat(daysLeft(d.effective, leapToday)).isEqualTo(1)
        assertThat(daysLeft(LocalDate.of(2028, 3, 1), LocalDate.of(2028, 2, 28))).isEqualTo(2)
    }

    // Ring progress.

    @Test
    fun ringProgressClamps() {
        assertThat(ringProgress(-5, 28)).isEqualTo(0f)
        assertThat(ringProgress(0, 28)).isEqualTo(0f)
        assertThat(ringProgress(14, 28)).isEqualTo(0.5f)
        assertThat(ringProgress(28, 28)).isEqualTo(1f)
        assertThat(ringProgress(61, 28)).isEqualTo(1f)
    }

    @Test
    fun ringProgressWithZeroPlanDaysDoesNotCrash() {
        assertThat(ringProgress(5, 0)).isEqualTo(1f)
    }
}
