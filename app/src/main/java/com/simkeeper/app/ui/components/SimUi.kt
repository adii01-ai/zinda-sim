package com.simkeeper.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.simkeeper.app.R
import com.simkeeper.app.domain.Reason
import com.simkeeper.app.domain.SimState
import com.simkeeper.app.domain.Status
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.domain.model.Sim
import com.simkeeper.app.ui.format.DateFormats
import com.simkeeper.app.ui.format.currentLocale
import com.simkeeper.app.ui.theme.ColorPair
import com.simkeeper.app.ui.theme.SimKeeperTheme
import kotlin.math.absoluteValue

@Composable
@ReadOnlyComposable
fun statusColors(status: Status): ColorPair {
    val s = SimKeeperTheme.status
    return when (status) {
        Status.SAFE -> s.safe
        Status.SOON -> s.soon
        Status.URGENT -> s.urgent
        Status.EXPIRED -> s.expired
    }
}

@Composable
@ReadOnlyComposable
fun carrierColors(carrier: Carrier): ColorPair {
    val s = SimKeeperTheme.status
    return when (carrier) {
        Carrier.JIO -> s.jio
        Carrier.AIRTEL -> s.airtel
        Carrier.VI -> s.vi
        Carrier.BSNL -> s.bsnl
        Carrier.OTHER -> s.other
    }
}

fun carrierNameRes(carrier: Carrier): Int = when (carrier) {
    Carrier.JIO -> R.string.carrier_jio
    Carrier.AIRTEL -> R.string.carrier_airtel
    Carrier.VI -> R.string.carrier_vi
    Carrier.BSNL -> R.string.carrier_bsnl
    Carrier.OTHER -> R.string.carrier_other
}

/** Carrier display name; free text for OTHER when the user typed one. */
@Composable
@ReadOnlyComposable
fun carrierName(carrier: Carrier, label: String?): String =
    if (carrier == Carrier.OTHER && !label.isNullOrBlank()) label else stringResource(carrierNameRes(carrier))

@Composable
@ReadOnlyComposable
fun carrierName(sim: Sim): String = carrierName(sim.carrier, sim.carrierLabel)

/** J, A, V, B, or the first letter of the carrier name for OTHER. */
@Composable
@ReadOnlyComposable
fun monogramLetter(carrier: Carrier, label: String?): String =
    carrierName(carrier, label).trim().firstOrNull()?.uppercase() ?: stringResource(R.string.carrier_other).take(1)

/** One calm line that always states the urgency in words. */
@Composable
@ReadOnlyComposable
fun statusLine(state: SimState): String {
    val days = state.daysLeft.toInt()
    val noUse = state.deadlines.reason == Reason.NO_USE
    return when (state.status) {
        Status.SAFE -> stringResource(R.string.status_all_good)
        Status.EXPIRED -> pluralStringResource(R.plurals.status_ended_ago, days.absoluteValue, days.absoluteValue)
        Status.SOON, Status.URGENT -> when {
            days == 0 && noUse -> stringResource(R.string.status_use_today)
            days == 0 -> stringResource(R.string.status_recharge_today)
            noUse -> pluralStringResource(R.plurals.status_use_in, days, days)
            else -> pluralStringResource(R.plurals.status_recharge_in, days, days)
        }
    }
}

@Composable
@ReadOnlyComposable
fun statusWord(status: Status): String = stringResource(
    when (status) {
        Status.SAFE -> R.string.status_word_safe
        Status.SOON, Status.URGENT -> R.string.status_word_needs_recharge
        Status.EXPIRED -> R.string.status_word_expired
    },
)

/** "Bank SIM, Airtel, 2 days left, ends 21 October 2026, needs recharge" */
@Composable
fun simCardDescription(state: SimState): String {
    val locale = currentLocale()
    val days = state.daysLeft.coerceAtLeast(0).toInt()
    return stringResource(
        R.string.card_a11y,
        state.sim.name,
        carrierName(state.sim),
        pluralStringResource(R.plurals.days_left, days, days),
        DateFormats.spoken(state.deadlines.effective, locale),
        statusWord(state.status),
    )
}
