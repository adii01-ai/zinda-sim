package com.simkeeper.app.domain

import com.simkeeper.app.domain.model.Sim
import java.time.LocalDate
import java.time.temporal.ChronoUnit

const val NO_USE_LIMIT_DAYS = 90

fun planEnd(sim: Sim): LocalDate = sim.exactEndDate ?: sim.lastRechargeDate.plusDays(sim.planDays.toLong())

fun noUseEnd(sim: Sim): LocalDate = sim.lastUsedDate.plusDays(NO_USE_LIMIT_DAYS.toLong())

data class Deadlines(val planEnd: LocalDate, val noUseEnd: LocalDate) {
    val effective: LocalDate get() = minOf(planEnd, noUseEnd)
    val reason: Reason get() = if (planEnd <= noUseEnd) Reason.PLAN else Reason.NO_USE
}

fun deadlinesOf(sim: Sim): Deadlines = Deadlines(planEnd = planEnd(sim), noUseEnd = noUseEnd(sim))

fun daysLeft(effective: LocalDate, today: LocalDate): Long = ChronoUnit.DAYS.between(today, effective)

fun status(daysLeft: Long): Status = when {
    daysLeft < 0 -> Status.EXPIRED
    daysLeft <= 2 -> Status.URGENT
    daysLeft <= 7 -> Status.SOON
    else -> Status.SAFE
}

fun ringProgress(daysLeft: Long, planDays: Int): Float =
    if (planDays <= 0) {
        if (daysLeft > 0) 1f else 0f
    } else {
        (daysLeft.toFloat() / planDays).coerceIn(0f, 1f)
    }

/** A SIM with everything the UI needs, computed for one "today". */
data class SimState(
    val sim: Sim,
    val deadlines: Deadlines,
    val daysLeft: Long,
    val status: Status,
    val ringProgress: Float,
) {
    val needsRecharge: Boolean get() = status == Status.URGENT || status == Status.SOON || status == Status.EXPIRED
}

fun simStateOf(sim: Sim, today: LocalDate): SimState {
    val deadlines = deadlinesOf(sim)
    val left = daysLeft(deadlines.effective, today)
    return SimState(
        sim = sim,
        deadlines = deadlines,
        daysLeft = left,
        status = status(left),
        ringProgress = ringProgress(left, sim.planDays),
    )
}
