package com.gyaani.stickstudio

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

fun newId(): String = UUID.randomUUID().toString().replace("-", "").take(8)

/**
 * One thing on screen: a PNG or a line of text.
 * All geometry is a fraction of the canvas, so the project survives a resolution change.
 */
data class Layer(
    var id: String = newId(),
    var type: String = "image",          // "image" | "text"
    var asset: String? = null,           // filename inside <files>/assets
    var text: String? = null,
    var x: Float = 0.5f,                 // centre, fraction of width
    var y: Float = 0.6f,                 // centre, fraction of height
    var h: Float = 0.40f,                // height, fraction of canvas height
    var z: Int = 2,
    var start: Double = 0.0,
    var end: Double = 5.0,
    var anim: String = "fade",           // cut | fade | pop | slide_up | slide_left | typeon
    var animDur: Double = 0.30,
    var cps: Int = 24,                   // characters per second for typeon
    var color: String = "#1A1A1A"
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("type", type)
        if (asset != null) put("asset", asset) else put("asset", JSONObject.NULL)
        if (text != null) put("text", text) else put("text", JSONObject.NULL)
        put("x", x.toDouble()); put("y", y.toDouble()); put("h", h.toDouble())
        put("z", z)
        put("start", start); put("end", end)
        put("anim", anim); put("animDur", animDur)
        put("cps", cps); put("color", color)
    }

    companion object {
        fun fromJson(o: JSONObject) = Layer(
            id = o.optString("id", newId()),
            type = o.optString("type", "image"),
            asset = if (o.isNull("asset")) null else o.optString("asset", null),
            text = if (o.isNull("text")) null else o.optString("text", null),
            x = o.optDouble("x", 0.5).toFloat(),
            y = o.optDouble("y", 0.6).toFloat(),
            h = o.optDouble("h", 0.4).toFloat(),
            z = o.optInt("z", 2),
            start = o.optDouble("start", 0.0),
            end = o.optDouble("end", 5.0),
            anim = o.optString("anim", "fade"),
            animDur = o.optDouble("animDur", 0.3),
            cps = o.optInt("cps", 24),
            color = o.optString("color", "#1A1A1A")
        )
    }
}

data class Scene(
    var id: String = newId(),
    var name: String = "scene",
    var start: Double = 0.0,
    var end: Double = 5.0,
    var layers: MutableList<Layer> = mutableListOf()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id); put("name", name)
        put("start", start); put("end", end)
        put("layers", JSONArray().also { arr -> layers.forEach { arr.put(it.toJson()) } })
    }

    companion object {
        fun fromJson(o: JSONObject) = Scene(
            id = o.optString("id", newId()),
            name = o.optString("name", "scene"),
            start = o.optDouble("start", 0.0),
            end = o.optDouble("end", 5.0),
            layers = mutableListOf<Layer>().also { list ->
                o.optJSONArray("layers")?.let { arr ->
                    for (i in 0 until arr.length()) list.add(Layer.fromJson(arr.getJSONObject(i)))
                }
            }
        )
    }
}

data class Project(
    var title: String = "untitled",
    var width: Int = 1280,
    var height: Int = 720,
    var fps: Int = 30,
    var duration: Double = 30.0,
    var background: String? = null,
    var audio: String? = null,
    var font: String? = null,
    var scenes: MutableList<Scene> = mutableListOf()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("title", title)
        put("width", width); put("height", height); put("fps", fps)
        put("duration", duration)
        put("background", background ?: JSONObject.NULL)
        put("audio", audio ?: JSONObject.NULL)
        put("font", font ?: JSONObject.NULL)
        put("scenes", JSONArray().also { arr -> scenes.forEach { arr.put(it.toJson()) } })
    }

    fun prettyJson(): String = toJson().toString(2)

    companion object {
        fun fromJson(o: JSONObject) = Project(
            title = o.optString("title", "untitled"),
            width = o.optInt("width", 1280),
            height = o.optInt("height", 720),
            fps = o.optInt("fps", 30),
            duration = o.optDouble("duration", 30.0),
            background = if (o.isNull("background")) null else o.optString("background", null),
            audio = if (o.isNull("audio")) null else o.optString("audio", null),
            font = if (o.isNull("font")) null else o.optString("font", null),
            scenes = mutableListOf<Scene>().also { list ->
                o.optJSONArray("scenes")?.let { arr ->
                    for (i in 0 until arr.length()) list.add(Scene.fromJson(arr.getJSONObject(i)))
                }
            }
        )

        fun empty(): Project = Project(
            title = "new project",
            duration = 20.0,
            scenes = mutableListOf(Scene(name = "scene 1", start = 0.0, end = 5.0))
        )
    }
}
