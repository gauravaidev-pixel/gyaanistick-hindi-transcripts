package com.gyaani.stickstudio

import android.graphics.Bitmap
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder

/**
 * The app's own HTTP server. It is started by [ServerService] so it keeps running
 * when the app is in the background, and it is the surface an AI assistant talks to.
 *
 * Every route reads and writes through [ProjectStore], so API edits and manual edits
 * in the UI always operate on the same project.
 */
class ApiServer(private val port: Int, private val bindLan: Boolean) {

    @Volatile var running = false
        private set
    private var server: ServerSocket? = null

    fun start() {
        if (running) return
        val addr = if (bindLan) InetAddress.getByName("0.0.0.0") else InetAddress.getByName("127.0.0.1")
        server = ServerSocket(port, 16, addr)
        running = true
        Thread {
            while (running) {
                val client = runCatching { server?.accept() }.getOrNull() ?: continue
                Thread { runCatching { handle(client) }.also { client.close() } }.start()
            }
        }.start()
    }

    fun stop() {
        running = false
        runCatching { server?.close() }
        server = null
    }

    // ---------------------------------------------------------------- request

    private class Req(
        val method: String,
        val path: String,
        val query: Map<String, String>,
        val headers: Map<String, String>,
        val body: ByteArray
    )

    private class Res(val status: Int, val contentType: String, val bytes: ByteArray)

    private fun handle(client: Socket) {
        val input = BufferedInputStream(client.getInputStream())
        val output = BufferedOutputStream(client.getOutputStream())
        val head = readHead(input) ?: return
        val lines = head.split("\r\n")
        if (lines.isEmpty()) return
        val parts = lines[0].split(" ")
        if (parts.size < 2) return
        val method = parts[0].uppercase()
        val target = parts[1]
        val headers = HashMap<String, String>()
        for (i in 1 until lines.size) {
            val l = lines[i]
            val idx = l.indexOf(':')
            if (idx > 0) headers[l.substring(0, idx).trim().lowercase()] = l.substring(idx + 1).trim()
        }
        val qIdx = target.indexOf('?')
        val path = if (qIdx >= 0) target.substring(0, qIdx) else target
        val query = HashMap<String, String>()
        if (qIdx >= 0) {
            target.substring(qIdx + 1).split("&").forEach { kv ->
                val p = kv.split("=", limit = 2)
                if (p.isNotEmpty() && p[0].isNotEmpty()) {
                    query[URLDecoder.decode(p[0], "UTF-8")] =
                        URLDecoder.decode(p.getOrElse(1) { "" }, "UTF-8")
                }
            }
        }
        val len = headers["content-length"]?.toIntOrNull() ?: 0
        val body = ByteArray(len)
        var read = 0
        while (read < len) {
            val r = input.read(body, read, len - read)
            if (r < 0) break
            read += r
        }
        val res = try {
            route(Req(method, path, query, headers, body))
        } catch (t: Throwable) {
            json(500, JSONObject().put("error", t.message ?: "error"))
        }
        writeResponse(output, res)
        output.flush()
    }

    private fun readHead(input: InputStream): String? {
        val sb = StringBuilder()
        var last4 = 0
        while (true) {
            val b = input.read()
            if (b < 0) return if (sb.isEmpty()) null else sb.toString()
            sb.append(b.toChar())
            last4 = (last4 shl 8) or b
            if (last4 == 0x0D0A0D0A) return sb.toString()
            if (sb.length > 65536) return null
        }
    }

    private fun writeResponse(out: OutputStream, res: Res) {
        val header = buildString {
            append("HTTP/1.1 ${res.status} ${statusText(res.status)}\r\n")
            append("Content-Type: ${res.contentType}\r\n")
            append("Content-Length: ${res.bytes.size}\r\n")
            append("Access-Control-Allow-Origin: *\r\n")
            append("Access-Control-Allow-Headers: *\r\n")
            append("Connection: close\r\n\r\n")
        }
        out.write(header.toByteArray())
        out.write(res.bytes)
    }

    private fun statusText(s: Int) = when (s) {
        200 -> "OK"; 201 -> "Created"; 204 -> "No Content"
        400 -> "Bad Request"; 401 -> "Unauthorized"; 404 -> "Not Found"
        405 -> "Method Not Allowed"; 500 -> "Internal Server Error"
        else -> "OK"
    }

    private fun json(status: Int, o: JSONObject): Res =
        Res(status, "application/json", o.toString().toByteArray())

    private fun ok() = JSONObject().put("ok", true)

    private fun authorized(req: Req): Boolean {
        val supplied = req.headers["x-token"] ?: req.query["token"]
        return supplied == Prefs.token
    }

    // ---------------------------------------------------------------- routing

