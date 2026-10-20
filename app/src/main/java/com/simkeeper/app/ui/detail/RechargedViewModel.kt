package com.simkeeper.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.simkeeper.app.domain.SimState
import com.simkeeper.app.domain.usecase.ComputeDeadlinesUseCase
import com.simkeeper.app.domain.usecase.MarkRechargedUseCase
import com.simkeeper.app.domain.usecase.RechargeReceipt
import com.simkeeper.app.domain.usecase.RechargeUndoStore
import com.simkeeper.app.navigation.RechargedRoute
import com.simkeeper.app.reminders.NotificationHelper
import com.simkeeper.app.ui.setup.PRESET_PLAN_DAYS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RechargedUiState(
    val receipt: RechargeReceipt? = null,
    /** Before the tap: where the ring and colour start from. */
    val before: SimState? = null,
    /** After the tap: deadlines restarted. */
    val after: SimState? = null,
    val planChoices: List<Int> = PRESET_PLAN_DAYS,
    val amountText: String = "",
    val closed: Boolean = false,
)

/**
 * The tap is recorded as soon as this screen opens ("one-tap restart"). Plan length and amount
 * can still be changed here; Done offers Undo on the previous screen, Undo here reverts at once.
 */
@HiltViewModel
class RechargedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val markRecharged: MarkRechargedUseCase,
    private val computeDeadlines: ComputeDeadlinesUseCase,
    private val undoStore: RechargeUndoStore,
    private val notificationHelper: NotificationHelper,
) : ViewModel() {

    private val simId = savedStateHandle.toRoute<RechargedRoute>().simId
    private val _state = MutableStateFlow(RechargedUiState())
    val state: StateFlow<RechargedUiState> = _state.asStateFlow()

    init {
        if (savedStateHandle.get<Boolean>(KEY_RECORDED) == true) {
            // Recreated after process death: the recharge is already saved. Just close.
            _state.update { it.copy(closed = true) }
        } else {
            savedStateHandle[KEY_RECORDED] = true
            viewModelScope.launch {
                val receipt = markRecharged(simId)
                notificationHelper.cancel(simId)
                if (receipt == null) {
                    _state.update { it.copy(closed = true) }
                } else {
                    _state.update {
                        it.copy(
                            receipt = receipt,
                            before = computeDeadlines(receipt.previous),
                            after = computeDeadlines(receipt.updated),
                            planChoices = (PRESET_PLAN_DAYS + receipt.updated.planDays).distinct().sorted(),
                        )
                    }
                }
            }
        }
    }

    fun onPlanDays(days: Int) {
        val receipt = _state.value.receipt ?: return
        if (days == receipt.updated.planDays) return
        viewModelScope.launch {
            val amended = markRecharged.amend(receipt, days, amountRupees())
            _state.update { it.copy(receipt = amended, after = computeDeadlines(amended.updated)) }
        }
    }

    fun onAmount(text: String) = _state.update { it.copy(amountText = text.filter(Char::isDigit).take(MAX_AMOUNT_DIGITS)) }

    fun onDone() {
        val receipt = _state.value.receipt ?: return close()
        viewModelScope.launch {
            val amount = amountRupees()
            val final = if (amount != receipt.amountRupees) markRecharged.amend(receipt, receipt.updated.planDays, amount) else receipt
            undoStore.post(final)
            close()
        }
    }

    fun onUndo() {
        val receipt = _state.value.receipt ?: return close()
        viewModelScope.launch {
            markRecharged.undo(receipt)
            close()
        }
    }

    private fun amountRupees(): Int? = _state.value.amountText.toIntOrNull()?.takeIf { it > 0 }

    private fun close() = _state.update { it.copy(closed = true) }

    private companion object {
        const val KEY_RECORDED = "recorded"
        const val MAX_AMOUNT_DIGITS = 6
    }
}
