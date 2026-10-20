package com.simkeeper.app.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.simkeeper.app.domain.model.Recharge
import java.time.LocalDate

@Entity(
    tableName = "recharges",
    foreignKeys = [
        ForeignKey(
            entity = SimEntity::class,
            parentColumns = ["id"],
            childColumns = ["simId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("simId")],
)
data class RechargeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val simId: Long,
    val date: LocalDate,
    val planDays: Int,
    val amountRupees: Int? = null,
    val note: String? = null,
)

fun RechargeEntity.toDomain() = Recharge(
    id = id,
    simId = simId,
    date = date,
    planDays = planDays,
    amountRupees = amountRupees,
    note = note,
)

fun Recharge.toEntity() = RechargeEntity(
    id = id,
    simId = simId,
    date = date,
    planDays = planDays,
    amountRupees = amountRupees,
    note = note,
)
