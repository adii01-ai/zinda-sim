package com.simkeeper.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.simkeeper.app.ui.theme.Motion
import com.simkeeper.app.ui.theme.SimKeeperTheme
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val GRAVITY = 1.6f
private const val SPEED_MIN = 0.35f
private const val SPEED_MAX = 0.8f
private const val PIECE_FRACTION = 0.018f
private const val SPIN = 720f
private const val FULL_TURN_RAD = 2 * Math.PI
private const val SEED = 19

private data class Piece(val angle: Double, val speed: Float, val color: Color, val spin: Float, val wide: Boolean)

/** Light burst from [originY] (fraction of height): ≤ 40 pieces, ≤ 1.2 s. Nothing with reduced motion. */
@Composable
fun Confetti(modifier: Modifier = Modifier, originY: Float = 0.3f) {
    if (SimKeeperTheme.reduceMotion) return
    val status = SimKeeperTheme.status
    val palette = listOf(
        SimKeeperTheme.colors.primary, status.safe.content, status.soon.content,
        status.jio.content, status.vi.content, status.bsnl.content,
    )
    val pieces = remember {
        val random = Random(SEED)
        List(Motion.CONFETTI_MAX_PARTICLES) {
            Piece(
                angle = random.nextDouble() * FULL_TURN_RAD,
                speed = SPEED_MIN + random.nextFloat() * (SPEED_MAX - SPEED_MIN),
                color = palette[it % palette.size],
                spin = (random.nextFloat() - 0.5f) * SPIN,
                wide = random.nextBoolean(),
            )
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(Motion.CONFETTI_MS, easing = LinearEasing)) }
    if (progress.value >= 1f) return
    Canvas(modifier = modifier.clearAndSetSemantics { }) {
        val t = progress.value
        val origin = Offset(size.width / 2, size.height * originY)
        val unit = size.minDimension
        val piece = unit * PIECE_FRACTION
        val alpha = 1f - t * t
        pieces.forEach { p ->
            val distance = p.speed * unit * t
            val x = origin.x + (cos(p.angle) * distance).toFloat()
            val y = origin.y + (sin(p.angle) * distance).toFloat() + GRAVITY * unit * t * t / 2
            val pieceSize = if (p.wide) Size(piece * 2, piece) else Size(piece, piece * 2)
            rotate(p.spin * t, pivot = Offset(x, y)) {
                drawRect(p.color.copy(alpha = alpha), topLeft = Offset(x - pieceSize.width / 2, y - pieceSize.height / 2), size = pieceSize)
            }
        }
    }
}
