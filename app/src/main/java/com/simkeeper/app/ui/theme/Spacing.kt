package com.simkeeper.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 4 dp grid. Use these steps only. */
@Immutable
data class Spacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    val huge: Dp = 48.dp,
    /** Screen side padding. */
    val screen: Dp = 20.dp,
)

/** Component dimensions from DESIGN_SYSTEM.md "Components". */
@Immutable
data class Sizes(
    val touchTarget: Dp = 48.dp,
    val buttonHeight: Dp = 56.dp,
    val buttonIcon: Dp = 18.dp,
    val chipHeight: Dp = 40.dp,
    val planChipHeight: Dp = 64.dp,
    val monogram: Dp = 44.dp,
    val icon: Dp = 24.dp,
    val iconSmall: Dp = 20.dp,
    val iconTiny: Dp = 16.dp,
    val cardPadding: Dp = 18.dp,
    val outline: Dp = 1.5.dp,
    val outlineSelected: Dp = 2.dp,
    val cardElevation: Dp = 1.dp,
    val noElevation: Dp = 0.dp,
    val floatingElevation: Dp = 8.dp,
    val ringCard: Dp = 76.dp,
    val ringCardStroke: Dp = 8.dp,
    val ringDetail: Dp = 120.dp,
    val ringDetailStroke: Dp = 12.dp,
    val ringWidget: Dp = 104.dp,
    val ringWidgetStroke: Dp = 10.dp,
    val ringHero: Dp = 200.dp,
    val ringHeroStroke: Dp = 16.dp,
    val tagPaddingVertical: Dp = 2.dp,
    val tagPaddingHorizontal: Dp = 8.dp,
    val bannerPaddingVertical: Dp = 12.dp,
    val bannerPaddingHorizontal: Dp = 14.dp,
    val sheetHandleWidth: Dp = 36.dp,
    val sheetHandleHeight: Dp = 4.dp,
    val pageDot: Dp = 8.dp,
    val pageDotActive: Dp = 24.dp,
    val divider: Dp = 1.dp,
    val illustration: Dp = 240.dp,
    val logoTile: Dp = 32.dp,
    val maxContentWidth: Dp = 600.dp,
    val fabBottomClearance: Dp = 96.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
val LocalSizes = staticCompositionLocalOf { Sizes() }
