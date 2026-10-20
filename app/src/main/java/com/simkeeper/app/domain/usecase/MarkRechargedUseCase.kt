package com.simkeeper.app.domain.usecase

import com.simkeeper.app.data.repo.NotificationLog
import com.simkeeper.app.data.repo.SimRepository
import com.simkeeper.app.domain.model.Recharge
import com.simkeeper.app.domain.model.Sim
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** Everything needed to amend or undo one "I recharged" tap. Keep it until the snackbar closes. */
data class RechargeReceipt(
    val previous: Sim,
    val updated: Sim,
    val rechargeId: Long,
    val amountRupees: Int?,
    val previousNotificationLog: Map<String, String>,
)

/**
 * "I recharged" (DATA_MODEL.md): records a recharge for today, restarts the plan,
 * counts as use, and clears sent reminders. Undo puts everything back exactly.
 */
class MarkRechargedUseCase @Inject constructor(
    private val repository: SimRepository,
    private val notificationLog: NotificationLog,
    private val clock: Clock,
) {
    suspend operator fun invoke(simId: Long, planDays: Int? = null, amountRupees: Int? = null): RechargeReceipt? {
        val sim = repository.getSim(simId) ?: return null
        val today = LocalDate.now(clock)
        val days = planDays ?: sim.planDays
        val updated = sim.copy(
            lastRechargeDate = today,
            planDays = days,
            exactEndDate = null,
            lastUsedDate = today,
        )
        val logSnapshot = notificationLog.snapshot(simId)
        val rechargeId = repository.recordRecharge(
            updated = updated,
            recharge = Recharge(simId = simId, date = today, planDays = days, amountRupees = amountRupees),
        )
        notificationLog.clear(simId)
        return RechargeReceipt(
            previous = sim,
            updated = updated,
            rechargeId = rechargeId,
            amountRupees = amountRupees,
            previousNotificationLog = logSnapshot,
        )
    }

    /** Change plan length or amount on the confirmation screen, after the tap was recorded. */
    suspend fun amend(receipt: RechargeReceipt, planDays: Int, amountRupees: Int?): RechargeReceipt {
        val updated = receipt.updated.copy(planDays = planDays)
        repository.updateSim(updated)
        repository.updateRecharge(
            Recharge(
                id = receipt.rechargeId,
                simId = updated.id,
                date = updated.lastRechargeDate,
                planDays = planDays,
                amountRupees = amountRupees,
            ),
        )
        return receipt.copy(updated = updated, amountRupees = amountRupees)
    }

    suspend fun undo(receipt: RechargeReceipt) {
        repository.revertRecharge(receipt.previous, receipt.rechargeId)
        notificationLog.restore(receipt.previous.id, receipt.previousNotificationLog)
    }
}
