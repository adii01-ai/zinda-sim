package com.simkeeper.app.ui.home

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simkeeper.app.data.repo.CarrierInfoRepository
import com.simkeeper.app.data.repo.SettingsRepository
import com.simkeeper.app.data.repo.SimRepository
import com.simkeeper.app.domain.SimState
import com.simkeeper.app.domain.Status
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.domain.model.PlanOffer
import com.simkeeper.app.domain.usecase.ComputeDeadlinesUseCase
import com.simkeeper.app.domain.usecase.MarkRechargedUseCase
import com.simkeeper.app.domain.usecase.RechargeReceipt
import com.simkeeper.app.domain.usecase.RechargeUndoStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = true,
    val today: LocalDate? = null,
    val phoneSims: List<SimState> = emptyList(),
    val familySims: List<SimState> = emptyList(),
    val plans: Map<Carrier, PlanOffer> = emptyMap(),
    val showBatteryGuide: Boolean = false,
    val manufacturer: String = "",
    /** Ask for POST_NOTIFICATIONS once the first SIM is saved. */
    val askNotifications: Boolean = false,
) {
    val isEmpty: Boolean get() = !loading && phoneSims.isEmpty() && familySims.isEmpty()
    val needCount: Int get() = (phoneSims + familySims).count { it.needsRecharge }
    val bannerStatus: Status get() = if (needCount > 0) Status.URGENT else Status.SAFE
}

/** Manufacturers known to kill background work (TASKS.md #19). */
private val AGGRESSIVE_OEMS = setOf("xiaomi", "redmi", "poco", "oppo", "vivo", "realme", "oneplus")

@HiltViewModel
class HomeViewModel @Inject constructor(
    simRepository: SimRepository,
    carrierInfoRepository: CarrierInfoRepository,
    private val settingsRepository: SettingsRepository,
    private val computeDeadlines: ComputeDeadlinesUseCase,
    private val markRecharged: MarkRechargedUseCase,
    private val undoStore: RechargeUndoStore,
) : ViewModel() {

    /** Bumped on resume so "days left" is right after midnight. */
    private val refresh = MutableStateFlow(0)

    private val plans = flow {
        val info = carrierInfoRepository.info()
        emit(Carrier.entries.mapNotNull { c -> info.cheapestPlan(c)?.let { c to PlanOffer(it.rupees, it.days) } }.toMap())
    }

    private val manufacturer = Build.MANUFACTURER.orEmpty()
    private val aggressiveOem = manufacturer.lowercase() in AGGRESSIVE_OEMS

    val state: StateFlow<HomeUiState> = combine(
        simRepository.observeSims(),
        refresh,
        plans,
        settingsRepository.settings,
    ) { sims, _, plans, settings ->
        val states = computeDeadlines(sims)
        HomeUiState(
            loading = false,
            today = computeDeadlines.today(),
            phoneSims = states.filterNot { it.sim.isFamily },
            familySims = states.filter { it.sim.isFamily },
            plans = plans,
            showBatteryGuide = aggressiveOem && !settings.batteryGuideDismissed && sims.isNotEmpty(),
            manufacturer = manufacturer,
            askNotifications = !settings.notificationsAsked && sims.isNotEmpty(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState())

    val pendingUndo: StateFlow<RechargeReceipt?> = undoStore.pending

    fun refresh() {
        refresh.value++
    }

    fun onUndo(receipt: RechargeReceipt) {
        viewModelScope.launch { markRecharged.undo(receipt) }
    }

    fun onUndoShown(receipt: RechargeReceipt) = undoStore.consume(receipt)

    fun onNotificationsAsked() {
        viewModelScope.launch { settingsRepository.setNotificationsAsked(true) }
    }

    fun onDismissBatteryGuide() {
        viewModelScope.launch { settingsRepository.setBatteryGuideDismissed(true) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
