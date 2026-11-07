package com.gyaani.stickstudio

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
import java.nio.ByteBuffer

/**
 * Renders the project to an MP4 using only platform codecs (no FFmpeg, no native libs).
 * Video is drawn frame by frame by [FrameComposer]; audio is decoded, re-encoded to AAC
 * and muxed in a second pass so both tracks are known before the muxer starts.
 */
object Renderer {

    @Volatile var state: String = "idle"      // idle | running | done | error
    @Volatile var progress: Float = 0f
    @Volatile var message: String = ""
    @Volatile var lastFile: File? = null
    @Volatile private var running = false

    fun isRunning(): Boolean = running

    fun start(project: Project, outFile: File) {
        if (running) return
        running = true
        state = "running"
        progress = 0f
        message = ""
        Thread {
            try {
                renderBlocking(project, outFile)
                state = "done"
                progress = 1f
                lastFile = outFile
                message = "ok"
            } catch (t: Throwable) {
                state = "error"
                message = t.message ?: t.javaClass.simpleName
            } finally {
                running = false
            }
        }.start()
    }

    private fun renderBlocking(project: Project, outFile: File) {
        val w = project.width - (project.width % 2)
        val h = project.height - (project.height % 2)
        val fps = project.fps
        val duration = project.duration.coerceAtLeast(0.5)
        val totalFrames = (duration * fps).toInt().coerceAtLeast(1)

        outFile.parentFile?.mkdirs()
        if (outFile.exists()) outFile.delete()

        // ---- pass 1: audio (so we know both track formats before the muxer starts) ----
        var tmpAudio: File? = null
        var audioFormat: MediaFormat? = null
        val audioName = project.audio
        if (audioName != null) {
            val src = File(ProjectStore.assetsDir(), audioName)
            if (src.exists()) {
                val tmp = File(outFile.parentFile, "audio_tmp.m4a")
                if (tmp.exists()) tmp.delete()
                runCatching {
                    encodeAudio(src, tmp)
                    audioFormat = readAudioFormat(tmp)
                    tmpAudio = tmp
                }
            }
        }
        message = "video"
        progress = 0.02f

        // ---- pass 2: video ----
        val muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var videoTrack = -1
        var audioTrack = -1
        var muxStarted = false

        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, w, h).apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
            )
            setInteger(MediaFormat.KEY_BIT_RATE, (w * h * 4).coerceIn(1_500_000, 12_000_000))
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }
        val enc = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        enc.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        enc.start()

        val composer = FrameComposer()
        val info = MediaCodec.BufferInfo()

        try {
            for (frame in 0 until totalFrames) {
                val t = frame.toDouble() / fps
                val bmp = composer.compose(project, t, w, h)
                val pts = (frame.toLong() * 1_000_000L) / fps

                var fed = false
                while (!fed) {
                    val inIdx = enc.dequeueInputBuffer(10_000)
                    if (inIdx >= 0) {
                        val image = enc.getInputImage(inIdx)
                        if (image != null) {
                            fillImage(image, bmp)
                        } else {
                            val buf = enc.getInputBuffer(inIdx)!!
                            buf.clear()
                            putI420(buf, bmp)
                        }
                        enc.queueInputBuffer(inIdx, 0, w * h * 3 / 2, pts, 0)
                        fed = true
                    }
                    drainVideo(enc, muxer, info, { videoTrack }, { muxStarted }, {
                        // on format changed
                    }).let { st ->
                        if (!muxStarted && st != null) {
                            videoTrack = muxer.addTrack(st)
                            audioFormat?.let { af -> runCatching { audioTrack = muxer.addTrack(af) } }
                            muxer.start()
                            muxStarted = true
                        }
                    }
                }
                drainVideo(enc, muxer, info, { videoTrack }, { muxStarted }, {})
                if (frame % 30 == 0) progress = 0.05f + 0.90f * (frame.toFloat() / totalFrames)
            }

            // flush encoder
            val inIdx = enc.dequeueInputBuffer(50_000)
            if (inIdx >= 0) {
                enc.queueInputBuffer(inIdx, 0, 0, (totalFrames.toLong() * 1_000_000L) / fps,
                    MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            }
            var eos = false
            while (!eos) {
                val r = drainVideo(enc, muxer, info, { videoTrack }, { muxStarted }, {})
                if (r == null) {
                    // format changed handled inside
                }
                val oi = enc.dequeueOutputBuffer(info, 10_000)
                if (oi == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if (!muxStarted) {
                        videoTrack = muxer.addTrack(enc.outputFormat)
                        audioFormat?.let { af -> runCatching { audioTrack = muxer.addTrack(af) } }
                        muxer.start()
                        muxStarted = true
                    }
                } else if (oi >= 0) {
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) eos = true
                    if (info.size > 0 && muxStarted) {
                        val buf = enc.getOutputBuffer(oi)!!
                        buf.position(info.offset)
                        buf.limit(info.offset + info.size)
                        if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0) {
                            muxer.writeSampleData(videoTrack, buf, info)
                        }
                    }
                    enc.releaseOutputBuffer(oi, false)
                }
            }

            // ---- copy audio samples in ----
            if (tmpAudio != null && audioTrack >= 0) {
                copyAudio(tmpAudio!!, muxer, audioTrack)
            }
        } finally {
            runCatching { enc.stop() }
            runCatching { enc.release() }
            runCatching { muxer.stop() }
            runCatching { muxer.release() }
            tmpAudio?.delete()
        }
        progress = 1f
    }

    private fun drainVideo(
        enc: MediaCodec,
        muxer: MediaMuxer,
        info: MediaCodec.BufferInfo,
        track: () -> Int,
        started: () -> Boolean,
        onFormat: () -> Unit
    ): MediaFormat? {
        while (true) {
            val oi = enc.dequeueOutputBuffer(info, 0)
            if (oi == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) return enc.outputFormat
            if (oi == MediaCodec.INFO_TRY_AGAIN_LATER) return null
            if (oi < 0) continue
            if (info.size > 0 && started()) {
                val buf = enc.getOutputBuffer(oi)!!
                buf.position(info.offset)
                buf.limit(info.offset + info.size)
                if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0) {
                    muxer.writeSampleData(track(), buf, info)
                }
            }
            enc.releaseOutputBuffer(oi, false)
        }
    }

    private fun fillImage(image: android.media.Image, bmp: android.graphics.Bitmap) {
        val w = image.width
        val h = image.height
        val pixels = IntArray(w * h)
        bmp.getPixels(pixels, 0, w, 0, 0, w, h)
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]
        val yBuf = yPlane.buffer
        val uBuf = uPlane.buffer
        val vBuf = vPlane.buffer
        for (row in 0 until h) {
            for (col in 0 until w) {
                val c = pixels[row * w + col]
                val r = (c shr 16) and 0xff
                val g = (c shr 8) and 0xff
                val b = c and 0xff
                val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                yBuf.put(row * yPlane.rowStride + col * yPlane.pixelStride, y.toByte())
                if (row % 2 == 0 && col % 2 == 0) {
                    val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                    val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128
                    uBuf.put((row / 2) * uPlane.rowStride + (col / 2) * uPlane.pixelStride, u.toByte())
                    vBuf.put((row / 2) * vPlane.rowStride + (col / 2) * vPlane.pixelStride, v.toByte())
                }
            }
        }
    }

    private fun putI420(buf: ByteBuffer, bmp: android.graphics.Bitmap) {
        val w = bmp.width
        val h = bmp.height
        val pixels = IntArray(w * h)
        bmp.getPixels(pixels, 0, w, 0, 0, w, h)
        val ySize = w * h
        val out = ByteArray(ySize * 3 / 2)
        var y = 0
        var u = ySize
        var v = ySize + ySize / 4
        for (row in 0 until h) {
            for (col in 0 until w) {
                val c = pixels[row * w + col]
                val r = (c shr 16) and 0xff
                val g = (c shr 8) and 0xff
                val b = c and 0xff
                out[y++] = (((66 * r + 129 * g + 25 * b + 128) shr 8) + 16).toByte()
                if (row % 2 == 0 && col % 2 == 0) {
                    out[u++] = (((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128).toByte()
                    out[v++] = (((112 * r - 94 * g - 18 * b + 128) shr 8) + 128).toByte()
                }
            }
        }
        buf.clear()
        buf.put(out)
    }

    // ---------- audio ----------

    private fun readAudioFormat(f: File): MediaFormat? {
        val ex = MediaExtractor()
        ex.setDataSource(f.absolutePath)
        val fmt = if (ex.trackCount > 0) ex.getTrackFormat(0) else null
        ex.release()
        return fmt
    }

    private fun encodeAudio(src: File, out: File) {
        val ex = MediaExtractor()
        ex.setDataSource(src.absolutePath)
        var idx = -1
        for (i in 0 until ex.trackCount) {
            val f = ex.getTrackFormat(i)
            if (f.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) { idx = i; break }
        }
        if (idx < 0) { ex.release(); return }
        ex.selectTrack(idx)
        val inFmt = ex.getTrackFormat(idx)
        val mime = inFmt.getString(MediaFormat.KEY_MIME)!!

        val decoder = MediaCodec.createDecoderByType(mime)
        decoder.configure(inFmt, null, null, 0)
        decoder.start()

        val sampleRate = if (inFmt.containsKey(MediaFormat.KEY_SAMPLE_RATE))
            inFmt.getInteger(MediaFormat.KEY_SAMPLE_RATE) else 44100
        val channels = if (inFmt.containsKey(MediaFormat.KEY_CHANNEL_COUNT))
            inFmt.getInteger(MediaFormat.KEY_CHANNEL_COUNT) else 2

        val encFmt = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channels).apply {
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_BIT_RATE, 128000)
        }
        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        encoder.configure(encFmt, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()

        val mux = MediaMuxer(out.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var track = -1
        var started = false
        var inputDone = false
        var outputDone = false
        val info = MediaCodec.BufferInfo()

        try {
            while (!outputDone) {
                if (!inputDone) {
                    val di = decoder.dequeueInputBuffer(5_000)
                    if (di >= 0) {
                        val buf = decoder.getInputBuffer(di)!!
                        val size = ex.readSampleData(buf, 0)
                        if (size < 0) {
                            decoder.queueInputBuffer(di, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            decoder.queueInputBuffer(di, 0, size, ex.sampleTime, 0)
                            ex.advance()
                        }
                    }
                }

                var doIdx = decoder.dequeueOutputBuffer(info, 5_000)
                while (doIdx >= 0) {
                    val dbuf = decoder.getOutputBuffer(doIdx)
                    if (info.size > 0 && dbuf != null) {
                        dbuf.position(info.offset)
                        dbuf.limit(info.offset + info.size)
                        val ei = encoder.dequeueInputBuffer(5_000)
                        if (ei >= 0) {
                            val eb = encoder.getInputBuffer(ei)!!
                            eb.clear()
                            eb.put(dbuf)
                            encoder.queueInputBuffer(ei, 0, info.size, info.presentationTimeUs, 0)
                        }
                    }
                    decoder.releaseOutputBuffer(doIdx, false)
                    doIdx = decoder.dequeueOutputBuffer(info, 0)
                }

                var eo = encoder.dequeueOutputBuffer(info, 5_000)
                while (true) {
                    if (eo == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (!started) {
                            track = mux.addTrack(encoder.outputFormat)
                            mux.start()
                            started = true
                        }
                    } else if (eo >= 0) {
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
                        if (info.size > 0 && started) {
                            val eb = encoder.getOutputBuffer(eo)!!
                            eb.position(info.offset)
                            eb.limit(info.offset + info.size)
                            if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0) {
                                mux.writeSampleData(track, eb, info)
                            }
                        }
                        encoder.releaseOutputBuffer(eo, false)
                    } else break
                    eo = encoder.dequeueOutputBuffer(info, 0)
                }
            }
        } finally {
            runCatching { decoder.stop() }; runCatching { decoder.release() }
            runCatching { encoder.stop() }; runCatching { encoder.release() }
            runCatching { mux.stop() }; runCatching { mux.release() }
            runCatching { ex.release() }
        }
    }

    private fun copyAudio(src: File, muxer: MediaMuxer, track: Int) {
        val ex = MediaExtractor()
        ex.setDataSource(src.absolutePath)
        if (ex.trackCount == 0) { ex.release(); return }
        ex.selectTrack(0)
        val buf = ByteBuffer.allocateDirect(256 * 1024)
        val info = MediaCodec.BufferInfo()
        try {
            while (true) {
                val size = ex.readSampleData(buf, 0)
                if (size < 0) break
                info.offset = 0
                info.size = size
                info.presentationTimeUs = ex.sampleTime
                info.flags = 0
                muxer.writeSampleData(track, buf, info)
                ex.advance()
            }
        } finally {
            ex.release()
        }
    }
}
