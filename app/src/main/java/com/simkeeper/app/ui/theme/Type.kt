package com.simkeeper.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.simkeeper.app.R

/** Space Grotesk: numbers only. */
val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_medium, FontWeight.Medium),
    Font(R.font.space_grotesk_bold, FontWeight.Bold),
)

/** Plus Jakarta Sans: everything else. */
val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)

private val CenteredLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun jakarta(size: Int, line: Int, weight: FontWeight, letterSpacing: Float = 0f) = TextStyle(
    fontFamily = PlusJakartaSans,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    letterSpacing = letterSpacing.sp,
    lineHeightStyle = CenteredLineHeight,
)

private fun grotesk(size: Int, line: Int, weight: FontWeight) = TextStyle(
    fontFamily = SpaceGrotesk,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    lineHeightStyle = CenteredLineHeight,
)

/** Material roles, mapped 1:1 from DESIGN_SYSTEM.md. Nothing smaller than 11 sp. */
internal val SimKeeperTypography = Typography(
    headlineLarge = jakarta(32, 38, FontWeight.ExtraBold),
    headlineMedium = jakarta(28, 34, FontWeight.ExtraBold),
    titleLarge = jakarta(18, 24, FontWeight.ExtraBold),
    titleMedium = jakarta(17, 22, FontWeight.ExtraBold),
    bodyLarge = jakarta(16, 24, FontWeight.Normal),
    bodyMedium = jakarta(14, 20, FontWeight.Medium),
    labelLarge = jakarta(16, 20, FontWeight.Bold),
    labelMedium = jakarta(13, 16, FontWeight.Bold, letterSpacing = 0.4f),
    labelSmall = jakarta(11, 14, FontWeight.Bold),
)

/** Styles outside the Material roles. */
@Immutable
data class ExtendedTypography(
    /** 56/56 Space Grotesk 700: detail ring number. */
    val displayNumber: TextStyle,
    /** 24/24 Space Grotesk 700: home card ring number. */
    val ringNumber: TextStyle,
    /** Space Grotesk 20: PlanChip number. */
    val chipNumber: TextStyle,
    /** Space Grotesk 36: Set validity preview and confirmation numbers. */
    val previewNumber: TextStyle,
    /** Jakarta 18 sp 800: monogram letter. */
    val monogram: TextStyle,
    /** labelLarge at 15 sp: TextLink. */
    val textLink: TextStyle,
    /** Jakarta 14 sp 600: chip labels. */
    val chip: TextStyle,
)

internal val SimKeeperExtendedTypography = ExtendedTypography(
    displayNumber = grotesk(56, 56, FontWeight.Bold),
    ringNumber = grotesk(24, 24, FontWeight.Bold),
    chipNumber = grotesk(20, 24, FontWeight.Bold),
    previewNumber = grotesk(36, 40, FontWeight.Bold),
    monogram = jakarta(18, 22, FontWeight.ExtraBold),
    textLink = jakarta(15, 20, FontWeight.Bold),
    chip = jakarta(14, 20, FontWeight.SemiBold),
)

val LocalExtendedTypography = staticCompositionLocalOf { SimKeeperExtendedTypography }
