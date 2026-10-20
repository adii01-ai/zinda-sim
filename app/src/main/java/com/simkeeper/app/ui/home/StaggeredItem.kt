package com.simkeeper.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import com.simkeeper.app.ui.theme.Motion
import com.simkeeper.app.ui.theme.SimKeeperTheme
import kotlinx.coroutines.delay

/** Cards enter with a 40 ms stagger: slide 12 dp + fade, spring (damping 0.8). Skipped with reduced motion. */
@Composable
fun StaggeredItem(index: Int, animate: Boolean, content: @Composable () -> Unit) {
    val run = animate && !SimKeeperTheme.reduceMotion
    val visible = remember { MutableTransitionState(!run) }
    LaunchedEffect(Unit) {
        if (run) {
            delay(index.toLong() * Motion.CARD_STAGGER_MS)
            visible.targetState = true
        }
    }
    val slidePx = with(LocalDensity.current) { Motion.cardSlide.roundToPx() }
    AnimatedVisibility(
        visibleState = visible,
        enter = fadeIn(spring(dampingRatio = Motion.CARD_DAMPING)) +
            slideInVertically(spring(dampingRatio = Motion.CARD_DAMPING)) { slidePx },
    ) {
        content()
    }
}
