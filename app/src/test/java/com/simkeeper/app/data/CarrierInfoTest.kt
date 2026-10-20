package com.simkeeper.app.data

import com.google.common.truth.Truth.assertThat
import com.simkeeper.app.data.repo.CarrierInfo
import com.simkeeper.app.domain.model.Carrier
import org.junit.Test
import java.io.File
import java.time.LocalDate
import java.time.YearMonth

class CarrierInfoTest {

    private val rules = """
        {
          "lastChecked": "2026-10-19",
          "noUseLimitDays": 90,
          "carriers": {
            "AIRTEL": { "graceNote": "Airtel note", "verified": false },
            "OTHER":  { "graceNote": "Other note",  "verified": false }
          }
        }
    """.trimIndent()

    private fun plans(vararg entries: String) =
        """{ "pricesAsOf": "2026-10", "plans": [ ${entries.joinToString()} ] }"""

    @Test
    fun cheapestIsLowestNonNullPrice() {
        val info = CarrierInfo.parse(
            rules,
            plans(
                """{ "carrier": "AIRTEL", "rupees": null, "days": 28 }""",
                """{ "carrier": "AIRTEL", "rupees": 199, "days": 28 }""",
                """{ "carrier": "AIRTEL", "rupees": 155, "days": 24 }""",
                """{ "carrier": "JIO", "rupees": 99, "days": 14 }""",
            ),
        )
        assertThat(info.cheapestPlan(Carrier.AIRTEL)?.rupees).isEqualTo(155)
        assertThat(info.plansFor(Carrier.AIRTEL).map { it.rupees }).containsExactly(155, 199, null).inOrder()
    }

    @Test
    fun allNullPricesStillReturnsAPlanWithNullPrice() {
        val info = CarrierInfo.parse(rules, plans("""{ "carrier": "VI", "rupees": null, "days": 28 }"""))
        val plan = info.cheapestPlan(Carrier.VI)
        assertThat(plan).isNotNull()
        assertThat(plan!!.rupees).isNull()
        assertThat(plan.days).isEqualTo(28)
    }

    @Test
    fun noPlanForCarrier() {
        val info = CarrierInfo.parse(rules, plans())
        assertThat(info.cheapestPlan(Carrier.OTHER)).isNull()
    }

    @Test
    fun graceNoteFallsBackToOther() {
        val info = CarrierInfo.parse(rules, plans())
        assertThat(info.graceNote(Carrier.AIRTEL)).isEqualTo("Airtel note")
        assertThat(info.graceNote(Carrier.BSNL)).isEqualTo("Other note")
    }

    @Test
    fun parsesDates() {
        val info = CarrierInfo.parse(rules, plans())
        assertThat(info.lastChecked).isEqualTo(LocalDate.of(2026, 10, 19))
        assertThat(info.pricesAsOf).isEqualTo(YearMonth.of(2026, 10))
    }

    @Test
    fun bundledAssetsParse() {
        val assets = File("src/main/assets")
        val info = CarrierInfo.parse(
            File(assets, "carrier_rules.json").readText(),
            File(assets, "carrier_plans.json").readText(),
        )
        Carrier.entries.forEach { assertThat(info.graceNote(it)).isNotEmpty() }
        listOf(Carrier.JIO, Carrier.AIRTEL, Carrier.VI, Carrier.BSNL).forEach {
            assertThat(info.cheapestPlan(it)).isNotNull()
        }
    }
}
