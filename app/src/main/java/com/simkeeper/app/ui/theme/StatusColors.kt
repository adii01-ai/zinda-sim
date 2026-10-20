package com.simkeeper.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Raw urgency and carrier values from DESIGN_SYSTEM.md. Never dynamic.
 * Three light text colours are 1-2.5% darker than the spec hex so text on their tinted tile
 * meets the spec's own 4.5:1 contrast rule (spec: safe #1B7F4F 4.36:1, soon #A35A00 4.47:1, BSNL #C2410C 4.46:1).
 */
internal object StatusPalette {
    val safeLight = Color(0xFF1A7B4D)
    val safeDark = Color(0xFF6FD39C)
    val safeContainerLight = Color(0xFFE3F3EA)
    val safeContainerDark = Color(0xFF123B27)

    val soonLight = Color(0xFFA15900)
    val soonDark = Color(0xFFF2B35E)
    val soonContainerLight = Color(0xFFFCEBD3)
    val soonContainerDark = Color(0xFF3E2A0E)

    val urgentLight = Color(0xFFB42318)
    val urgentDark = Color(0xFFFF8A7E)
    val urgentContainerLight = Color(0xFFFDE4E1)
    val urgentContainerDark = Color(0xFF4A1611)

    val expiredLight = Color(0xFF52615B)
    val expiredDark = Color(0xFFA9B8B2)
    val expiredContainerLight = Color(0xFFEEF2EF)
    val expiredContainerDark = Color(0xFF22302B)

    val jioText = Color(0xFF1D4ED8)
    val jioTile = Color(0xFFE3EAFD)
    val airtelText = Color(0xFFB42318)
    val airtelTile = Color(0xFFFDE4E1)
    val viText = Color(0xFF6D28D9)
    val viTile = Color(0xFFEEE6FD)
    val bsnlText = Color(0xFFBF400B)
    val bsnlTile = Color(0xFFFDEBDD)
    val otherText = Color(0xFF52615B)
    val otherTile = Color(0xFFEEF2EF)
}

/** A content colour and its container (ring track, banner, chip background). */
@Immutable
data class ColorPair(val content: Color, val container: Color)

@Immutable
data class StatusColors(
    val safe: ColorPair,
    val soon: ColorPair,
    val urgent: ColorPair,
    val expired: ColorPair,
    val jio: ColorPair,
    val airtel: ColorPair,
    val vi: ColorPair,
    val bsnl: ColorPair,
    val other: ColorPair,
)

private const val DARK_CARRIER_LIGHTEN = 0.3f
private const val DARK_CARRIER_TILE_ALPHA = 0.2f

/**
 * Dark mode carrier tiles: text lightened 30% (HSL lightness, which keeps contrast above
 * 4.5:1 on the dark surface) on a 20% opacity tile of that colour.
 */
private fun darkCarrier(text: Color): ColorPair {
    val lightened = text.lightenHsl(DARK_CARRIER_LIGHTEN)
    return ColorPair(content = lightened, container = lightened.copy(alpha = DARK_CARRIER_TILE_ALPHA))
}

private fun Color.lightenHsl(amount: Float): Color {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val lightness = (max + min) / 2f
    val delta = max - min
    if (delta == 0f) return Color.hsl(0f, 0f, (lightness + amount).coerceAtMost(1f))
    val saturation = delta / (1f - kotlin.math.abs(2f * lightness - 1f))
    val hueSector = when (max) {
        red -> ((green - blue) / delta).mod(6f)
        green -> (blue - red) / delta + 2f
        else -> (red - green) / delta + 4f
    }
    return Color.hsl(hueSector * 60f, saturation.coerceIn(0f, 1f), (lightness + amount).coerceAtMost(1f))
}

internal val LightStatusColors = with(StatusPalette) {
    StatusColors(
        safe = ColorPair(safeLight, safeContainerLight),
        soon = ColorPair(soonLight, soonContainerLight),
        urgent = ColorPair(urgentLight, urgentContainerLight),
        expired = ColorPair(expiredLight, expiredContainerLight),
        jio = ColorPair(jioText, jioTile),
        airtel = ColorPair(airtelText, airtelTile),
        vi = ColorPair(viText, viTile),
        bsnl = ColorPair(bsnlText, bsnlTile),
        other = ColorPair(otherText, otherTile),
    )
}

internal val DarkStatusColors = with(StatusPalette) {
    StatusColors(
        safe = ColorPair(safeDark, safeContainerDark),
        soon = ColorPair(soonDark, soonContainerDark),
        urgent = ColorPair(urgentDark, urgentContainerDark),
        expired = ColorPair(expiredDark, expiredContainerDark),
        jio = darkCarrier(jioText),
        airtel = darkCarrier(airtelText),
        vi = darkCarrier(viText),
        bsnl = darkCarrier(bsnlText),
        other = darkCarrier(otherText),
    )
}

val LocalStatusColors = staticCompositionLocalOf { LightStatusColors }
