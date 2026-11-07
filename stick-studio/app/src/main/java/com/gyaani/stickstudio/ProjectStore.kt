package com.gyaani.stickstudio

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * Single source of truth. The UI and the HTTP API both go through this object,
 * so a change made by the AI is instantly visible in the editor and vice versa.
 */
object ProjectStore {

    private val lock = Any()
    private var project: Project = Project.empty()
    private var root: File? = null
    private val listeners = mutableListOf<() -> Unit>()

    fun init(ctx: Context) {
        root = File(ctx.filesDir, "project").apply { mkdirs() }
        File(root, "assets").mkdirs()
        load()
    }

    fun root(): File = root ?: error("ProjectStore not initialised")

    fun assetsDir(): File = File(root(), "assets").apply { mkdirs() }

    fun outputDir(): File = File(root(), "out").apply { mkdirs() }

    fun timelineFile(): File = File(root(), "timeline.json")

    fun current(): Project = synchronized(lock) { project }

    fun snapshotJson(): String = synchronized(lock) { project.prettyJson() }

    /** Replace the whole project from JSON text (used by the API). */
    fun replaceFromJson(text: String): Project {
        val p = Project.fromJson(JSONObject(text))
        synchronized(lock) { project = p }
        persist()
        notifyListeners()
        return p
    }

    /** Mutate under the lock, then persist and tell everyone. */
    fun mutate(block: (Project) -> Unit) {
        synchronized(lock) { block(project) }
        persist()
        notifyListeners()
    }

    fun load() {
        val f = timelineFile()
        if (!f.exists()) {
            synchronized(lock) { project = Project.empty() }
            persist()
            return
        }
        runCatching {
            val p = Project.fromJson(JSONObject(f.readText()))
            synchronized(lock) { project = p }
        }
    }

    fun persist() {
        val f = timelineFile()
        val text = synchronized(lock) { project.prettyJson() }
        runCatching { f.writeText(text) }
    }

    fun onChange(l: () -> Unit) { synchronized(listeners) { listeners.add(l) } }

    fun offChange(l: () -> Unit) { synchronized(listeners) { listeners.remove(l) } }

    fun notifyListeners() {
        val copy = synchronized(listeners) { listeners.toList() }
        copy.forEach { runCatching { it() } }
    }

    // ---- convenience lookups used by the API ----

    fun findScene(id: String): Scene? = current().scenes.firstOrNull { it.id == id }

    fun findLayer(layerId: String): Pair<Scene, Layer>? {
        current().scenes.forEach { s ->
            s.layers.firstOrNull { it.id == layerId }?.let { return s to it }
        }
        return null
    }
}
