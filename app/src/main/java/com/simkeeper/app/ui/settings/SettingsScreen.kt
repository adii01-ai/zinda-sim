package com.simkeeper.app.ui.settings

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simkeeper.app.R
import com.simkeeper.app.domain.model.ThemeMode
import com.simkeeper.app.ui.components.SectionLabel
import com.simkeeper.app.ui.format.DateFormats
import com.simkeeper.app.ui.format.currentLocale
import com.simkeeper.app.ui.preview.SampleData
import com.simkeeper.app.ui.theme.SimKeeperTheme
import java.time.LocalTime

class SettingsActions(
    val onBack: () -> Unit,
    val onShare: () -> Unit,
    val onThemeMode: (ThemeMode) -> Unit,
    val onDynamicColor: (Boolean) -> Unit,
    val onLanguage: (AppLanguage) -> Unit,
    val onReminderTime: (LocalTime) -> Unit,
    val onQuietHours: (Boolean) -> Unit,
)

@Composable
fun SettingsRoute(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val share = rememberShareAction()
    SettingsScreen(
        state = state,
        language = currentAppLanguage(),
        actions = SettingsActions(
            onBack = onBack,
            onShare = share,
            onThemeMode = viewModel::onThemeMode,
            onDynamicColor = viewModel::onDynamicColor,
            onLanguage = viewModel::onLanguage,
            onReminderTime = viewModel::onReminderTime,
            onQuietHours = viewModel::onQuietHours,
        ),
    )
}

/** The app language, or the device language when none was picked. */
@Composable
private fun currentAppLanguage(): AppLanguage {
    val tag = AppCompatDelegate.getApplicationLocales()[0]?.language ?: currentLocale().language
    return AppLanguage.entries.firstOrNull { it.tag == tag } ?: AppLanguage.ENGLISH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(state: SettingsUiState, language: AppLanguage, actions: SettingsActions) {
    var showLanguage by rememberSaveable { mutableStateOf(false) }
    var showTime by rememberSaveable { mutableStateOf(false) }
    val spacing = SimKeeperTheme.spacing
    Scaffold(
        containerColor = SimKeeperTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), style = SimKeeperTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SimKeeperTheme.colors.background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screen, vertical = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            ShareCard(onClick = actions.onShare)
            SectionLabel(stringResource(R.string.settings_appearance), Modifier.padding(top = spacing.md))
            AppearanceGroup(state, language, actions, onLanguageClick = { showLanguage = true })
            SectionLabel(stringResource(R.string.settings_reminders), Modifier.padding(top = spacing.md))
            RemindersGroup(state, actions, onTimeClick = { showTime = true })
            SectionLabel(stringResource(R.string.settings_your_data), Modifier.padding(top = spacing.md))
            DataGroup()
            SettingsFooter(state)
        }
    }
    if (showLanguage) LanguageDialog(language, actions.onLanguage, onDismiss = { showLanguage = false })
    if (showTime) ReminderTimeDialog(state.settings.reminderTime, actions.onReminderTime, onDismiss = { showTime = false })
}

@Composable
private fun DataGroup() {
    // Backup and restore arrive in v1.1.
    SettingsGroup {
        val soon = stringResource(R.string.settings_coming_soon)
        SettingsRow(stringResource(R.string.settings_backup), body = soon, enabled = false, leadingIcon = R.drawable.ic_backup)
        SettingsDivider()
        SettingsRow(stringResource(R.string.settings_restore), body = soon, enabled = false, leadingIcon = R.drawable.ic_restore)
    }
}

@Composable
private fun SettingsFooter(state: SettingsUiState) {
    val spacing = SimKeeperTheme.spacing
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
            Icon(painterResource(R.drawable.ic_lock), contentDescription = null, tint = SimKeeperTheme.colors.primary, modifier = Modifier.size(SimKeeperTheme.sizes.iconTiny))
            Text(stringResource(R.string.settings_privacy), style = SimKeeperTheme.typography.labelLarge, color = SimKeeperTheme.colors.primary)
        }
        val checked = state.rulesLastChecked?.let { DateFormats.date(it, currentLocale()) }.orEmpty()
        Text(
            stringResource(R.string.settings_footer, checked, state.versionName),
            style = SimKeeperTheme.typography.bodyMedium,
            color = SimKeeperTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@PreviewLightDark
@Composable
private fun SettingsScreenPreview() {
    SimKeeperTheme {
        SettingsScreen(
            state = SettingsUiState(rulesLastChecked = SampleData.today),
            language = AppLanguage.ENGLISH,
            actions = SettingsActions({}, {}, {}, {}, {}, {}, {}),
        )
    }
}

internal val supportsDynamicColor: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
