package com.simkeeper.app.data.repo

import androidx.room.withTransaction
import com.simkeeper.app.data.db.AppDatabase
import com.simkeeper.app.data.db.RechargeDao
import com.simkeeper.app.data.db.ReminderPrefDao
import com.simkeeper.app.data.db.ReminderPrefEntity
import com.simkeeper.app.data.db.SimDao
import com.simkeeper.app.data.db.toDomain
import com.simkeeper.app.data.db.toEntity
import com.simkeeper.app.domain.SimState
import com.simkeeper.app.domain.model.Recharge
import com.simkeeper.app.domain.model.Sim
import com.simkeeper.app.domain.usecase.ComputeDeadlinesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Reminder offsets in days before the deadline: 7, 3, 1 and on the day. */
val REMINDER_OFFSETS = listOf(7, 3, 1, 0)

interface SimRepository {
    fun observeSims(): Flow<List<Sim>>
    fun observeSim(id: Long): Flow<Sim?>
    suspend fun getSim(id: Long): Sim?
    suspend fun getAllSims(): List<Sim>
    suspend fun simCount(): Int

    /** Inserts the SIM, its first recharge row and default reminders (all on). Returns the new id. */
    suspend fun addSim(sim: Sim): Long
    suspend fun updateSim(sim: Sim)
    suspend fun deleteSim(id: Long)

    fun observeRecharges(simId: Long): Flow<List<Recharge>>

    /** Saves [updated] and inserts [recharge] in one transaction. Returns the recharge id. */
    suspend fun recordRecharge(updated: Sim, recharge: Recharge): Long
    suspend fun updateRecharge(recharge: Recharge)

    /** Undo: delete the recharge row and put the SIM back exactly as it was. */
    suspend fun revertRecharge(previous: Sim, rechargeId: Long)

    /** Enabled offsets (subset of [REMINDER_OFFSETS]). */
    fun observeReminderOffsets(simId: Long): Flow<Set<Int>>
    suspend fun setReminder(simId: Long, offsetDays: Int, enabled: Boolean)
    suspend fun enabledOffsetsBySim(): Map<Long, Set<Int>>
}

/** Adds computed deadlines. Lists are sorted by days left ascending. */
interface SimStateSource {
    fun observeSimStates(): Flow<List<SimState>>
    fun observeSimState(id: Long): Flow<SimState?>
}

@Singleton
class RoomSimRepository @Inject constructor(
    private val db: AppDatabase,
    private val simDao: SimDao,
    private val rechargeDao: RechargeDao,
    private val reminderPrefDao: ReminderPrefDao,
    private val computeDeadlines: ComputeDeadlinesUseCase,
) : SimRepository, SimStateSource {

    override fun observeSims(): Flow<List<Sim>> = simDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeSim(id: Long): Flow<Sim?> = simDao.observe(id).map { it?.toDomain() }

    override fun observeSimStates(): Flow<List<SimState>> = observeSims().map { computeDeadlines(it) }

    override fun observeSimState(id: Long): Flow<SimState?> = observeSim(id).map { it?.let(computeDeadlines::invoke) }

    override suspend fun getSim(id: Long): Sim? = simDao.get(id)?.toDomain()

    override suspend fun getAllSims(): List<Sim> = simDao.getAll().map { it.toDomain() }

    override suspend fun simCount(): Int = simDao.count()

    override suspend fun addSim(sim: Sim): Long = db.withTransaction {
        val id = simDao.insert(sim.toEntity().copy(id = 0))
        rechargeDao.insert(
            Recharge(simId = id, date = sim.lastRechargeDate, planDays = sim.planDays).toEntity(),
        )
        reminderPrefDao.upsertAll(REMINDER_OFFSETS.map { ReminderPrefEntity(simId = id, offsetDays = it, enabled = true) })
        id
    }

    override suspend fun updateSim(sim: Sim) = simDao.update(sim.toEntity())

    override suspend fun deleteSim(id: Long) = simDao.deleteById(id)

    override fun observeRecharges(simId: Long): Flow<List<Recharge>> =
        rechargeDao.observeForSim(simId).map { list -> list.map { it.toDomain() } }

    override suspend fun recordRecharge(updated: Sim, recharge: Recharge): Long = db.withTransaction {
        simDao.update(updated.toEntity())
        rechargeDao.insert(recharge.toEntity())
    }

    override suspend fun updateRecharge(recharge: Recharge) = rechargeDao.update(recharge.toEntity())

    override suspend fun revertRecharge(previous: Sim, rechargeId: Long) = db.withTransaction {
        rechargeDao.delete(rechargeId)
        simDao.update(previous.toEntity())
    }

    override fun observeReminderOffsets(simId: Long): Flow<Set<Int>> =
        reminderPrefDao.observeForSim(simId).map { prefs -> enabledOffsets(prefs) }

    override suspend fun setReminder(simId: Long, offsetDays: Int, enabled: Boolean) =
        reminderPrefDao.upsert(ReminderPrefEntity(simId = simId, offsetDays = offsetDays, enabled = enabled))

    override suspend fun enabledOffsetsBySim(): Map<Long, Set<Int>> {
        val bySim = reminderPrefDao.getAll().groupBy { it.simId }
        return simDao.getAll().associate { sim -> sim.id to enabledOffsets(bySim[sim.id].orEmpty()) }
    }

    /** A missing row means "on" (every reminder is enabled by default). */
    private fun enabledOffsets(prefs: List<ReminderPrefEntity>): Set<Int> {
        val stored = prefs.associate { it.offsetDays to it.enabled }
        return REMINDER_OFFSETS.filter { stored[it] ?: true }.toSet()
    }
}
