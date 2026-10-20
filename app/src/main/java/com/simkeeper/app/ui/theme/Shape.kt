package com.simkeeper.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Corner radii from DESIGN_SYSTEM.md. */
@Immutable
data class SimKeeperShapes(
    val card: Shape = RoundedCornerShape(24.dp),
    val bottomSheet: Shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    val pill: Shape = CircleShape,
    val monogram: Shape = RoundedCornerShape(14.dp),
    /** Plan chips and other small tiles. */
    val tile: Shape = RoundedCornerShape(14.dp),
    val tag: Shape = RoundedCornerShape(8.dp),
    val banner: Shape = RoundedCornerShape(16.dp),
    val snackbar: Shape = RoundedCornerShape(18.dp),
    val dialog: Shape = RoundedCornerShape(32.dp),
)

internal val MaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

val LocalSimKeeperShapes = staticCompositionLocalOf { SimKeeperShapes() }
