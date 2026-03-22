package com.example.film.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CustomCinema @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var seatId: String = ""
    var isSelectedSeat: Boolean = false
        set(value) {
            field = value
            invalidate() // vẽ lại khi thay đổi
        }
    var isBookedSeat: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = 40f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // màu nền ghế
        paint.color = when {
            isBookedSeat -> Color.GRAY
            isSelectedSeat -> Color.GREEN
            else -> Color.LTGRAY
        }

        // vẽ hình tròn ghế
        val radius = width.coerceAtMost(height) / 2f - 4
        canvas.drawCircle(width/2f, height/2f, radius, paint)

        // vẽ seatId
        canvas.drawText(seatId, width/2f, height/2f + textPaint.textSize/3, textPaint)
    }
}