package com.simkeeper.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.domain.model.Sim
import java.time.LocalDate

@Entity(tableName = "sims")
data class SimEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val carrier: Carrier,
    val carrierLabel: String?,
    val slot: Int?,
    val isFamily: Boolean = false,
    val ownerName: String? = null,
    val subscriptionId: Int? = null,
    val planDays: Int,
    val lastRechargeDate: LocalDate,
    val exactEndDate: LocalDate? = null,
    val lastUsedDate: LocalDate,
    val createdAt: Long,
    val sortOrder: Int = 0,
)

fun SimEntity.toDomain() = Sim(
    id = id,
    name = name,
    carrier = carrier,
    carrierLabel = carrierLabel,
    slot = slot,
    isFamily = isFamily,
    ownerName = ownerName,
    subscriptionId = subscriptionId,
    planDays = planDays,
    lastRechargeDate = lastRechargeDate,
    exactEndDate = exactEndDate,
    lastUsedDate = lastUsedDate,
    createdAt = createdAt,
    sortOrder = sortOrder,
)

fun Sim.toEntity() = SimEntity(
    id = id,
    name = name,
    carrier = carrier,
    carrierLabel = carrierLabel,
    slot = slot,
    isFamily = isFamily,
    ownerName = ownerName,
    subscriptionId = subscriptionId,
    planDays = planDays,
    lastRechargeDate = lastRechargeDate,
    exactEndDate = exactEndDate,
    lastUsedDate = lastUsedDate,
    createdAt = createdAt,
    sortOrder = sortOrder,
)
