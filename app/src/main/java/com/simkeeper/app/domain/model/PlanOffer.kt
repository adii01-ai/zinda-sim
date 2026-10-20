package com.simkeeper.app.domain.model

/** A plan from the bundled carrier_plans.json. [rupees] is null until a verified price is filled in. */
data class PlanOffer(val rupees: Int?, val days: Int)
