package com.simkeeper.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.simkeeper.app.R
import com.simkeeper.app.ui.theme.SimKeeperTheme
import java.time.LocalTime

fun languageLabel(language: AppLanguage): Int = when (language) {
    AppLanguage.ENGLISH -> R.string.language_english
    AppLanguage.HINDI -> R.string.language_hindi
    AppLanguage.MARATHI -> R.string.language_marathi
}

@Composable
fun LanguageDialog(current: AppLanguage, onSelect: (AppLanguage) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language)) },
        text = {
            Column(Modifier.selectableGroup()) {
                AppLanguage.entries.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = SimKeeperTheme.sizes.touchTarget)
                            .selectable(selected = language == current, role = Role.RadioButton) {
                                onSelect(language)
                                onDismiss()
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md),
                    ) {
                        RadioButton(
                            selected = language == current,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = SimKeeperTheme.colors.primary),
                        )
                        Text(stringResource(languageLabel(language)), style = SimKeeperTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
        containerColor = SimKeeperTheme.colors.surface,
        shape = SimKeeperTheme.shapes.dialog,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTimeDialog(initial: LocalTime, onPick: (LocalTime) -> Unit, onDismiss: () -> Unit) {
    val is24h = android.text.format.DateFormat.is24HourFormat(LocalContext.current)
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = is24h)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_reminder_time)) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = {
                onPick(LocalTime.of(state.hour, state.minute))
                onDismiss()
            }) { Text(stringResource(R.string.action_ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
        containerColor = SimKeeperTheme.colors.surface,
        shape = SimKeeperTheme.shapes.dialog,
    )
}