    private fun route(req: Req): Res {
        if (req.path == "/health") {
            return json(200, JSONObject()
                .put("ok", true)
                .put("app", "StickStudio")
                .put("port", port)
                .put("state", Renderer.state)
                .put("project", ProjectStore.current().title))
        }
        if (!authorized(req)) {
            return json(401, JSONObject().put("error", "missing or wrong token; send header X-Token"))
        }

        val p = req.path

        // ---- discovery: the AI reads this first ----
        if (req.method == "GET" && (p == "/api" || p == "/api/schema" || p == "/")) {
            return json(200, schema())
        }

        if (req.method == "GET" && p == "/api/project") {
            return Res(200, "application/json", ProjectStore.snapshotJson().toByteArray())
        }
        if (req.method == "PUT" && p == "/api/project") {
            val body = String(req.body)
            if (!body.trimStart().startsWith("{")) {
                return json(400, JSONObject().put("error", "body must be the project JSON object"))
            }
            val np = ProjectStore.replaceFromJson(body)
            return json(200, JSONObject().put("ok", true).put("scenes", np.scenes.size))
        }

        if (req.method == "GET" && p == "/api/assets") {
            val files = ProjectStore.assetsDir().listFiles()?.sortedBy { it.name } ?: emptyList()
            val arr = JSONArray()
            files.forEach { f ->
                arr.put(JSONObject().put("name", f.name).put("size", f.length()))
            }
            return json(200, JSONObject().put("assets", arr))
        }

        if (req.method == "POST" && p == "/api/assets") {
            val name = req.query["name"] ?: return json(400, JSONObject().put("error", "name required"))
            val f = File(ProjectStore.assetsDir(), name.replace("/", "_"))
            f.writeBytes(req.body)
            return json(201, JSONObject().put("ok", true).put("name", f.name).put("size", f.length()))
        }

        if (req.method == "DELETE" && p == "/api/assets") {
            val name = req.query["name"] ?: return json(400, JSONObject().put("error", "name required"))
            val f = File(ProjectStore.assetsDir(), name)
            return json(200, JSONObject().put("ok", f.delete()))
        }

        if (req.method == "GET" && p == "/api/frame") {
            val t = req.query["t"]?.toDoubleOrNull() ?: 0.0
            val w = (req.query["w"]?.toIntOrNull() ?: 640).coerceIn(160, 1920)
            val proj = ProjectStore.current()
            val h = (w * proj.height / proj.width.toFloat()).toInt().coerceAtLeast(2)
            val bmp: Bitmap = FrameComposer().compose(proj, t, w, h)
            val out = java.io.ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            return Res(200, "image/png", out.toByteArray())
        }

        if (req.method == "POST" && p == "/api/render") {
            if (Renderer.isRunning()) {
                return json(409, JSONObject().put("error", "render already running")
                    .put("progress", Renderer.progress))
            }
            val proj = ProjectStore.current()
            val out = File(ProjectStore.outputDir(), "final.mp4")
            Renderer.start(proj, out)
            return json(202, JSONObject().put("ok", true).put("state", Renderer.state))
        }

        if (req.method == "GET" && p == "/api/render") {
            return json(200, JSONObject()
                .put("state", Renderer.state)
                .put("progress", Renderer.progress)
                .put("message", Renderer.message)
                .put("file", Renderer.lastFile?.name ?: JSONObject.NULL))
        }

        if (req.method == "GET" && p == "/api/file") {
            val name = req.query["name"] ?: "final.mp4"
            val f = File(ProjectStore.outputDir(), name)
            if (!f.exists()) return json(404, JSONObject().put("error", "not found"))
            return Res(200, "video/mp4", f.readBytes())
        }

        // ---- scenes ----
        if (req.method == "POST" && p == "/api/scenes") {
            val o = bodyJson(req) ?: JSONObject()
            val s = Scene(
                name = o.optString("name", "scene"),
                start = o.optDouble("start", 0.0),
                end = o.optDouble("end", 5.0)
            )
            ProjectStore.mutate { it.scenes.add(s) }
            return json(201, s.toJson())
        }
        if (p.startsWith("/api/scenes/")) {
            val id = p.removePrefix("/api/scenes/")
            if (req.method == "DELETE") {
                ProjectStore.mutate { proj -> proj.scenes.removeAll { it.id == id } }
                return json(200, ok())
            }
            if (req.method == "PATCH" || req.method == "PUT") {
                val o = bodyJson(req) ?: return json(400, JSONObject().put("error", "json body required"))
                val scene = ProjectStore.findScene(id)
                    ?: return json(404, JSONObject().put("error", "no scene $id"))
                ProjectStore.mutate {
                    if (o.has("name")) scene.name = o.getString("name")
                    if (o.has("start")) scene.start = o.getDouble("start")
                    if (o.has("end")) scene.end = o.getDouble("end")
                }
                return json(200, scene.toJson())
            }
        }

        // ---- layers ----
        if (req.method == "POST" && p == "/api/layers") {
            val sceneId = req.query["scene"]
                ?: return json(400, JSONObject().put("error", "scene=ID required"))
            val scene = ProjectStore.findScene(sceneId)
                ?: return json(404, JSONObject().put("error", "no scene $sceneId"))
            val o = bodyJson(req) ?: JSONObject()
            val layer = Layer(
                type = o.optString("type", "image"),
                asset = if (o.has("asset") && !o.isNull("asset")) o.getString("asset") else null,
                text = if (o.has("text") && !o.isNull("text")) o.getString("text") else null,
                x = o.optDouble("x", 0.5).toFloat(),
                y = o.optDouble("y", 0.6).toFloat(),
                h = o.optDouble("h", 0.4).toFloat(),
                z = o.optInt("z", 2),
                start = o.optDouble("start", scene.start),
                end = o.optDouble("end", scene.end),
                anim = o.optString("anim", "fade"),
                animDur = o.optDouble("animDur", 0.3),
                cps = o.optInt("cps", 24),
                color = o.optString("color", "#1A1A1A")
            )
            ProjectStore.mutate { scene.layers.add(layer) }
            return json(201, layer.toJson())
        }
        if (p.startsWith("/api/layers/")) {
            val id = p.removePrefix("/api/layers/")
            if (req.method == "DELETE") {
                ProjectStore.mutate { proj ->
                    proj.scenes.forEach { s -> s.layers.removeAll { it.id == id } }
                }
                return json(200, ok())
            }
            if (req.method == "PATCH" || req.method == "PUT") {
                val o = bodyJson(req) ?: return json(400, JSONObject().put("error", "json body required"))
                val found = ProjectStore.findLayer(id)
                    ?: return json(404, JSONObject().put("error", "no layer $id"))
                val layer = found.second
                ProjectStore.mutate {
                    if (o.has("asset")) layer.asset = if (o.isNull("asset")) null else o.getString("asset")
                    if (o.has("text")) layer.text = if (o.isNull("text")) null else o.getString("text")
                    if (o.has("x")) layer.x = o.getDouble("x").toFloat()
                    if (o.has("y")) layer.y = o.getDouble("y").toFloat()
                    if (o.has("h")) layer.h = o.getDouble("h").toFloat()
                    if (o.has("z")) layer.z = o.getInt("z")
                    if (o.has("start")) layer.start = o.getDouble("start")
                    if (o.has("end")) layer.end = o.getDouble("end")
                    if (o.has("anim")) layer.anim = o.getString("anim")
                    if (o.has("animDur")) layer.animDur = o.getDouble("animDur")
                    if (o.has("cps")) layer.cps = o.getInt("cps")
                    if (o.has("color")) layer.color = o.getString("color")
                    if (o.has("type")) layer.type = o.getString("type")
                }
                return json(200, layer.toJson())
            }
        }

        return json(404, JSONObject().put("error", "no route $p"))
    }

