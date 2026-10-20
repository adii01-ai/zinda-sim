package com.simkeeper.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Brand and neutrals (DESIGN_SYSTEM.md). Light / dark pairs.
internal object BrandLight {
    val primary = Color(0xFF0E6B5F)
    val onPrimary = Color(0xFFFFFFFF)
    val primaryContainer = Color(0xFFDCEFEA)
    val onPrimaryContainer = Color(0xFF0A4F46)
    val background = Color(0xFFF4F6F3)
    val surface = Color(0xFFFFFFFF)
    val surfaceVariant = Color(0xFFEEF2EF)
    val onSurface = Color(0xFF10201B)
    val onSurfaceVariant = Color(0xFF52615B)
    val outline = Color(0xFFD5DDD9)
    val inverseSurface = Color(0xFF10201B)
}

internal object BrandDark {
    val primary = Color(0xFF7FD3C2)
    val onPrimary = Color(0xFF003731)
    val primaryContainer = Color(0xFF0E4F46)
    val onPrimaryContainer = Color(0xFFBFEDE3)
    val background = Color(0xFF0E1513)
    val surface = Color(0xFF17201D)
    val surfaceVariant = Color(0xFF22302B)
    val onSurface = Color(0xFFE6EEEA)
    val onSurfaceVariant = Color(0xFFA9B8B2)
    val outline = Color(0xFF33433D)
    val inverseSurface = Color(0xFFE6EEEA)
}

internal val LightColors: ColorScheme = lightColorScheme(
    primary = BrandLight.primary,
    onPrimary = BrandLight.onPrimary,
    primaryContainer = BrandLight.primaryContainer,
    onPrimaryContainer = BrandLight.onPrimaryContainer,
    secondary = BrandLight.primary,
    onSecondary = BrandLight.onPrimary,
    secondaryContainer = BrandLight.primaryContainer,
    onSecondaryContainer = BrandLight.onPrimaryContainer,
    tertiary = BrandLight.primary,
    onTertiary = BrandLight.onPrimary,
    tertiaryContainer = BrandLight.primaryContainer,
    onTertiaryContainer = BrandLight.onPrimaryContainer,
    background = BrandLight.background,
    onBackground = BrandLight.onSurface,
    surface = BrandLight.surface,
    onSurface = BrandLight.onSurface,
    surfaceVariant = BrandLight.surfaceVariant,
    onSurfaceVariant = BrandLight.onSurfaceVariant,
    surfaceTint = BrandLight.primary,
    surfaceBright = BrandLight.surface,
    surfaceDim = BrandLight.background,
    surfaceContainerLowest = BrandLight.surface,
    surfaceContainerLow = BrandLight.surface,
    surfaceContainer = BrandLight.surface,
    surfaceContainerHigh = BrandLight.surface,
    surfaceContainerHighest = BrandLight.surfaceVariant,
    outline = BrandLight.outline,
    outlineVariant = BrandLight.outline,
    inverseSurface = BrandLight.inverseSurface,
    inverseOnSurface = BrandLight.background,
    inversePrimary = BrandDark.primary,
    error = StatusPalette.urgentLight,
    onError = BrandLight.onPrimary,
    errorContainer = StatusPalette.urgentContainerLight,
    onErrorContainer = StatusPalette.urgentLight,
)

internal val DarkColors: ColorScheme = darkColorScheme(
    primary = BrandDark.primary,
    onPrimary = BrandDark.onPrimary,
    primaryContainer = BrandDark.primaryContainer,
    onPrimaryContainer = BrandDark.onPrimaryContainer,
    secondary = BrandDark.primary,
    onSecondary = BrandDark.onPrimary,
    secondaryContainer = BrandDark.primaryContainer,
    onSecondaryContainer = BrandDark.onPrimaryContainer,
    tertiary = BrandDark.primary,
    onTertiary = BrandDark.onPrimary,
    tertiaryContainer = BrandDark.primaryContainer,
    onTertiaryContainer = BrandDark.onPrimaryContainer,
    background = BrandDark.background,
    onBackground = BrandDark.onSurface,
    surface = BrandDark.surface,
    onSurface = BrandDark.onSurface,
    surfaceVariant = BrandDark.surfaceVariant,
    onSurfaceVariant = BrandDark.onSurfaceVariant,
    surfaceTint = BrandDark.primary,
    surfaceBright = BrandDark.surfaceVariant,
    surfaceDim = BrandDark.background,
    surfaceContainerLowest = BrandDark.background,
    surfaceContainerLow = BrandDark.surface,
    surfaceContainer = BrandDark.surface,
    surfaceContainerHigh = BrandDark.surface,
    surfaceContainerHighest = BrandDark.surfaceVariant,
    outline = BrandDark.outline,
    outlineVariant = BrandDark.outline,
    inverseSurface = BrandDark.inverseSurface,
    inverseOnSurface = BrandLight.onSurface,
    inversePrimary = BrandLight.primary,
    error = StatusPalette.urgentDark,
    onError = BrandDark.onPrimary,
    errorContainer = StatusPalette.urgentContainerDark,
    onErrorContainer = StatusPalette.urgentDark,
)

/**
 * Material You: only neutral roles come from the wallpaper so the brand teal stays
 * recognisable. Urgency colours are never dynamic (they live in [StatusColors]).
 */
internal fun ColorScheme.withDynamicNeutrals(dynamic: ColorScheme): ColorScheme = copy(
    background = dynamic.background,
    onBackground = dynamic.onBackground,
    surface = dynamic.surface,
    onSurface = dynamic.onSurface,
    surfaceVariant = dynamic.surfaceVariant,
    onSurfaceVariant = dynamic.onSurfaceVariant,
    surfaceBright = dynamic.surfaceBright,
    surfaceDim = dynamic.surfaceDim,
    surfaceContainerLowest = dynamic.surfaceContainerLowest,
    surfaceContainerLow = dynamic.surfaceContainerLow,
    surfaceContainer = dynamic.surfaceContainer,
    surfaceContainerHigh = dynamic.surfaceContainerHigh,
    surfaceContainerHighest = dynamic.surfaceContainerHighest,
    outline = dynamic.outline,
    outlineVariant = dynamic.outlineVariant,
    inverseSurface = dynamic.inverseSurface,
    inverseOnSurface = dynamic.inverseOnSurface,
)
