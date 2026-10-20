package com.simkeeper.app.domain.model

import java.time.LocalDate

/** A tracked SIM, exactly as the user entered it. The app never knows balance or usage. */
data class Sim(
    val id: Long = 0,
    val name: String,
    val carrier: Carrier,
    val carrierLabel: String? = null,
    /** 1 or 2; null for family SIMs. */
    val slot: Int? = null,
    val isFamily: Boolean = false,
    val ownerName: String? = null,
    val subscriptionId: Int? = null,
    val planDays: Int,
    val lastRechargeDate: LocalDate,
    /** Set when the user chose "I know the exact end date". */
    val exactEndDate: LocalDate? = null,
    /** For the 90-day no-use limit. */
    val lastUsedDate: LocalDate,
    val createdAt: Long,
    val sortOrder: Int = 0,
)
