package com.simkeeper.app.ui.theme

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/** Motion values from DESIGN_SYSTEM.md "Motion". */
object Motion {
    const val RING_FILL_MS = 700
    const val CARD_STAGGER_MS = 40
    const val CARD_DAMPING = 0.8f
    val cardSlide = 12.dp
    const val RECHARGE_MS = 500
    const val CONFETTI_MS = 1200
    const val CONFETTI_MAX_PARTICLES = 40
}

/** True when the user turned on "Remove animations" (animator scale 0). Skip all motion. */
val LocalReduceMotion = staticCompositionLocalOf { false }

@Composable
internal fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) { context.animatorScale() == 0f }
}

private fun Context.animatorScale(): Float =
    Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
