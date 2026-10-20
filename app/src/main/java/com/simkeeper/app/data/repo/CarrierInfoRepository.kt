package com.simkeeper.app.data.repo

import android.content.Context
import com.simkeeper.app.data.assets.CarrierPlan
import com.simkeeper.app.data.assets.CarrierPlans
import com.simkeeper.app.data.assets.CarrierRules
import com.simkeeper.app.domain.model.Carrier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/** Parsed carrier assets. Pure Kotlin so the selection rules are unit-tested. */
class CarrierInfo(private val rules: CarrierRules, private val plans: CarrierPlans) {

    val lastChecked: LocalDate = LocalDate.parse(rules.lastChecked)
    val pricesAsOf: YearMonth = YearMonth.parse(plans.pricesAsOf)

    fun graceNote(carrier: Carrier): String? =
        (rules.carriers[carrier.name] ?: rules.carriers[Carrier.OTHER.name])?.graceNote

    /**
     * Up to [limit] plans for this carrier, cheapest first. Entries without a verified price
     * (rupees = null) come last so a real price always wins.
     */
    fun plansFor(carrier: Carrier, limit: Int = MAX_PLANS): List<CarrierPlan> =
        plans.plans
            .filter { it.carrier == carrier.name }
            .sortedWith(compareBy<CarrierPlan> { it.rupees == null }.thenBy { it.rupees }.thenBy { it.days })
            .take(limit)

    /** Lowest rupees among non-null entries; a null-price entry only when none has a price. */
    fun cheapestPlan(carrier: Carrier): CarrierPlan? = plansFor(carrier, limit = 1).firstOrNull()

    companion object {
        const val MAX_PLANS = 3

        private val json = Json { ignoreUnknownKeys = true }

        fun parse(rulesJson: String, plansJson: String) = CarrierInfo(
            rules = json.decodeFromString(CarrierRules.serializer(), rulesJson),
            plans = json.decodeFromString(CarrierPlans.serializer(), plansJson),
        )
    }
}

@Singleton
class CarrierInfoRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val mutex = Mutex()
    private var cached: CarrierInfo? = null

    suspend fun info(): CarrierInfo = mutex.withLock {
        cached ?: withContext(Dispatchers.IO) {
            CarrierInfo.parse(readAsset(RULES_FILE), readAsset(PLANS_FILE))
        }.also { cached = it }
    }

    private fun readAsset(name: String): String =
        context.assets.open(name).bufferedReader(Charsets.UTF_8).use { it.readText() }

    private companion object {
        const val RULES_FILE = "carrier_rules.json"
        const val PLANS_FILE = "carrier_plans.json"
    }
}
