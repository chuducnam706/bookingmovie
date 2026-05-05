package com.example.film.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CustomScreenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        strokeCap = Paint.Cap.ROUND
        // Neon pink color
        color = Color.parseColor("#FF0099")
        // Add shadow for neon effect
        setShadowLayer(20f, 0f, 0f, Color.parseColor("#FF0099"))
    }

    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        path.reset()
        path.moveTo(w * 0.1f, h * 0.8f)
        path.quadTo(w * 0.5f, -h * 0.2f, w * 0.9f, h * 0.8f)

        canvas.drawPath(path, paint)
    }
}
