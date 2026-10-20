package com.simkeeper.app.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.simkeeper.app.R
import com.simkeeper.app.domain.model.ThemeMode
import com.simkeeper.app.ui.components.SelectChip
import com.simkeeper.app.ui.format.currentLocale
import com.simkeeper.app.ui.theme.SimKeeperTheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private const val WHATSAPP_PACKAGE = "com.whatsapp"
private const val TEXT_PLAIN = "text/plain"

private fun themeLabel(mode: ThemeMode): Int = when (mode) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system
    ThemeMode.LIGHT -> R.string.settings_theme_light
    ThemeMode.DARK -> R.string.settings_theme_dark
}

@Composable
fun AppearanceGroup(state: SettingsUiState, language: AppLanguage, actions: SettingsActions, onLanguageClick: () -> Unit) {
    SettingsGroup {
        Column(Modifier.padding(SimKeeperTheme.spacing.lg)) {
            Text(stringResource(R.string.settings_theme), style = SimKeeperTheme.typography.bodyLarge, color = SimKeeperTheme.colors.onSurface)
            ThemePicker(
                selected = state.settings.themeMode,
                onSelect = actions.onThemeMode,
                modifier = Modifier.fillMaxWidth().padding(top = SimKeeperTheme.spacing.sm),
            )
        }
        if (supportsDynamicColor) {
            SettingsDivider()
            SwitchRow(
                stringResource(R.string.settings_dynamic_color),
                stringResource(R.string.settings_dynamic_color_body),
                state.settings.dynamicColor,
                actions.onDynamicColor,
            )
        }
        SettingsDivider()
        SettingsRow(
            stringResource(R.string.settings_language),
            value = stringResource(languageLabel(language)),
            onClick = onLanguageClick,
            trailing = { ChevronIcon() },
        )
    }
}

/** System / Light / Dark: a segmented row, or wrapping chips when large text would break the labels. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ThemePicker(selected: ThemeMode, onSelect: (ThemeMode) -> Unit, modifier: Modifier = Modifier) {
    if (SimKeeperTheme.isLargeFont) {
        val gap = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)
        FlowRow(modifier.selectableGroup(), horizontalArrangement = gap, verticalArrangement = gap) {
            ThemeMode.entries.forEach { mode ->
                SelectChip(stringResource(themeLabel(mode)), selected = selected == mode, onClick = { onSelect(mode) })
            }
        }
        return
    }
    SingleChoiceSegmentedButtonRow(modifier) {
        ThemeMode.entries.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = selected == mode,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = SimKeeperTheme.colors.primaryContainer,
                    activeContentColor = SimKeeperTheme.colors.primary,
                ),
            ) { Text(stringResource(themeLabel(mode)), style = SimKeeperTheme.type.chip) }
        }
    }
}

@Composable
fun RemindersGroup(state: SettingsUiState, actions: SettingsActions, onTimeClick: () -> Unit) {
    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(currentLocale())
    SettingsGroup {
        SettingsRow(
            stringResource(R.string.settings_reminder_time),
            value = formatter.format(state.settings.reminderTime),
            onClick = onTimeClick,
        )
        SettingsDivider()
        SwitchRow(
            stringResource(R.string.settings_quiet_hours),
            stringResource(R.string.settings_quiet_hours_body),
            state.settings.quietHours,
            actions.onQuietHours,
        )
    }
}

/** ACTION_SEND with the Play Store link: WhatsApp if installed, otherwise the share sheet. */
@Composable
fun rememberShareAction(): () -> Unit {
    val context = LocalContext.current
    val link = stringResource(R.string.play_store_url, context.packageName)
    val text = stringResource(R.string.settings_share_text, link)
    val chooserTitle = stringResource(R.string.settings_share_chooser)
    return remember(text, chooserTitle) {
        {
            val send = Intent(Intent.ACTION_SEND).setType(TEXT_PLAIN).putExtra(Intent.EXTRA_TEXT, text)
            try {
                context.startActivity(Intent(send).setPackage(WHATSAPP_PACKAGE))
            } catch (_: ActivityNotFoundException) {
                context.startActivity(Intent.createChooser(send, chooserTitle))
            }
        }
    }
}
