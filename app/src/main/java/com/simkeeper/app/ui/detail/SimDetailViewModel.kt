package com.simkeeper.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.simkeeper.app.data.repo.CarrierInfoRepository
import com.simkeeper.app.data.repo.NotificationLog
import com.simkeeper.app.data.repo.SimRepository
import com.simkeeper.app.domain.SimState
import com.simkeeper.app.domain.model.PlanOffer
import com.simkeeper.app.domain.model.Recharge
import com.simkeeper.app.domain.usecase.ComputeDeadlinesUseCase
import com.simkeeper.app.domain.usecase.MarkRechargedUseCase
import com.simkeeper.app.domain.usecase.RechargeReceipt
import com.simkeeper.app.domain.usecase.RechargeUndoStore
import com.simkeeper.app.navigation.SimDetailRoute
import com.simkeeper.app.ui.setup.ValidityForm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CarrierExtras(val plans: List<PlanOffer> = emptyList(), val pricesAsOf: YearMonth? = null, val graceNote: String? = null)

data class DetailLocal(
    val usedUpdated: Boolean = false,
    val editForm: ValidityForm? = null,
    val showAllHistory: Boolean = false,
    val deleted: Boolean = false,
    val openRecharged: Boolean = false,
)

data class SimDetailUiState(
    val loading: Boolean = true,
    val simState: SimState? = null,
    val today: LocalDate? = null,
    val recharges: List<Recharge> = emptyList(),
    val reminderOffsets: Set<Int> = emptySet(),
    val extras: CarrierExtras = CarrierExtras(),
    val local: DetailLocal = DetailLocal(),
)

@HiltViewModel
class SimDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val simRepository: SimRepository,
    private val carrierInfoRepository: CarrierInfoRepository,
    private val notificationLog: NotificationLog,
    private val computeDeadlines: ComputeDeadlinesUseCase,
    private val markRecharged: MarkRechargedUseCase,
    private val undoStore: RechargeUndoStore,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<SimDetailRoute>()
    val simId: Long = route.simId

    private val extras = MutableStateFlow(CarrierExtras())
    private val local = MutableStateFlow(
        DetailLocal(openRecharged = route.action == SimDetailRoute.ACTION_RECHARGED && savedStateHandle.get<Boolean>(KEY_ACTION_DONE) != true),
    )
    private val refresh = MutableStateFlow(0)

    init {
        if (local.value.openRecharged) savedStateHandle[KEY_ACTION_DONE] = true
        loadExtras()
    }

    val state: StateFlow<SimDetailUiState> = combine(
        combine(simRepository.observeSim(simId), refresh) { sim, _ -> sim?.let(computeDeadlines::invoke) },
        simRepository.observeRecharges(simId),
        simRepository.observeReminderOffsets(simId),
        extras,
        local,
    ) { simState, recharges, offsets, extras, local ->
        SimDetailUiState(
            loading = false,
            simState = simState,
            today = computeDeadlines.today(),
            recharges = recharges,
            reminderOffsets = offsets,
            extras = extras,
            local = local.copy(deleted = local.deleted || simState == null),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), SimDetailUiState())

    val pendingUndo: StateFlow<RechargeReceipt?> = undoStore.pending

    private fun loadExtras() {
        viewModelScope.launch {
            val carrier = simRepository.getSim(simId)?.carrier ?: return@launch
            val info = carrierInfoRepository.info()
            extras.value = CarrierExtras(
                plans = info.plansFor(carrier).map { PlanOffer(it.rupees, it.days) },
                pricesAsOf = info.pricesAsOf,
                graceNote = info.graceNote(carrier),
            )
        }
    }

    fun refresh() {
        refresh.value++
    }

    fun onRechargedOpened() = local.update { it.copy(openRecharged = false) }

    fun onUsedToday() {
        val sim = state.value.simState?.sim ?: return
        viewModelScope.launch {
            simRepository.updateSim(sim.copy(lastUsedDate = computeDeadlines.today()))
            local.update { it.copy(usedUpdated = true) }
        }
    }

    fun onToggleReminder(offset: Int) {
        val enabled = offset in state.value.reminderOffsets
        viewModelScope.launch { simRepository.setReminder(simId, offset, !enabled) }
    }

    fun onToggleHistory() = local.update { it.copy(showAllHistory = !it.showAllHistory) }

    fun onEdit() {
        val sim = state.value.simState?.sim ?: return
        local.update { it.copy(editForm = ValidityForm.from(sim, computeDeadlines.today())) }
    }

    fun onEditChange(form: ValidityForm) = local.update { it.copy(editForm = form) }

    fun onEditDismiss() = local.update { it.copy(editForm = null) }

    fun onEditSave() {
        val sim = state.value.simState?.sim ?: return
        val form = local.value.editForm ?: return
        if (!form.isComplete) return
        viewModelScope.launch {
            simRepository.updateSim(form.applyTo(sim, computeDeadlines.today()))
            notificationLog.clear(simId)
            local.update { it.copy(editForm = null, usedUpdated = false) }
        }
    }

    fun onDelete() {
        viewModelScope.launch {
            local.update { it.copy(deleted = true) }
            notificationLog.clear(simId)
            simRepository.deleteSim(simId)
        }
    }

    fun onUndo(receipt: RechargeReceipt) {
        viewModelScope.launch { markRecharged.undo(receipt) }
    }

    fun onUndoShown(receipt: RechargeReceipt) = undoStore.consume(receipt)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        const val KEY_ACTION_DONE = "recharged_action_done"
    }
}
