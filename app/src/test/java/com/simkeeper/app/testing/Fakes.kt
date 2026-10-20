package com.simkeeper.app.testing

import com.simkeeper.app.data.repo.NotificationLog
import com.simkeeper.app.data.repo.REMINDER_OFFSETS
import com.simkeeper.app.data.repo.SimRepository
import com.simkeeper.app.domain.model.Recharge
import com.simkeeper.app.domain.model.Sim
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

val TODAY: LocalDate = LocalDate.of(2026, 10, 19)

fun fixedClock(date: LocalDate = TODAY): Clock =
    Clock.fixed(date.atTime(10, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC)

class FakeSimRepository : SimRepository {
    val sims = MutableStateFlow<Map<Long, Sim>>(emptyMap())
    val recharges = MutableStateFlow<List<Recharge>>(emptyList())
    val reminders = MutableStateFlow<Map<Long, Set<Int>>>(emptyMap())
    private var nextSimId = 1L
    private var nextRechargeId = 1L

    override fun observeSims(): Flow<List<Sim>> = sims.map { it.values.toList() }
    override fun observeSim(id: Long): Flow<Sim?> = sims.map { it[id] }
    override suspend fun getSim(id: Long): Sim? = sims.value[id]
    override suspend fun getAllSims(): List<Sim> = sims.value.values.toList()
    override suspend fun simCount(): Int = sims.value.size

    override suspend fun addSim(sim: Sim): Long {
        val id = nextSimId++
        sims.value = sims.value + (id to sim.copy(id = id))
        recharges.value = recharges.value + Recharge(nextRechargeId++, id, sim.lastRechargeDate, sim.planDays)
        reminders.value = reminders.value + (id to REMINDER_OFFSETS.toSet())
        return id
    }

    override suspend fun updateSim(sim: Sim) {
        sims.value = sims.value + (sim.id to sim)
    }

    override suspend fun deleteSim(id: Long) {
        sims.value = sims.value - id
        recharges.value = recharges.value.filterNot { it.simId == id }
    }

    override fun observeRecharges(simId: Long): Flow<List<Recharge>> =
        recharges.map { list -> list.filter { it.simId == simId }.sortedWith(compareByDescending<Recharge> { it.date }.thenByDescending { it.id }) }

    override suspend fun recordRecharge(updated: Sim, recharge: Recharge): Long {
        val id = nextRechargeId++
        updateSim(updated)
        recharges.value = recharges.value + recharge.copy(id = id)
        return id
    }

    override suspend fun updateRecharge(recharge: Recharge) {
        recharges.value = recharges.value.map { if (it.id == recharge.id) recharge else it }
    }

    override suspend fun revertRecharge(previous: Sim, rechargeId: Long) {
        recharges.value = recharges.value.filterNot { it.id == rechargeId }
        updateSim(previous)
    }

    override fun observeReminderOffsets(simId: Long): Flow<Set<Int>> = reminders.map { it[simId].orEmpty() }

    override suspend fun setReminder(simId: Long, offsetDays: Int, enabled: Boolean) {
        val current = reminders.value[simId].orEmpty()
        reminders.value = reminders.value + (simId to if (enabled) current + offsetDays else current - offsetDays)
    }

    override suspend fun enabledOffsetsBySim(): Map<Long, Set<Int>> = reminders.value
}

class FakeNotificationLog : NotificationLog {
    val entries = mutableMapOf<String, String>()

    private fun key(simId: Long, key: Int) = "lastNotified_${simId}_$key"
    private fun snoozeKey(simId: Long) = "snoozedUntil_$simId"
    private fun belongs(name: String, simId: Long) = name.startsWith("lastNotified_${simId}_") || name == snoozeKey(simId)

    override suspend fun lastNotified(simId: Long, key: Int): LocalDate? = entries[key(simId, key)]?.let(LocalDate::parse)
    override suspend fun markNotified(simId: Long, key: Int, date: LocalDate) {
        entries[key(simId, key)] = date.toString()
    }
    override suspend fun snoozedUntil(simId: Long): LocalDate? = entries[snoozeKey(simId)]?.let(LocalDate::parse)
    override suspend fun snooze(simId: Long, until: LocalDate) {
        entries[snoozeKey(simId)] = until.toString()
    }
    override suspend fun clearSnooze(simId: Long) {
        entries.remove(snoozeKey(simId))
    }
    override suspend fun clear(simId: Long) {
        entries.keys.removeAll { belongs(it, simId) }
    }
    override suspend fun snapshot(simId: Long): Map<String, String> = entries.filterKeys { belongs(it, simId) }
    override suspend fun restore(simId: Long, snapshot: Map<String, String>) {
        clear(simId)
        entries.putAll(snapshot)
    }
}
