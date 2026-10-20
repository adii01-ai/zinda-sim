package com.simkeeper.app.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.domain.model.Carrier
import com.simkeeper.app.ui.components.MonogramTile
import com.simkeeper.app.ui.components.PrimaryButton
import com.simkeeper.app.ui.components.carrierName
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.preview.SampleData
import com.simkeeper.app.ui.theme.SimKeeperTheme
import java.time.LocalDate

/** Who the sheet is for: "Bank SIM", "Airtel · slot 2", step "1 of 2". */
data class ValidityHeader(
    val name: String,
    val carrier: Carrier,
    val carrierLabel: String?,
    val slot: Int?,
    val step: Int? = null,
    val stepCount: Int? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetValiditySheet(
    header: ValidityHeader,
    form: ValidityForm,
    today: LocalDate,
    saveLabel: String,
    onChange: (ValidityForm) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = SimKeeperTheme.shapes.bottomSheet,
        containerColor = SimKeeperTheme.colors.surface,
    ) {
        SetValiditySheetBody(header, form, today, saveLabel, onChange, onSave)
    }
}

@Composable
private fun SetValiditySheetBody(
    header: ValidityHeader,
    form: ValidityForm,
    today: LocalDate,
    saveLabel: String,
    onChange: (ValidityForm) -> Unit,
    onSave: () -> Unit,
) {
    val spacing = SimKeeperTheme.spacing
    Column(modifier = Modifier.padding(horizontal = spacing.screen).navigationBarsPadding()) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.xl),
        ) {
            SheetHeader(header)
            SetValidityContent(form = form, today = today, onChange = onChange)
        }
        PrimaryButton(
            text = saveLabel,
            onClick = onSave,
            enabled = form.isComplete,
            modifier = Modifier.padding(vertical = spacing.lg),
        )
    }
}

@Composable
fun SheetHeader(header: ValidityHeader) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.md),
    ) {
        MonogramTile(header.carrier, header.carrierLabel)
        Column(Modifier.weight(1f)) {
            Text(
                text = header.name,
                style = SimKeeperTheme.typography.titleLarge,
                color = SimKeeperTheme.colors.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            val carrier = carrierName(header.carrier, header.carrierLabel)
            Text(
                text = if (header.slot != null) stringResource(R.string.validity_carrier_slot, carrier, header.slot) else carrier,
                style = SimKeeperTheme.typography.bodyMedium,
                color = SimKeeperTheme.colors.onSurfaceVariant,
            )
        }
        if (header.step != null && header.stepCount != null && header.stepCount > 1) {
            Text(
                text = stringResource(R.string.validity_step, header.step, header.stepCount),
                style = SimKeeperTheme.typography.labelMedium,
                color = SimKeeperTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SetValiditySheetBodyPreview() {
    PreviewSurface {
        SetValiditySheetBody(
            header = ValidityHeader("Bank SIM", Carrier.AIRTEL, null, slot = 2, step = 1, stepCount = 2),
            form = ValidityForm(),
            today = SampleData.today,
            saveLabel = stringResource(R.string.action_save),
            onChange = {},
            onSave = {},
        )
    }
}