    private fun bodyJson(req: Req): JSONObject? = runCatching {
        val s = String(req.body).trim()
        if (s.startsWith("{")) JSONObject(s) else null
    }.getOrNull()

    /** Self-description so an assistant can discover the API without external docs. */
    private fun schema(): JSONObject = JSONObject().apply {
        put("app", "StickStudio")
        put("note", "Local video editor. Send header X-Token on every call except /health.")
        put("token_header", "X-Token")
        put("endpoints", JSONArray().apply {
            put("GET /health")
            put("GET /api  (this document)")
            put("GET /api/project")
            put("PUT /api/project   body: whole project JSON")
            put("GET /api/assets")
            put("POST /api/assets?name=file.png   body: raw bytes")
            put("DELETE /api/assets?name=file.png")
            put("GET /api/frame?t=1.5&w=640   returns PNG of that moment")
            put("POST /api/render   starts a render")
            put("GET /api/render   {state, progress, message, file}")
            put("GET /api/file?name=final.mp4")
            put("POST /api/scenes   {name,start,end}")
            put("PATCH /api/scenes/{id}   {name|start|end}")
            put("DELETE /api/scenes/{id}")
            put("POST /api/layers?scene={id}   {type,asset|text,x,y,h,z,start,end,anim,animDur,cps,color}")
            put("PATCH /api/layers/{id}   any subset of layer fields")
            put("DELETE /api/layers/{id}")
        })
        put("geometry", "x and y are the layer centre as a fraction of the canvas; h is height as a fraction of canvas height")
        put("anim", JSONArray().put("cut").put("fade").put("pop").put("slide_up").put("typeon"))
        put("project", ProjectStore.current().toJson())
    }

    companion object {
        @Volatile var instance: ApiServer? = null

        fun startServer(): Boolean {
            stopServer()
            val s = ApiServer(Prefs.port, Prefs.lan)
            return runCatching { s.start(); instance = s; true }.getOrDefault(false)
        }

        fun stopServer() {
            instance?.stop()
            instance = null
        }

        fun isRunning(): Boolean = instance?.running == true
    }
}
