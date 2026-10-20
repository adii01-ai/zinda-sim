package com.simkeeper.app.domain

import com.google.common.truth.Truth.assertThat
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.domain.model.Sim
import com.simkeeper.app.domain.usecase.ComputeDeadlinesUseCase
import com.simkeeper.app.testing.TODAY
import com.simkeeper.app.testing.fixedClock
import org.junit.Test
import java.time.LocalDate

class ComputeDeadlinesUseCaseTest {

    private val useCase = ComputeDeadlinesUseCase(fixedClock())

    private fun sim(id: Long, name: String, planDays: Int, lastRecharge: LocalDate) = Sim(
        id = id,
        name = name,
        carrier = Carrier.JIO,
        planDays = planDays,
        lastRechargeDate = lastRecharge,
        lastUsedDate = TODAY,
        createdAt = 0,
    )

    @Test
    fun todayComesFromTheClock() {
        assertThat(useCase.today()).isEqualTo(TODAY)
    }

    @Test
    fun mostUrgentFirst() {
        val daily = sim(1, "Daily SIM", 84, LocalDate.of(2026, 9, 26))
        val bank = sim(2, "Bank SIM", 28, LocalDate.of(2026, 9, 23))
        val mummy = sim(3, "Mummy's phone", 28, LocalDate.of(2026, 10, 3))
        val states = useCase(listOf(daily, bank, mummy))
        assertThat(states.map { it.sim.name }).containsExactly("Bank SIM", "Mummy's phone", "Daily SIM").inOrder()
        assertThat(states.map { it.daysLeft }).containsExactly(2L, 12L, 61L).inOrder()
    }
}
