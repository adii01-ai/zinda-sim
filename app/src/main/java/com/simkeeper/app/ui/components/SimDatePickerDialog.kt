package com.simkeeper.app.ui.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.simkeeper.app.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Material date picker working in [LocalDate]. [maxDate] blocks later days (for example, "max = today"). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimDatePickerDialog(
    initial: LocalDate?,
    onPick: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    maxDate: LocalDate? = null,
) {
    val selectable = remember(maxDate) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                maxDate == null || utcTimeMillis.toLocalDate() <= maxDate

            override fun isSelectableYear(year: Int): Boolean = maxDate == null || year <= maxDate.year
        }
    }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial?.toUtcMillis(),
        selectableDates = selectable,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedDateMillis != null,
                onClick = {
                    state.selectedDateMillis?.let { onPick(it.toLocalDate()) }
                    onDismiss()
                },
            ) { Text(stringResource(R.string.action_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    ) {
        DatePicker(state = state)
    }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
