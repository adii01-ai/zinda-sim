package com.simkeeper.app.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.ui.components.PlanChip
import com.simkeeper.app.ui.components.SelectChip
import com.simkeeper.app.ui.components.SimDatePickerDialog
import com.simkeeper.app.ui.components.TextLink
import com.simkeeper.app.ui.format.DateFormats
import com.simkeeper.app.ui.format.currentLocale
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.preview.SampleData
import com.simkeeper.app.ui.theme.SimKeeperTheme
import java.time.LocalDate

/** The body of Set validity, shared by the bottom sheet, edit mode and the manual Add SIM screen. */
@Composable
fun SetValidityContent(form: ValidityForm, today: LocalDate, onChange: (ValidityForm) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.lg)) {
        PlanSection(form, onChange)
        if (form.useExactEnd) EndDateSection(form, today, onChange) else RechargeSection(form, today, onChange)
        TextLink(
            text = stringResource(if (form.useExactEnd) R.string.validity_use_recharge_date else R.string.validity_exact_end),
            onClick = { onChange(form.copy(useExactEnd = !form.useExactEnd)) },
        )
        form.preview(today)?.let { ValidityPreviewCard(it) }
        UsedSection(form, today, onChange)
    }
}

@Composable
private fun Question(textRes: Int) {
    Text(stringResource(textRes), style = SimKeeperTheme.typography.titleMedium, color = SimKeeperTheme.colors.onSurface)
}

@Composable
private fun PlanSection(form: ValidityForm, onChange: (ValidityForm) -> Unit) {
    val spacing = SimKeeperTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        Question(R.string.validity_plan_question)
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            val days = stringResource(R.string.validity_days)
            PRESET_PLAN_DAYS.forEach { preset ->
                PlanChip(
                    number = preset.toString(),
                    caption = days,
                    selected = form.presetDays == preset,
                    onClick = { onChange(form.copy(presetDays = preset)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        SelectChip(
            text = stringResource(R.string.validity_custom),
            selected = form.isCustom,
            onClick = { onChange(form.copy(presetDays = null)) },
        )
        if (form.isCustom) {
            OutlinedTextField(
                value = form.customDaysText,
                onValueChange = { text -> onChange(form.copy(customDaysText = text.filter(Char::isDigit).take(MAX_PLAN_DIGITS))) },
                label = { Text(stringResource(R.string.validity_custom_label)) },
                isError = form.customDaysError,
                supportingText = if (form.customDaysError) {
                    { Text(stringResource(R.string.validity_custom_error)) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RechargeSection(form: ValidityForm, today: LocalDate, onChange: (ValidityForm) -> Unit) {
    var picking by rememberSaveable { mutableStateOf(false) }
    val gap = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)
    Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md)) {
        Question(R.string.validity_recharge_question)
        FlowRow(horizontalArrangement = gap, verticalArrangement = gap) {
            listOf(
                RechargeChoice.TODAY to R.string.today,
                RechargeChoice.ONE_WEEK to R.string.validity_1_week_ago,
                RechargeChoice.THREE_WEEKS to R.string.validity_3_weeks_ago,
                RechargeChoice.ONE_MONTH to R.string.validity_1_month_ago,
            ).forEach { (choice, label) ->
                SelectChip(
                    text = stringResource(label),
                    selected = form.rechargeChoice == choice,
                    onClick = { onChange(form.copy(rechargeChoice = choice)) },
                )
            }
            val picked = form.rechargeChoice == RechargeChoice.PICKED && form.pickedRechargeDate != null
            SelectChip(
                text = if (picked) DateFormats.date(form.rechargeDate(today), currentLocale()) else stringResource(R.string.validity_pick_date),
                selected = form.rechargeChoice == RechargeChoice.PICKED,
                onClick = { picking = true },
                icon = R.drawable.ic_calendar_month,
            )
        }
    }
    if (picking) {
        SimDatePickerDialog(
            initial = form.pickedRechargeDate ?: today,
            maxDate = today,
            onPick = { onChange(form.copy(rechargeChoice = RechargeChoice.PICKED, pickedRechargeDate = it)) },
            onDismiss = { picking = false },
        )
    }
}

@Composable
private fun EndDateSection(form: ValidityForm, today: LocalDate, onChange: (ValidityForm) -> Unit) {
    var picking by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md)) {
        Question(R.string.validity_end_question)
        val end = form.exactEndDate
        SelectChip(
            text = if (end != null) DateFormats.date(end, currentLocale()) else stringResource(R.string.validity_pick_date),
            selected = end != null,
            onClick = { picking = true },
            icon = R.drawable.ic_calendar_month,
        )
    }
    if (picking) {
        SimDatePickerDialog(
            initial = form.exactEndDate ?: today,
            onPick = { onChange(form.copy(exactEndDate = it)) },
            onDismiss = { picking = false },
        )
    }
}

@PreviewLightDark
@Composable
private fun SetValidityContentPreview() {
    PreviewSurface {
        SetValidityContent(form = ValidityForm(), today = SampleData.today, onChange = {})
    }
}
