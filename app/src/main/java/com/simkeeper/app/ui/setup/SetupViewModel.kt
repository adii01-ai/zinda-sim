package com.simkeeper.app.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simkeeper.app.data.repo.SettingsRepository
import com.simkeeper.app.data.repo.SimRepository
import com.simkeeper.app.data.telephony.DetectedSim
import com.simkeeper.app.data.telephony.SimDetector
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.domain.model.Sim
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

enum class NameChoice { DAILY, BANK, WORK, CUSTOM }

data class DetectedSimUi(
    val detected: DetectedSim,
    val nameChoice: NameChoice,
    val customName: String = "",
)

data class SetupUiState(
    val loading: Boolean = true,
    val sims: List<DetectedSimUi> = emptyList(),
    val today: LocalDate,
    /** Names resolved by the UI when "Continue" was tapped. */
    val names: List<String> = emptyList(),
    /** How many detected SIMs are already saved. */
    val savedCount: Int = 0,
    /** Index of the SIM whose Set validity sheet is open. */
    val sheetIndex: Int? = null,
    val form: ValidityForm = ValidityForm(),
    val trackedSimCount: Int = 0,
    val finished: Boolean = false,
) {
    val canContinue: Boolean
        get() = !loading && (savedCount < sims.size || trackedSimCount > 0) &&
            sims.none { it.nameChoice == NameChoice.CUSTOM && it.customName.isBlank() }
}

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val detector: SimDetector,
    private val simRepository: SimRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(SetupUiState(today = LocalDate.now(clock)))
    val state: StateFlow<SetupUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val tracked = simRepository.getAllSims().mapNotNull { it.subscriptionId }.toSet()
            val detected = withContext(Dispatchers.IO) { detector.detect() }
                .filterNot { it.subscriptionId in tracked }
            _state.update { s ->
                s.copy(loading = false, sims = detected.mapIndexed { i, sim -> DetectedSimUi(sim, defaultName(sim.slot, i)) })
            }
        }
        viewModelScope.launch {
            simRepository.observeSims().collect { sims -> _state.update { it.copy(trackedSimCount = sims.size) } }
        }
    }

    private fun defaultName(slot: Int, index: Int): NameChoice = when {
        slot == 1 -> NameChoice.DAILY
        slot == 2 -> NameChoice.BANK
        index == 0 -> NameChoice.DAILY
        else -> NameChoice.WORK
    }

    fun onNameChoice(index: Int, choice: NameChoice) = updateSim(index) { it.copy(nameChoice = choice) }

    fun onCustomName(index: Int, name: String) = updateSim(index) { it.copy(customName = name) }

    private fun updateSim(index: Int, change: (DetectedSimUi) -> DetectedSimUi) = _state.update { s ->
        s.copy(sims = s.sims.mapIndexed { i, sim -> if (i == index) change(sim) else sim })
    }

    /** [names] are the display names the UI resolved from string resources. */
    fun onContinue(names: List<String>) {
        val s = _state.value
        if (!s.canContinue) return
        if (s.savedCount >= s.sims.size) {
            finish()
        } else {
            _state.update { it.copy(names = names, sheetIndex = it.savedCount, form = ValidityForm(), today = LocalDate.now(clock)) }
        }
    }

    fun onFormChange(form: ValidityForm) = _state.update { it.copy(form = form) }

    fun onSheetDismiss() = _state.update { it.copy(sheetIndex = null) }

    fun onSave() {
        val s = _state.value
        val index = s.sheetIndex ?: return
        if (!s.form.isComplete) return
        val detected = s.sims[index].detected
        viewModelScope.launch {
            val blank = Sim(
                name = s.names.getOrElse(index) { detected.carrierName },
                carrier = detected.carrier,
                carrierLabel = detected.carrierName.takeIf { detected.carrier == Carrier.OTHER && it.isNotBlank() },
                slot = detected.slot,
                subscriptionId = detected.subscriptionId,
                planDays = 0,
                lastRechargeDate = s.today,
                lastUsedDate = s.today,
                createdAt = clock.millis(),
                sortOrder = index,
            )
            simRepository.addSim(s.form.applyTo(blank, s.today))
            val next = index + 1
            if (next < s.sims.size) {
                _state.update { it.copy(savedCount = next, sheetIndex = next, form = ValidityForm()) }
            } else {
                _state.update { it.copy(savedCount = next, sheetIndex = null) }
                finish()
            }
        }
    }

    private fun finish() {
        viewModelScope.launch {
            settingsRepository.setOnboardingDone(true)
            _state.update { it.copy(finished = true) }
        }
    }
}
