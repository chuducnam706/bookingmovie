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
    private val seatDrawable = context.getDrawable(com.example.film.R.drawable.ic_cinema_seat)?.mutate()

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = 18f
        alpha = 255
        isFakeBoldText = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val tintColor = when {
            isBookedSeat -> Color.parseColor("#C2185B") // Dark Pink (Reserved)
            isSelectedSeat -> Color.parseColor("#5EEAD4") // Mint Green (Selected)
            else -> Color.WHITE // Available
        }

        seatDrawable?.let {
            val padding = (width * 0.1f).toInt()
            it.setTint(tintColor)
            it.setBounds(padding, padding, width - padding, height - padding)
            it.draw(canvas)
        }

        // Draw seat ID (e.g., A1, B2)
        if (seatId.isNotEmpty() && seatId != "_") {
            canvas.drawText(seatId, width / 2f, height / 2f + textPaint.textSize / 3f, textPaint)
        }
    }
}