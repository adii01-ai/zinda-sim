package com.simkeeper.app.ui.setup

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.domain.Status
import com.simkeeper.app.ui.components.SelectChip
import com.simkeeper.app.ui.components.SimDatePickerDialog
import com.simkeeper.app.ui.components.statusColors
import com.simkeeper.app.ui.format.DateFormats
import com.simkeeper.app.ui.format.currentLocale
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.preview.SampleData
import com.simkeeper.app.ui.theme.SimKeeperTheme
import java.time.LocalDate
import kotlin.math.absoluteValue

private const val EXPANDED_ROTATION = 180f

/** "Plan ends on 21 Oct 2026 · 2 days left", in the status container colour. */
@Composable
fun ValidityPreviewCard(preview: ValidityPreview, modifier: Modifier = Modifier) {
    val colors = statusColors(preview.status)
    val ended = preview.status == Status.EXPIRED
    val days = preview.daysLeft.toInt().absoluteValue
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.container, SimKeeperTheme.shapes.banner)
            .padding(SimKeeperTheme.spacing.lg)
            .semantics(mergeDescendants = true) { },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(if (ended) R.string.validity_preview_ended else R.string.validity_preview_ends),
                style = SimKeeperTheme.typography.bodyMedium,
                color = SimKeeperTheme.colors.onSurfaceVariant,
            )
            Text(
                text = DateFormats.date(preview.end, currentLocale()),
                style = SimKeeperTheme.typography.titleLarge,
                color = SimKeeperTheme.colors.onSurface,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = days.toString(), style = SimKeeperTheme.type.previewNumber, color = colors.content)
            Text(
                text = pluralStringResource(if (ended) R.plurals.days_ago_label else R.plurals.days_left_label, days),
                style = SimKeeperTheme.typography.labelSmall,
                color = colors.content,
            )
        }
    }
}

/** Collapsed row "When did you last use this SIM?" (default Today); expands to chips. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UsedSection(form: ValidityForm, today: LocalDate, onChange: (ValidityForm) -> Unit) {
    var picking by rememberSaveable { mutableStateOf(false) }
    val locale = currentLocale()
    val usedDate = form.usedDate(today)
    val value = if (usedDate == today) stringResource(R.string.today) else DateFormats.date(usedDate, locale)
    Column(modifier = Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = SimKeeperTheme.sizes.touchTarget)
                .clickable(role = Role.Button) { onChange(form.copy(usedExpanded = !form.usedExpanded)) }
                .semantics { stateDescription = value },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.sm),
        ) {
            Text(
                text = stringResource(R.string.validity_used_question),
                style = SimKeeperTheme.typography.bodyLarge,
                color = SimKeeperTheme.colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(value, style = SimKeeperTheme.typography.labelLarge, color = SimKeeperTheme.colors.primary)
            Icon(
                painter = painterResource(R.drawable.ic_expand_more),
                contentDescription = null,
                tint = SimKeeperTheme.colors.onSurfaceVariant,
                modifier = Modifier
                    .size(SimKeeperTheme.sizes.icon)
                    .rotate(if (form.usedExpanded) EXPANDED_ROTATION else 0f),
            )
        }
        if (form.usedExpanded) {
            val gap = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)
            FlowRow(horizontalArrangement = gap, verticalArrangement = gap) {
                listOf(
                    UsedChoice.TODAY to R.string.today,
                    UsedChoice.ONE_WEEK to R.string.validity_1_week_ago,
                    UsedChoice.ONE_MONTH to R.string.validity_1_month_ago,
                ).forEach { (choice, label) ->
                    SelectChip(stringResource(label), form.usedChoice == choice, onClick = { onChange(form.copy(usedChoice = choice)) })
                }
                val picked = form.usedChoice == UsedChoice.PICKED
                SelectChip(
                    text = if (picked) DateFormats.date(usedDate, locale) else stringResource(R.string.validity_pick_date),
                    selected = picked,
                    onClick = { picking = true },
                    icon = R.drawable.ic_calendar_month,
                )
            }
            Text(stringResource(R.string.validity_used_hint), style = SimKeeperTheme.typography.bodyMedium, color = SimKeeperTheme.colors.onSurfaceVariant)
        }
    }
    if (picking) {
        SimDatePickerDialog(
            initial = usedDate,
            maxDate = today,
            onPick = { onChange(form.copy(usedChoice = UsedChoice.PICKED, pickedUsedDate = it)) },
            onDismiss = { picking = false },
        )
    }
}

@PreviewLightDark
@Composable
private fun ValidityPreviewCardPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md)) {
            ValidityForm().preview(SampleData.today)?.let { ValidityPreviewCard(it) }
            ValidityForm(rechargeChoice = RechargeChoice.THREE_WEEKS).preview(SampleData.today)?.let { ValidityPreviewCard(it) }
            UsedSection(ValidityForm(usedExpanded = true), SampleData.today, onChange = {})
        }
    }
}
