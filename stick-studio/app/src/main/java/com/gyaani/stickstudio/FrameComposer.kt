package com.gyaani.stickstudio

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import java.io.File

/**
 * Draws one frame of the project at time [t] into a bitmap.
 * Used by the live preview and by the renderer, so what you see is what you get.
 */
class FrameComposer {

    private val bmpCache = HashMap<String, Bitmap>()
    private var bgCache: Pair<String, Bitmap>? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    fun clear() {
        bmpCache.clear()
        bgCache = null
        TextPainter.setFont(null)
    }

    private fun assetBitmap(name: String?): Bitmap? {
        if (name == null) return null
        bmpCache[name]?.let { return it }
        val f = File(ProjectStore.assetsDir(), name)
        if (!f.exists()) return null
        val b = BitmapFactory.decodeFile(f.absolutePath) ?: return null
        bmpCache[name] = b
        return b
    }

    /** Background scaled to cover the canvas without stretching. */
    private fun background(w: Int, h: Int, name: String?): Bitmap? {
        if (name == null) return null
        val cached = bgCache
        if (cached != null && cached.first == name && cached.second.width == w && cached.second.height == h) {
            return cached.second
        }
        val src = assetBitmap(name) ?: return null
        val scale = maxOf(w.toFloat() / src.width, h.toFloat() / src.height)
        val dw = (src.width * scale).toInt()
        val dh = (src.height * scale).toInt()
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(out)
        c.drawColor(Color.WHITE)
        val left = (w - dw) / 2
        val top = (h - dh) / 2
        c.drawBitmap(src, Rect(0, 0, src.width, src.height), Rect(left, top, left + dw, top + dh), paint)
        bgCache = name to out
        return out
    }

    fun compose(project: Project, t: Double, w: Int, h: Int): Bitmap {
        TextPainter.setFont(project.font)
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.parseColor("#F3EBDD"))

        background(w, h, project.background)?.let { canvas.drawBitmap(it, 0f, 0f, paint) }

        val active = project.scenes.filter { t >= it.start && t < it.end }
        val layers = active.flatMap { it.layers }.sortedBy { it.z }

        for (layer in layers) {
            if (t < layer.start || t > layer.end) continue
            drawLayer(canvas, layer, t, w, h)
        }
        return out
    }

    private fun drawLayer(canvas: Canvas, layer: Layer, t: Double, w: Int, h: Int) {
        val enter = (t - layer.start).coerceAtLeast(0.0)
        val leave = (layer.end - t).coerceAtLeast(0.0)
        val d = layer.animDur.coerceAtLeast(0.01)

        var alpha = 1f
        var scale = 1f
        var dy = 0f

        when (layer.anim) {
            "cut", "typeon" -> {}
            "fade" -> {
                if (enter < d) alpha = (enter / d).toFloat()
                if (leave < d) alpha = minOf(alpha, (leave / d).toFloat())
            }
            "pop" -> {
                if (enter < d) {
                    val p = (enter / d).toFloat()
                    alpha = p
                    scale = 0.85f + 0.15f * p
                }
                if (leave < d) alpha = minOf(alpha, (leave / d).toFloat())
            }
            "slide_up" -> {
                if (enter < d) {
                    val p = (enter / d).toFloat()
                    alpha = p
                    dy = (1f - p) * 0.18f * h
                }
                if (leave < d) alpha = minOf(alpha, (leave / d).toFloat())
            }
            "slide_left" -> {
                if (enter < d) {
                    val p = (enter / d).toFloat()
                    alpha = p
                    dy = 0f
                }
            }
        }
        if (alpha <= 0.001f) return

        if (layer.type == "text") {
            val text = layer.text ?: return
            if (text.isBlank()) return
            val px = (h * layer.h).toInt().coerceIn(8, 400)
            val bmp = TextPainter.bitmap(text, px, layer.color)
            var visible = bmp.width
            if (layer.anim == "typeon") {
                val chars = text.length.coerceAtLeast(1)
                val p = (((t - layer.start) * layer.cps) / chars).coerceIn(0.0, 1.0)
                visible = (bmp.width * p).toInt().coerceAtLeast(1)
            }
            val dw = (visible * scale).toInt().coerceAtLeast(1)
            val dh = (bmp.height * scale).toInt().coerceAtLeast(1)
            val cx = (w * layer.x).toFloat()
            val cy = (h * layer.y).toFloat() + dy
            val dst = RectF(cx - dw / 2f, cy - dh / 2f, cx + dw / 2f, cy + dh / 2f)
            paint.alpha = (alpha * 255).toInt().coerceIn(0, 255)
            canvas.drawBitmap(bmp, Rect(0, 0, visible, bmp.height), dst, paint)
            paint.alpha = 255
        } else {
            val bmp = assetBitmap(layer.asset) ?: return
            val targetH = h * layer.h * scale
            val ratio = bmp.width.toFloat() / bmp.height.toFloat()
            val targetW = targetH * ratio
            val cx = (w * layer.x).toFloat()
            val cy = (h * layer.y).toFloat() + dy
            val dst = RectF(cx - targetW / 2f, cy - targetH / 2f, cx + targetW / 2f, cy + targetH / 2f)
            paint.alpha = (alpha * 255).toInt().coerceIn(0, 255)
            canvas.drawBitmap(bmp, null, dst, paint)
            paint.alpha = 255
        }
    }
}
