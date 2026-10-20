package com.simkeeper.app.data.assets

import kotlinx.serialization.Serializable

/** assets/carrier_rules.json (CARRIER_RULES.md). */
@Serializable
data class CarrierRules(
    /** ISO date, e.g. "2026-10-19". */
    val lastChecked: String,
    val noUseLimitDays: Int,
    /** Keyed by Carrier enum name: JIO, AIRTEL, VI, BSNL, OTHER. */
    val carriers: Map<String, CarrierRule>,
)

@Serializable
data class CarrierRule(
    val graceNote: String,
    val verified: Boolean = false,
)
