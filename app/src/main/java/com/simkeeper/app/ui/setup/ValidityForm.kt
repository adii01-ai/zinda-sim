package com.simkeeper.app.ui.setup

import com.simkeeper.app.domain.Status
import com.simkeeper.app.domain.daysLeft
import com.simkeeper.app.domain.model.Sim
import com.simkeeper.app.domain.status
import java.time.LocalDate

val PRESET_PLAN_DAYS = listOf(28, 56, 84, 365)
const val MIN_PLAN_DAYS = 1
const val MAX_PLAN_DAYS = 3650
const val MAX_PLAN_DIGITS = 4

enum class RechargeChoice { TODAY, ONE_WEEK, THREE_WEEKS, ONE_MONTH, PICKED }

enum class UsedChoice { TODAY, ONE_WEEK, ONE_MONTH, PICKED }

/**
 * Everything on the Set validity sheet. Pure state: the preview is always computed from it,
 * so it can never disagree with the selected chips.
 */
data class ValidityForm(
    val presetDays: Int? = PRESET_PLAN_DAYS.first(),
    val customDaysText: String = "",
    val rechargeChoice: RechargeChoice = RechargeChoice.TODAY,
    val pickedRechargeDate: LocalDate? = null,
    val useExactEnd: Boolean = false,
    val exactEndDate: LocalDate? = null,
    val usedExpanded: Boolean = false,
    val usedChoice: UsedChoice = UsedChoice.TODAY,
    val pickedUsedDate: LocalDate? = null,
) {
    val isCustom: Boolean get() = presetDays == null

    /** Plan length, or null while the custom field is empty or out of range. */
    val planDays: Int?
        get() = presetDays ?: customDaysText.toIntOrNull()?.takeIf { it in MIN_PLAN_DAYS..MAX_PLAN_DAYS }

    val customDaysError: Boolean get() = isCustom && customDaysText.isNotEmpty() && planDays == null

    fun rechargeDate(today: LocalDate): LocalDate = when (rechargeChoice) {
        RechargeChoice.TODAY -> today
        RechargeChoice.ONE_WEEK -> today.minusWeeks(1)
        RechargeChoice.THREE_WEEKS -> today.minusWeeks(3)
        RechargeChoice.ONE_MONTH -> today.minusMonths(1)
        RechargeChoice.PICKED -> pickedRechargeDate ?: today
    }

    fun usedDate(today: LocalDate): LocalDate = when (usedChoice) {
        UsedChoice.TODAY -> today
        UsedChoice.ONE_WEEK -> today.minusWeeks(1)
        UsedChoice.ONE_MONTH -> today.minusMonths(1)
        UsedChoice.PICKED -> pickedUsedDate ?: today
    }

    /** Plan end shown in the preview card, or null until the form is complete. */
    fun planEnd(today: LocalDate): LocalDate? {
        val days = planDays ?: return null
        return if (useExactEnd) exactEndDate else rechargeDate(today).plusDays(days.toLong())
    }

    val isComplete: Boolean get() = planDays != null && (!useExactEnd || exactEndDate != null)

    fun preview(today: LocalDate): ValidityPreview? {
        val end = planEnd(today) ?: return null
        val left = daysLeft(end, today)
        return ValidityPreview(end = end, daysLeft = left, status = status(left))
    }

    /** The validity fields of a SIM. With an exact end date the recharge date is derived from it. */
    fun applyTo(sim: Sim, today: LocalDate): Sim {
        val days = requireNotNull(planDays)
        val end = planEnd(today)
        return sim.copy(
            planDays = days,
            lastRechargeDate = if (useExactEnd && end != null) end.minusDays(days.toLong()) else rechargeDate(today),
            exactEndDate = if (useExactEnd) end else null,
            lastUsedDate = usedDate(today),
        )
    }

    companion object {
        /** Edit mode: start from what the SIM already has, on the matching chips where possible. */
        fun from(sim: Sim, today: LocalDate): ValidityForm {
            val rechargeChoice = RechargeChoice.entries.firstOrNull {
                it != RechargeChoice.PICKED && ValidityForm(rechargeChoice = it).rechargeDate(today) == sim.lastRechargeDate
            } ?: RechargeChoice.PICKED
            val usedChoice = UsedChoice.entries.firstOrNull {
                it != UsedChoice.PICKED && ValidityForm(usedChoice = it).usedDate(today) == sim.lastUsedDate
            } ?: UsedChoice.PICKED
            return ValidityForm(
                presetDays = sim.planDays.takeIf { it in PRESET_PLAN_DAYS },
                customDaysText = if (sim.planDays in PRESET_PLAN_DAYS) "" else sim.planDays.toString(),
                rechargeChoice = rechargeChoice,
                pickedRechargeDate = sim.lastRechargeDate,
                useExactEnd = sim.exactEndDate != null,
                exactEndDate = sim.exactEndDate,
                usedChoice = usedChoice,
                pickedUsedDate = sim.lastUsedDate,
            )
        }
    }
}

data class ValidityPreview(val end: LocalDate, val daysLeft: Long, val status: Status)
