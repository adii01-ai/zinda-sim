package com.simkeeper.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.simkeeper.app.domain.model.ThemeMode

val LocalDarkTheme = staticCompositionLocalOf { false }

@Composable
fun isAppInDarkTheme(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
fun SimKeeperTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = isAppInDarkTheme(themeMode)
    val baseScheme = if (darkTheme) DarkColors else LightColors
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        val dynamic = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        baseScheme.withDynamicNeutrals(dynamic)
    } else {
        baseScheme
    }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalStatusColors provides if (darkTheme) DarkStatusColors else LightStatusColors,
        LocalExtendedTypography provides SimKeeperExtendedTypography,
        LocalSimKeeperShapes provides SimKeeperShapes(),
        LocalSpacing provides Spacing(),
        LocalSizes provides Sizes(),
        LocalReduceMotion provides rememberReduceMotion(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SimKeeperTypography,
            shapes = MaterialShapes,
            content = content,
        )
    }
}

/** Single entry point for every token. Screens use only these. */
object SimKeeperTheme {
    val colors @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme
    val typography @Composable @ReadOnlyComposable get() = MaterialTheme.typography
    val type @Composable @ReadOnlyComposable get() = LocalExtendedTypography.current
    val status @Composable @ReadOnlyComposable get() = LocalStatusColors.current
    val shapes @Composable @ReadOnlyComposable get() = LocalSimKeeperShapes.current
    val spacing @Composable @ReadOnlyComposable get() = LocalSpacing.current
    val sizes @Composable @ReadOnlyComposable get() = LocalSizes.current
    val isDark @Composable @ReadOnlyComposable get() = LocalDarkTheme.current
    val reduceMotion @Composable @ReadOnlyComposable get() = LocalReduceMotion.current

    /** Large text (font scale 1.5x and up): side-by-side layouts stack instead of breaking words. */
    val isLargeFont @Composable @ReadOnlyComposable get() = LocalDensity.current.fontScale >= LARGE_FONT_SCALE
}

private const val LARGE_FONT_SCALE = 1.5f
