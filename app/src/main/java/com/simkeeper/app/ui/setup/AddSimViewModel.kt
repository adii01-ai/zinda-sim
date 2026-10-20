package com.simkeeper.app.ui.setup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.simkeeper.app.data.repo.SettingsRepository
import com.simkeeper.app.data.repo.SimRepository
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.domain.model.Sim
import com.simkeeper.app.navigation.AddSimRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class AddSimUiState(
    val carrier: Carrier = Carrier.JIO,
    val carrierLabel: String = "",
    val name: String = "",
    val isFamily: Boolean = false,
    val ownerName: String = "",
    val form: ValidityForm = ValidityForm(),
    val today: LocalDate,
    val showNameError: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
) {
    val canSave: Boolean get() = !saving && form.isComplete
}

/** Manual add: carrier, name, family switch + owner, then the Set validity fields. No phone number. */
@HiltViewModel
class AddSimViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val simRepository: SimRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<AddSimRoute>()

    private val _state = MutableStateFlow(AddSimUiState(isFamily = route.family, today = LocalDate.now(clock)))
    val state: StateFlow<AddSimUiState> = _state.asStateFlow()

    fun onCarrier(carrier: Carrier) = _state.update { it.copy(carrier = carrier) }
    fun onCarrierLabel(label: String) = _state.update { it.copy(carrierLabel = label) }
    fun onName(name: String) = _state.update { it.copy(name = name, showNameError = false) }
    fun onFamily(isFamily: Boolean) = _state.update { it.copy(isFamily = isFamily) }
    fun onOwner(owner: String) = _state.update { it.copy(ownerName = owner) }
    fun onFormChange(form: ValidityForm) = _state.update { it.copy(form = form) }

    fun onSave() {
        val s = _state.value
        if (!s.canSave) return
        if (s.name.isBlank()) {
            _state.update { it.copy(showNameError = true) }
            return
        }
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val blank = Sim(
                name = s.name.trim(),
                carrier = s.carrier,
                carrierLabel = s.carrierLabel.trim().takeIf { s.carrier == Carrier.OTHER && it.isNotEmpty() },
                slot = null,
                isFamily = s.isFamily,
                ownerName = s.ownerName.trim().takeIf { s.isFamily && it.isNotEmpty() },
                planDays = 0,
                lastRechargeDate = s.today,
                lastUsedDate = s.today,
                createdAt = clock.millis(),
                sortOrder = simRepository.simCount(),
            )
            simRepository.addSim(s.form.applyTo(blank, s.today))
            if (route.finishSetup) settingsRepository.setOnboardingDone(true)
            _state.update { it.copy(saving = false, saved = true) }
        }
    }
}
