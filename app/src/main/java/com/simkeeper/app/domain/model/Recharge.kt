package com.simkeeper.app.domain.model

import java.time.LocalDate

data class Recharge(
    val id: Long = 0,
    val simId: Long,
    val date: LocalDate,
    val planDays: Int,
    val amountRupees: Int? = null,
    val note: String? = null,
)
