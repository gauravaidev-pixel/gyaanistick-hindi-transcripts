package com.gyaani.stickstudio

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import java.io.File

/** Renders a line of text to a transparent bitmap, plus the typewriter crop helper. */
object TextPainter {

    private val cache = HashMap<String, Bitmap>()
    private var fontFile: String? = null
    private var typeface: Typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

    fun setFont(name: String?) {
        if (name == fontFile) return
        fontFile = name
        typeface = if (name == null) {
            Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        } else {
            val f = File(ProjectStore.assetsDir(), name)
            if (f.exists()) runCatching { Typeface.createFromFile(f) }
                .getOrDefault(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD))
            else Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        cache.clear()
    }

    fun bitmap(text: String, px: Int, colorHex: String): Bitmap {
        val key = "$text|$px|$colorHex|$fontFile"
        cache[key]?.let { return it }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = this@TextPainter.typeface
            textSize = px.toFloat()
            color = parseColor(colorHex)
        }
        val fm = paint.fontMetrics
        val w = (paint.measureText(text).toInt() + px / 4).coerceAtLeast(1)
        val h = ((fm.descent - fm.ascent).toInt() + px / 4).coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        Canvas(bmp).drawText(text, px / 8f, -fm.ascent + px / 8f, paint)
        cache[key] = bmp
        return bmp
    }

    fun parseColor(hex: String): Int = runCatching { Color.parseColor(hex) }.getOrDefault(Color.BLACK)
}
