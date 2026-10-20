package com.simkeeper.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Version 1. Add a Migration for every later schema change; never use destructive
 * migration in release builds. Schemas are exported to app/schemas/.
 */
@Database(
    entities = [SimEntity::class, RechargeEntity::class, ReminderPrefEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun simDao(): SimDao
    abstract fun rechargeDao(): RechargeDao
    abstract fun reminderPrefDao(): ReminderPrefDao

    companion object {
        const val NAME = "simkeeper.db"
    }
}
