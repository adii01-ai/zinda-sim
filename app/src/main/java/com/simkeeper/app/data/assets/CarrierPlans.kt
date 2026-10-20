package com.simkeeper.app.data.assets

import kotlinx.serialization.Serializable

/** assets/carrier_plans.json (CARRIER_RULES.md). Prices are never fetched from the internet. */
@Serializable
data class CarrierPlans(
    /** "yyyy-MM", e.g. "2026-10". */
    val pricesAsOf: String,
    val plans: List<CarrierPlan>,
)

@Serializable
data class CarrierPlan(
    /** Carrier enum name. */
    val carrier: String,
    /** null until a verified price is filled in; the UI shows "₹—". */
    val rupees: Int? = null,
    val days: Int,
    val note: String? = null,
)
