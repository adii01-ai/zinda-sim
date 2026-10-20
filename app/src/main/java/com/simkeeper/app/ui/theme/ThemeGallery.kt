package com.simkeeper.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp

// Developer-only gallery: labels are token names, not user-facing text.

@Composable
private fun Swatch(name: String, color: Color, onColor: Color) {
    val sizes = SimKeeperTheme.sizes
    Column(
        modifier = Modifier
            .width(sizes.illustration / 2)
            .background(color, SimKeeperTheme.shapes.tag)
            .border(sizes.divider, SimKeeperTheme.colors.outline, SimKeeperTheme.shapes.tag)
            .padding(SimKeeperTheme.spacing.sm),
    ) {
        Text(name, style = SimKeeperTheme.typography.labelSmall, color = onColor)
    }
}

@Composable
private fun GallerySection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)) {
        Text(title.uppercase(), style = SimKeeperTheme.typography.labelMedium, color = SimKeeperTheme.colors.onSurfaceVariant)
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorTokens() {
    val c = SimKeeperTheme.colors
    val gap = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)
    FlowRow(horizontalArrangement = gap, verticalArrangement = gap) {
        Swatch("primary", c.primary, c.onPrimary)
        Swatch("primaryContainer", c.primaryContainer, c.onPrimaryContainer)
        Swatch("background", c.background, c.onBackground)
        Swatch("surface", c.surface, c.onSurface)
        Swatch("surfaceVariant", c.surfaceVariant, c.onSurfaceVariant)
        Swatch("outline", c.outline, c.onSurface)
        Swatch("inverseSurface", c.inverseSurface, c.inverseOnSurface)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PairTokens() {
    val s = SimKeeperTheme.status
    val gap = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)
    val pairs = listOf(
        "safe" to s.safe, "soon" to s.soon, "urgent" to s.urgent, "expired" to s.expired,
        "jio" to s.jio, "airtel" to s.airtel, "vi" to s.vi, "bsnl" to s.bsnl, "other" to s.other,
    )
    FlowRow(horizontalArrangement = gap, verticalArrangement = gap) {
        pairs.forEach { (name, pair) -> Swatch(name, pair.container, pair.content) }
    }
}

@Composable
private fun TypeTokens() {
    val t = SimKeeperTheme.typography
    val x = SimKeeperTheme.type
    val styles: List<Pair<String, TextStyle>> = listOf(
        "displayNumber 61" to x.displayNumber,
        "ringNumber 61" to x.ringNumber,
        "chipNumber 28" to x.chipNumber,
        "headlineLarge" to t.headlineLarge,
        "headlineMedium" to t.headlineMedium,
        "titleLarge" to t.titleLarge,
        "titleMedium" to t.titleMedium,
        "bodyLarge" to t.bodyLarge,
        "bodyMedium" to t.bodyMedium,
        "labelLarge" to t.labelLarge,
        "LABELMEDIUM" to t.labelMedium,
        "labelSmall" to t.labelSmall,
        "monogram J" to x.monogram,
        "textLink" to x.textLink,
    )
    styles.forEach { (name, style) -> Text(name, style = style, color = SimKeeperTheme.colors.onSurface) }
}

@Composable
private fun ShapeTokens() {
    val sh = SimKeeperTheme.shapes
    val shapes: List<Pair<String, Shape>> = listOf(
        "card" to sh.card, "pill" to sh.pill, "monogram" to sh.monogram,
        "tag" to sh.tag, "banner" to sh.banner, "snackbar" to sh.snackbar,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.sm)) {
        shapes.forEach { (name, shape) ->
            Box(
                modifier = Modifier
                    .size(SimKeeperTheme.sizes.ringCard)
                    .background(SimKeeperTheme.colors.primaryContainer, shape),
                contentAlignment = Alignment.Center,
            ) { Text(name, style = SimKeeperTheme.typography.labelSmall, color = SimKeeperTheme.colors.onPrimaryContainer) }
        }
    }
}

@Composable
private fun SpacingTokens() {
    val sp = SimKeeperTheme.spacing
    val steps: List<Pair<String, Dp>> = listOf(
        "xs" to sp.xs, "sm" to sp.sm, "md" to sp.md, "lg" to sp.lg,
        "xl / screen" to sp.xl, "xxl" to sp.xxl, "xxxl" to sp.xxxl,
    )
    steps.forEach { (name, dp) ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(sp.sm)) {
            Box(Modifier.width(dp).height(sp.md).background(SimKeeperTheme.colors.primary))
            Text("$name $dp", style = SimKeeperTheme.typography.bodyMedium, color = SimKeeperTheme.colors.onSurface)
        }
    }
}

@PreviewLightDark
@Composable
private fun ThemeGalleryPreview() {
    SimKeeperTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SimKeeperTheme.colors.background)
                .padding(SimKeeperTheme.spacing.screen),
            verticalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.xxl),
        ) {
            GallerySection("Brand and neutrals") { ColorTokens() }
            GallerySection("Urgency and carriers") { PairTokens() }
            GallerySection("Type") { TypeTokens() }
            GallerySection("Shapes") { ShapeTokens() }
            GallerySection("Spacing") { SpacingTokens() }
        }
    }
}
