package com.simkeeper.app.data.db

import androidx.room.Entity

@Entity(tableName = "reminder_prefs", primaryKeys = ["simId", "offsetDays"])
data class ReminderPrefEntity(
    val simId: Long,
    /** 7, 3, 1 or 0 days before the deadline. */
    val offsetDays: Int,
    val enabled: Boolean = true,
)
