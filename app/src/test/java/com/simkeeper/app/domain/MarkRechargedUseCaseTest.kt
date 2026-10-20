package com.simkeeper.app.domain

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.domain.model.Sim
import com.simkeeper.app.domain.usecase.MarkRechargedUseCase
import com.simkeeper.app.testing.FakeNotificationLog
import com.simkeeper.app.testing.FakeSimRepository
import com.simkeeper.app.testing.TODAY
import com.simkeeper.app.testing.fixedClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class MarkRechargedUseCaseTest {

    private lateinit var repo: FakeSimRepository
    private lateinit var log: FakeNotificationLog
    private lateinit var useCase: MarkRechargedUseCase
    private var bankSimId = 0L

    private val bankSim = Sim(
        name = "Bank SIM",
        carrier = Carrier.AIRTEL,
        slot = 2,
        planDays = 28,
        lastRechargeDate = LocalDate.of(2026, 9, 23),
        exactEndDate = LocalDate.of(2026, 10, 21),
        lastUsedDate = LocalDate.of(2026, 10, 15),
        createdAt = 0,
    )

    @Before
    fun setUp() = runTest {
        repo = FakeSimRepository()
        log = FakeNotificationLog()
        useCase = MarkRechargedUseCase(repo, log, fixedClock())
        bankSimId = repo.addSim(bankSim)
    }

    @Test
    fun rechargeUpdatesDatesAndHistory() = runTest {
        val receipt = useCase(bankSimId, amountRupees = 199)!!

        val sim = repo.getSim(bankSimId)!!
        assertThat(sim.lastRechargeDate).isEqualTo(TODAY)
        assertThat(sim.lastUsedDate).isEqualTo(TODAY)
        assertThat(sim.exactEndDate).isNull()
        assertThat(sim.planDays).isEqualTo(28)
        assertThat(planEnd(sim)).isEqualTo(LocalDate.of(2026, 11, 16))
        assertThat(simStateOf(sim, TODAY).status).isEqualTo(Status.SAFE)

        val history = repo.observeRecharges(bankSimId).first()
        assertThat(history).hasSize(2)
        assertThat(history.first().id).isEqualTo(receipt.rechargeId)
        assertThat(history.first().date).isEqualTo(TODAY)
        assertThat(history.first().amountRupees).isEqualTo(199)
    }

    @Test
    fun rechargeWithNewPlanLength() = runTest {
        useCase(bankSimId, planDays = 84)
        val sim = repo.getSim(bankSimId)!!
        assertThat(sim.planDays).isEqualTo(84)
        assertThat(planEnd(sim)).isEqualTo(TODAY.plusDays(84))
    }

    @Test
    fun rechargeClearsSentReminders() = runTest {
        log.markNotified(bankSimId, 3, LocalDate.of(2026, 10, 18))
        log.snooze(bankSimId, LocalDate.of(2026, 10, 20))
        useCase(bankSimId)
        assertThat(log.lastNotified(bankSimId, 3)).isNull()
        assertThat(log.snoozedUntil(bankSimId)).isNull()
    }

    @Test
    fun undoRestoresExactPreviousState() = runTest {
        log.markNotified(bankSimId, 3, LocalDate.of(2026, 10, 18))
        val before = repo.getSim(bankSimId)
        val historyBefore = repo.observeRecharges(bankSimId).first()
        val logBefore = log.snapshot(bankSimId)

        val receipt = useCase(bankSimId, amountRupees = 199)!!
        useCase.undo(receipt)

        assertThat(repo.getSim(bankSimId)).isEqualTo(before)
        assertThat(repo.observeRecharges(bankSimId).first()).isEqualTo(historyBefore)
        assertThat(log.snapshot(bankSimId)).isEqualTo(logBefore)
    }

    @Test
    fun amendChangesPlanAndAmountThenUndoStillRestores() = runTest {
        val before = repo.getSim(bankSimId)
        val receipt = useCase(bankSimId)!!
        val amended = useCase.amend(receipt, planDays = 56, amountRupees = 299)

        val sim = repo.getSim(bankSimId)!!
        assertThat(sim.planDays).isEqualTo(56)
        val latest = repo.observeRecharges(bankSimId).first().first()
        assertThat(latest.planDays).isEqualTo(56)
        assertThat(latest.amountRupees).isEqualTo(299)

        useCase.undo(amended)
        assertThat(repo.getSim(bankSimId)).isEqualTo(before)
    }

    @Test
    fun historyEmitsNewRecharge() = runTest {
        repo.observeRecharges(bankSimId).test {
            assertThat(awaitItem()).hasSize(1)
            useCase(bankSimId)
            assertThat(awaitItem()).hasSize(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun unknownSimReturnsNull() = runTest {
        assertThat(useCase(999)).isNull()
    }
}
