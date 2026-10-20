package com.simkeeper.app.domain.usecase

import com.simkeeper.app.domain.SimState
import com.simkeeper.app.domain.model.Sim
import com.simkeeper.app.domain.simStateOf
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** Computes deadlines and status for "today" from the injected [Clock]. */
class ComputeDeadlinesUseCase @Inject constructor(private val clock: Clock) {

    fun today(): LocalDate = LocalDate.now(clock)

    operator fun invoke(sim: Sim): SimState = simStateOf(sim, today())

    /** Sorted by days left ascending (most urgent first). */
    operator fun invoke(sims: List<Sim>): List<SimState> {
        val today = today()
        return sims.map { simStateOf(it, today) }
            .sortedWith(compareBy<SimState> { it.daysLeft }.thenBy { it.sim.sortOrder }.thenBy { it.sim.id })
    }
}
