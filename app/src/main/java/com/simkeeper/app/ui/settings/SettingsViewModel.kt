package com.simkeeper.app.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simkeeper.app.BuildConfig
import com.simkeeper.app.data.repo.AppSettings
import com.simkeeper.app.data.repo.CarrierInfoRepository
import com.simkeeper.app.data.repo.SettingsRepository
import com.simkeeper.app.domain.model.ThemeMode
import com.simkeeper.app.reminders.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

enum class AppLanguage(val tag: String) { ENGLISH("en"), HINDI("hi"), MARATHI("mr") }

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val rulesLastChecked: LocalDate? = null,
    val versionName: String = BuildConfig.VERSION_NAME,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler,
    carrierInfoRepository: CarrierInfoRepository,
) : ViewModel() {

    private val lastChecked = flow<LocalDate?> { emit(carrierInfoRepository.info().lastChecked) }

    val state: StateFlow<SettingsUiState> = combine(settingsRepository.settings, lastChecked) { settings, checked ->
        SettingsUiState(settings = settings, rulesLastChecked = checked)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), SettingsUiState())

    fun onThemeMode(mode: ThemeMode) = launch { settingsRepository.setThemeMode(mode) }

    fun onDynamicColor(enabled: Boolean) = launch { settingsRepository.setDynamicColor(enabled) }

    fun onReminderTime(time: LocalTime) = launch {
        settingsRepository.setReminderTime(time)
        reminderScheduler.schedule()
    }

    fun onQuietHours(enabled: Boolean) = launch { settingsRepository.setQuietHours(enabled) }

    /** Per-app language API (AppCompat back-ports it to Android 8-12). */
    fun onLanguage(language: AppLanguage) {
        launch { settingsRepository.setLanguage(language.tag) }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
