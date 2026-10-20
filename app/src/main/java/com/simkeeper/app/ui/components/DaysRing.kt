package com.simkeeper.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.simkeeper.app.R
import com.simkeeper.app.domain.Status
import com.simkeeper.app.ui.preview.PreviewSurface
import com.simkeeper.app.ui.theme.Motion
import com.simkeeper.app.ui.theme.SimKeeperTheme

enum class RingSize { CARD, DETAIL, WIDGET }

private const val FULL_CIRCLE = 360f
private const val TWELVE_O_CLOCK = -90f
private const val MAX_RING_SCALE = 1.5f
private const val THREE_DIGIT_SHRINK = 0.72f

/**
 * Circular track in the status container colour, progress arc in the status colour,
 * round cap, starting at 12 o'clock. Centre: number + "DAYS".
 * Fills from 0 on first appear (700 ms) unless "Remove animations" is on.
 */
@Composable
fun DaysRing(
    days: Long,
    progress: Float,
    status: Status,
    size: RingSize,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.ring_days),
    contentDescription: String? = null,
    animateOnAppear: Boolean = true,
    fillDurationMs: Int = Motion.RING_FILL_MS,
) {
    val sizes = SimKeeperTheme.sizes
    val (baseDiameter, stroke, numberStyle) = when (size) {
        RingSize.CARD -> Triple(sizes.ringCard, sizes.ringCardStroke, SimKeeperTheme.type.ringNumber)
        RingSize.DETAIL -> Triple(sizes.ringDetail, sizes.ringDetailStroke, SimKeeperTheme.type.displayNumber)
        RingSize.WIDGET -> Triple(sizes.ringWidget, sizes.ringWidgetStroke, SimKeeperTheme.type.displayNumber)
    }
    val colors = statusColors(status)
    val reduceMotion = SimKeeperTheme.reduceMotion
    val target = progress.coerceIn(0f, 1f)

    val animated = remember { Animatable(if (animateOnAppear && !reduceMotion) 0f else target) }
    LaunchedEffect(target, reduceMotion) {
        if (reduceMotion) {
            animated.snapTo(target)
        } else {
            animated.animateTo(target, tween(fillDurationMs, easing = FastOutSlowInEasing))
        }
    }
    val arcColor by animateColorAsState(colors.content, tween(if (reduceMotion) 0 else Motion.RECHARGE_MS), label = "ringColor")
    val trackColor by animateColorAsState(colors.container, tween(if (reduceMotion) 0 else Motion.RECHARGE_MS), label = "trackColor")

    // Grow with the system font size (up to 1.5x) so the number never clips.
    val density = LocalDensity.current
    val scale = density.fontScale.coerceIn(1f, MAX_RING_SCALE)
    val diameter: Dp = baseDiameter * scale
    val number = days.coerceAtLeast(0).toString()
    val shrink = if (number.length >= 3 && size != RingSize.CARD) THREE_DIGIT_SHRINK else 1f

    val semantics = if (contentDescription != null) {
        Modifier.clearAndSetSemantics { this.contentDescription = contentDescription }
    } else {
        Modifier.clearAndSetSemantics { }
    }

    Box(modifier = modifier.size(diameter).then(semantics), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = stroke.toPx() * scale
            val inset = strokePx / 2
            val arcSize = androidx.compose.ui.geometry.Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(trackColor, 0f, FULL_CIRCLE, false, topLeft, arcSize, style = Stroke(strokePx))
            if (animated.value > 0f) {
                drawArc(
                    color = arcColor,
                    startAngle = TWELVE_O_CLOCK,
                    sweepAngle = FULL_CIRCLE * animated.value,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(strokePx, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                text = number,
                style = numberStyle.fixedSize(scale * shrink, density.fontScale),
                color = arcColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = label,
                style = SimKeeperTheme.typography.labelSmall.fixedSize(scale, density.fontScale),
                color = SimKeeperTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

/** Text inside the ring scales with the ring, not on its own. */
private fun TextStyle.fixedSize(scale: Float, fontScale: Float): TextStyle =
    copy(fontSize = fontSize.scaled(scale / fontScale), lineHeight = lineHeight.scaled(scale / fontScale))

private fun TextUnit.scaled(factor: Float): TextUnit = this * factor

@PreviewLightDark
@Composable
private fun DaysRingPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            DaysRing(days = 2, progress = 2 / 28f, status = Status.URGENT, size = RingSize.CARD, animateOnAppear = false)
            DaysRing(days = 5, progress = 5 / 28f, status = Status.SOON, size = RingSize.CARD, animateOnAppear = false)
            DaysRing(days = 61, progress = 61 / 84f, status = Status.SAFE, size = RingSize.CARD, animateOnAppear = false)
            DaysRing(days = 0, progress = 0f, status = Status.EXPIRED, size = RingSize.CARD, animateOnAppear = false)
        }
    }
}

@PreviewLightDark
@Composable
private fun DaysRingLargePreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            DaysRing(days = 2, progress = 2 / 28f, status = Status.URGENT, size = RingSize.DETAIL, animateOnAppear = false)
            DaysRing(days = 365, progress = 1f, status = Status.SAFE, size = RingSize.DETAIL, animateOnAppear = false)
            DaysRing(days = 12, progress = 12 / 28f, status = Status.SAFE, size = RingSize.WIDGET, animateOnAppear = false)
        }
    }
}
