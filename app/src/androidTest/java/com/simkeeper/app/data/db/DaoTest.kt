package com.simkeeper.app.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.simkeeper.app.domain.model.Carrier
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class DaoTest {

    private lateinit var db: AppDatabase
    private lateinit var simDao: SimDao
    private lateinit var rechargeDao: RechargeDao
    private lateinit var prefDao: ReminderPrefDao

    private val today = LocalDate.of(2026, 10, 19)

    private fun bankSim() = SimEntity(
        name = "Bank SIM",
        carrier = Carrier.AIRTEL,
        carrierLabel = null,
        slot = 2,
        planDays = 28,
        lastRechargeDate = LocalDate.of(2026, 9, 23),
        lastUsedDate = LocalDate.of(2026, 10, 15),
        createdAt = 0,
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        simDao = db.simDao()
        rechargeDao = db.rechargeDao()
        prefDao = db.reminderPrefDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun insertAndObserve() = runTest {
        val id = simDao.insert(bankSim())
        simDao.observe(id).test {
            val sim = awaitItem()!!
            assertThat(sim.name).isEqualTo("Bank SIM")
            assertThat(sim.carrier).isEqualTo(Carrier.AIRTEL)
            assertThat(sim.lastRechargeDate).isEqualTo(LocalDate.of(2026, 9, 23))
            assertThat(sim.exactEndDate).isNull()
            cancelAndIgnoreRemainingEvents()
        }
        simDao.observeAll().test {
            assertThat(awaitItem()).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateIsObserved() = runTest {
        val id = simDao.insert(bankSim())
        simDao.observe(id).test {
            assertThat(awaitItem()!!.planDays).isEqualTo(28)
            simDao.update(simDao.get(id)!!.copy(planDays = 84, lastRechargeDate = today))
            val updated = awaitItem()!!
            assertThat(updated.planDays).isEqualTo(84)
            assertThat(updated.lastRechargeDate).isEqualTo(today)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun rechargesAreNewestFirst() = runTest {
        val id = simDao.insert(bankSim())
        rechargeDao.insert(RechargeEntity(simId = id, date = LocalDate.of(2026, 8, 26), planDays = 28))
        rechargeDao.insert(RechargeEntity(simId = id, date = LocalDate.of(2026, 9, 23), planDays = 28, amountRupees = 199))
        rechargeDao.observeForSim(id).test {
            val list = awaitItem()
            assertThat(list.map { it.date }).containsExactly(
                LocalDate.of(2026, 9, 23),
                LocalDate.of(2026, 8, 26),
            ).inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteRechargeForUndo() = runTest {
        val id = simDao.insert(bankSim())
        val rechargeId = rechargeDao.insert(RechargeEntity(simId = id, date = today, planDays = 28))
        rechargeDao.delete(rechargeId)
        assertThat(rechargeDao.get(rechargeId)).isNull()
    }

    @Test
    fun deletingSimCascadesToRecharges() = runTest {
        val id = simDao.insert(bankSim())
        val rechargeId = rechargeDao.insert(RechargeEntity(simId = id, date = today, planDays = 28))
        simDao.deleteById(id)
        assertThat(simDao.get(id)).isNull()
        assertThat(rechargeDao.get(rechargeId)).isNull()
    }

    @Test
    fun reminderPrefsUpsert() = runTest {
        val id = simDao.insert(bankSim())
        prefDao.upsertAll(listOf(7, 3, 1, 0).map { ReminderPrefEntity(id, it, enabled = true) })
        prefDao.upsert(ReminderPrefEntity(id, 3, enabled = false))
        prefDao.observeForSim(id).test {
            val prefs = awaitItem().associate { it.offsetDays to it.enabled }
            assertThat(prefs).containsExactly(7, true, 3, false, 1, true, 0, true)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
