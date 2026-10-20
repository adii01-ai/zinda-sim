package com.simkeeper.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simkeeper.app.data.repo.SettingsRepository
import com.simkeeper.app.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AppStartState(
    val onboardingDone: Boolean,
    val themeMode: ThemeMode,
    val dynamicColor: Boolean,
)

@HiltViewModel
class MainViewModel @Inject constructor(settingsRepository: SettingsRepository) : ViewModel() {
    /** null until DataStore is read; the splash screen stays up until then. */
    val state: StateFlow<AppStartState?> = settingsRepository.settings
        .map { AppStartState(it.onboardingDone, it.themeMode, it.dynamicColor) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
