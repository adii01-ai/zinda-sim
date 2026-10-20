package com.simkeeper.app.ui.preview

import com.simkeeper.app.domain.SimState
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.domain.model.Recharge
import com.simkeeper.app.domain.model.Sim
import com.simkeeper.app.domain.simStateOf
import java.time.LocalDate

/** Sample data from SCREENS.md. Every @Preview uses these so all screens agree. */
object SampleData {
    /** Monday 19 Oct 2026. */
    val today: LocalDate = LocalDate.of(2026, 10, 19)

    val bankSim = Sim(
        id = 1,
        name = "Bank SIM",
        carrier = Carrier.AIRTEL,
        slot = 2,
        planDays = 28,
        lastRechargeDate = LocalDate.of(2026, 9, 23),
        lastUsedDate = LocalDate.of(2026, 10, 15),
        createdAt = 0,
    )

    val dailySim = Sim(
        id = 2,
        name = "Daily SIM",
        carrier = Carrier.JIO,
        slot = 1,
        planDays = 84,
        lastRechargeDate = LocalDate.of(2026, 9, 26),
        lastUsedDate = today,
        createdAt = 0,
    )

    val mummysPhone = Sim(
        id = 3,
        name = "Mummy's phone",
        carrier = Carrier.VI,
        isFamily = true,
        ownerName = "Mummy",
        planDays = 28,
        lastRechargeDate = LocalDate.of(2026, 10, 3),
        lastUsedDate = today,
        createdAt = 0,
    )

    val bankState: SimState = simStateOf(bankSim, today)
    val dailyState: SimState = simStateOf(dailySim, today)
    val mummyState: SimState = simStateOf(mummysPhone, today)

    val soonState: SimState = simStateOf(
        dailySim.copy(id = 4, name = "Work", carrier = Carrier.BSNL, lastRechargeDate = LocalDate.of(2026, 9, 26), planDays = 28),
        today,
    )
    val expiredState: SimState = simStateOf(
        bankSim.copy(id = 5, lastRechargeDate = LocalDate.of(2026, 9, 1), lastUsedDate = LocalDate.of(2026, 9, 1)),
        today,
    )

    val allStates: List<SimState> = listOf(bankState, dailyState, mummyState)

    val bankHistory = listOf(
        Recharge(id = 2, simId = 1, date = LocalDate.of(2026, 9, 23), planDays = 28),
        Recharge(id = 1, simId = 1, date = LocalDate.of(2026, 8, 26), planDays = 28),
    )
}
