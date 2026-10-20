package com.simkeeper.app.domain

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.simkeeper.app.domain.model.Carrier
import org.junit.Test

class CarrierMappingTest {

    @Test
    fun mapsCarrierNames() {
        val cases = mapOf(
            "Jio 4G" to Carrier.JIO,
            "JIO" to Carrier.JIO,
            "Airtel" to Carrier.AIRTEL,
            "Vi India" to Carrier.VI,
            "Vodafone IN" to Carrier.VI,
            "IDEA" to Carrier.VI,
            "BSNL MOBILE" to Carrier.BSNL,
            "CellOne" to Carrier.BSNL,
            "" to Carrier.OTHER,
        )
        cases.forEach { (name, expected) ->
            assertWithMessage(name).that(Carrier.fromCarrierName(name)).isEqualTo(expected)
        }
    }

    @Test
    fun unknownAndNullAreOther() {
        assertThat(Carrier.fromCarrierName(null)).isEqualTo(Carrier.OTHER)
        assertThat(Carrier.fromCarrierName("T-Mobile")).isEqualTo(Carrier.OTHER)
    }

    @Test
    fun viMatchesOnlyAsAWord() {
        assertThat(Carrier.fromCarrierName("VI")).isEqualTo(Carrier.VI)
        assertThat(Carrier.fromCarrierName("Movistar")).isEqualTo(Carrier.OTHER)
    }
}
