package com.simkeeper.app.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.graphics.toArgb
import com.simkeeper.app.ui.theme.ColorPair

private const val FULL_CIRCLE = 360f
private const val TWELVE_O_CLOCK = -90f

/** Glance has no Canvas, so the widget ring is drawn into a bitmap. */
fun ringBitmap(sizePx: Int, strokePx: Float, progress: Float, colors: ColorPair): Bitmap {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val inset = strokePx / 2
    val bounds = RectF(inset, inset, sizePx - inset, sizePx - inset)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokePx
    }
    paint.color = colors.container.toArgb()
    canvas.drawArc(bounds, 0f, FULL_CIRCLE, false, paint)
    val sweep = FULL_CIRCLE * progress.coerceIn(0f, 1f)
    if (sweep > 0f) {
        paint.color = colors.content.toArgb()
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawArc(bounds, TWELVE_O_CLOCK, sweep, false, paint)
    }
    return bitmap
}
