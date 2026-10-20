package com.simkeeper.app.ui.setup

import com.google.common.truth.Truth.assertThat
import com.simkeeper.app.domain.Status
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.domain.model.Sim
import com.simkeeper.app.domain.planEnd
import com.simkeeper.app.testing.TODAY
import org.junit.Test
import java.time.LocalDate

class ValidityFormTest {

    @Test
    fun twentyEightDaysFromToday() {
        val preview = ValidityForm(presetDays = 28, rechargeChoice = RechargeChoice.TODAY).preview(TODAY)!!
        assertThat(preview.end).isEqualTo(LocalDate.of(2026, 11, 16))
        assertThat(preview.daysLeft).isEqualTo(28)
        assertThat(preview.status).isEqualTo(Status.SAFE)
    }

    @Test
    fun previewFollowsChips() {
        var form = ValidityForm(presetDays = 28, rechargeChoice = RechargeChoice.THREE_WEEKS)
        assertThat(form.preview(TODAY)!!.end).isEqualTo(LocalDate.of(2026, 10, 26))
        assertThat(form.preview(TODAY)!!.daysLeft).isEqualTo(7)
        assertThat(form.preview(TODAY)!!.status).isEqualTo(Status.SOON)

        form = form.copy(presetDays = 84)
        assertThat(form.preview(TODAY)!!.end).isEqualTo(LocalDate.of(2026, 12, 21))

        form = form.copy(rechargeChoice = RechargeChoice.ONE_MONTH, presetDays = 28)
        assertThat(form.preview(TODAY)!!.end).isEqualTo(LocalDate.of(2026, 10, 17))
        assertThat(form.preview(TODAY)!!.status).isEqualTo(Status.EXPIRED)
    }

    @Test
    fun customDays() {
        val empty = ValidityForm(presetDays = null)
        assertThat(empty.planDays).isNull()
        assertThat(empty.preview(TODAY)).isNull()
        assertThat(empty.isComplete).isFalse()
        assertThat(empty.customDaysError).isFalse()

        val custom = empty.copy(customDaysText = "30")
        assertThat(custom.planDays).isEqualTo(30)
        assertThat(custom.preview(TODAY)!!.daysLeft).isEqualTo(30)

        assertThat(empty.copy(customDaysText = "0").customDaysError).isTrue()
        assertThat(empty.copy(customDaysText = "4000").customDaysError).isTrue()
    }

    @Test
    fun exactEndDate() {
        val end = LocalDate.of(2026, 10, 21)
        val form = ValidityForm(presetDays = 28, useExactEnd = true, exactEndDate = end)
        assertThat(form.preview(TODAY)!!.daysLeft).isEqualTo(2)

        val sim = form.applyTo(blankSim(), TODAY)
        assertThat(sim.exactEndDate).isEqualTo(end)
        assertThat(sim.lastRechargeDate).isEqualTo(LocalDate.of(2026, 9, 23))
        assertThat(planEnd(sim)).isEqualTo(end)
    }

    @Test
    fun exactEndWithoutDateIsIncomplete() {
        assertThat(ValidityForm(useExactEnd = true).isComplete).isFalse()
    }

    @Test
    fun applyToSetsAllFields() {
        val sim = ValidityForm(
            presetDays = 56,
            rechargeChoice = RechargeChoice.ONE_WEEK,
            usedChoice = UsedChoice.PICKED,
            pickedUsedDate = LocalDate.of(2026, 10, 15),
        ).applyTo(blankSim(), TODAY)
        assertThat(sim.planDays).isEqualTo(56)
        assertThat(sim.lastRechargeDate).isEqualTo(LocalDate.of(2026, 10, 12))
        assertThat(sim.exactEndDate).isNull()
        assertThat(sim.lastUsedDate).isEqualTo(LocalDate.of(2026, 10, 15))
    }

    @Test
    fun editModeRoundTrips() {
        val original = blankSim().copy(planDays = 30, lastRechargeDate = LocalDate.of(2026, 10, 1), lastUsedDate = LocalDate.of(2026, 10, 10))
        val form = ValidityForm.from(original, TODAY)
        assertThat(form.isCustom).isTrue()
        assertThat(form.rechargeChoice).isEqualTo(RechargeChoice.PICKED)
        assertThat(form.applyTo(original, TODAY)).isEqualTo(original)
    }

    @Test
    fun editModePicksMatchingChips() {
        val original = blankSim().copy(lastRechargeDate = TODAY.minusWeeks(3), lastUsedDate = TODAY)
        val form = ValidityForm.from(original, TODAY)
        assertThat(form.presetDays).isEqualTo(28)
        assertThat(form.rechargeChoice).isEqualTo(RechargeChoice.THREE_WEEKS)
        assertThat(form.usedChoice).isEqualTo(UsedChoice.TODAY)
        assertThat(form.applyTo(original, TODAY)).isEqualTo(original)
    }

    private fun blankSim() = Sim(
        name = "Bank SIM",
        carrier = Carrier.AIRTEL,
        planDays = 28,
        lastRechargeDate = TODAY,
        lastUsedDate = TODAY,
        createdAt = 0,
    )
}
