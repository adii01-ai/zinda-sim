package com.simkeeper.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SimDao {
    @Query("SELECT * FROM sims ORDER BY sortOrder, id")
    fun observeAll(): Flow<List<SimEntity>>

    @Query("SELECT * FROM sims WHERE id = :id")
    fun observe(id: Long): Flow<SimEntity?>

    @Query("SELECT * FROM sims ORDER BY sortOrder, id")
    suspend fun getAll(): List<SimEntity>

    @Query("SELECT * FROM sims WHERE id = :id")
    suspend fun get(id: Long): SimEntity?

    @Query("SELECT COUNT(*) FROM sims")
    suspend fun count(): Int

    @Insert
    suspend fun insert(sim: SimEntity): Long

    @Update
    suspend fun update(sim: SimEntity)

    @Delete
    suspend fun delete(sim: SimEntity)

    @Query("DELETE FROM sims WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface RechargeDao {
    /** Newest first. */
    @Query("SELECT * FROM recharges WHERE simId = :simId ORDER BY date DESC, id DESC")
    fun observeForSim(simId: Long): Flow<List<RechargeEntity>>

    @Query("SELECT * FROM recharges WHERE id = :id")
    suspend fun get(id: Long): RechargeEntity?

    @Insert
    suspend fun insert(recharge: RechargeEntity): Long

    @Update
    suspend fun update(recharge: RechargeEntity)

    /** Used by Undo. */
    @Query("DELETE FROM recharges WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface ReminderPrefDao {
    @Query("SELECT * FROM reminder_prefs WHERE simId = :simId ORDER BY offsetDays DESC")
    fun observeForSim(simId: Long): Flow<List<ReminderPrefEntity>>

    @Query("SELECT * FROM reminder_prefs")
    suspend fun getAll(): List<ReminderPrefEntity>

    @Upsert
    suspend fun upsert(pref: ReminderPrefEntity)

    @Upsert
    suspend fun upsertAll(prefs: List<ReminderPrefEntity>)
}
