package com.gyaani.stickstudio

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7)
            }
        }
        setContent { MaterialTheme { StudioScreen() } }
    }
}

private fun copyUriToAssets(ctx: android.content.Context, uri: Uri, forcedName: String? = null): String? {
    val name = forcedName ?: run {
        var n = "asset_${System.currentTimeMillis()}"
        ctx.contentResolver.query(uri, null, null, null, null)?.use { c ->
            val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (i >= 0 && c.moveToFirst()) n = c.getString(i)
        }
        n.replace("/", "_")
    }
    val dest = File(ProjectStore.assetsDir(), name)
    runCatching {
        ctx.contentResolver.openInputStream(uri)!!.use { input ->
            dest.outputStream().use { out -> input.copyTo(out) }
        }
        return dest.name
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen() {
    val ctx = LocalContext.current
    var version by remember { mutableIntStateOf(0) }
    DisposableEffect(Unit) {
        val l: () -> Unit = { version++ }
        ProjectStore.onChange(l)
        onDispose { ProjectStore.offChange(l) }
    }
    val project = remember(version) { ProjectStore.current() }

    var selectedScene by remember { mutableStateOf<String?>(project.scenes.firstOrNull()?.id) }
    var selectedLayer by remember { mutableStateOf<String?>(null) }
    var time by remember { mutableStateOf(0.0) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var renderState by remember { mutableStateOf(Renderer.state) }
    var renderProgress by remember { mutableStateOf(Renderer.progress) }
    var serverOn by remember { mutableStateOf(ApiServer.isRunning()) }
    var token by remember { mutableStateOf(Prefs.token) }
    var port by remember { mutableStateOf(Prefs.port.toString()) }
    var lan by remember { mutableStateOf(Prefs.lan) }
    var assets by remember { mutableStateOf(ProjectStore.assetsDir().listFiles()?.map { it.name } ?: emptyList()) }

    val composer = remember { FrameComposer() }

    // live preview
    LaunchedEffect(version, time) {
        val p = ProjectStore.current()
        val bmp = withContext(Dispatchers.Default) {
            composer.compose(p, time.coerceIn(0.0, p.duration), 720, (720 * p.height / p.width.toFloat()).toInt())
        }
        preview = bmp
    }

    LaunchedEffect(Unit) {
        while (true) {
            renderState = Renderer.state
            renderProgress = Renderer.progress
            delay(400)
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            copyUriToAssets(ctx, uri)?.let { name ->
                composer.clear()
                assets = ProjectStore.assetsDir().listFiles()?.map { it.name } ?: emptyList()
                val sceneId = selectedScene
                if (sceneId != null) {
                    val scene = ProjectStore.findScene(sceneId)
                    ProjectStore.mutate {
                        scene?.layers?.add(
                            Layer(type = "image", asset = name,
                                start = scene.start, end = scene.end)
                        )
                    }
                }
            }
        }
    }
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            copyUriToAssets(ctx, uri, "voice.mp3")?.let {
                ProjectStore.mutate { p -> p.audio = it }
                assets = ProjectStore.assetsDir().listFiles()?.map { it.name } ?: emptyList()
            }
        }
    }
    val bgPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            copyUriToAssets(ctx, uri, "background.png")?.let {
                ProjectStore.mutate { p -> p.background = it }
                composer.clear()
                assets = ProjectStore.assetsDir().listFiles()?.map { it.name } ?: emptyList()
            }
        }
    }
    val fontPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            copyUriToAssets(ctx, uri, "font.ttf")?.let {
                ProjectStore.mutate { p -> p.font = it }
                TextPainter.setFont(null)
                composer.clear()
                assets = ProjectStore.assetsDir().listFiles()?.map { it.name } ?: emptyList()
            }
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Stick Studio", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                AssistChip(
                    onClick = {
                        if (serverOn) { ServerService.stop(ctx); ApiServer.stopServer(); serverOn = false }
                        else { ServerService.start(ctx); serverOn = true }
                    },
                    label = { Text(if (serverOn) "API on :$port" else "API off") }
                )
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = {
                    token = Prefs.newToken()
                }) { Text("new token", fontSize = 11.sp) }
            }
            Text(
                "http://127.0.0.1:$port  ·  X-Token: $token",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            // ---------- preview ----------
            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val boxW = constraints.maxWidth.toFloat()
                val boxH = constraints.maxHeight.toFloat()
                val p = project
                val ratio = p.width.toFloat() / p.height.toFloat()
                var drawW = boxW
                var drawH = drawW / ratio
                if (drawH > boxH) { drawH = boxH; drawW = drawH * ratio }

                preview?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "preview",
                        modifier = Modifier
                            .size(with(LocalDensity.current) { drawW.toDp() }, with(LocalDensity.current) { drawH.toDp() })
                            .align(Alignment.Center)
                            .pointerInput(selectedLayer, drawW, drawH) {
                                detectDragGestures { change, drag ->
                                    val id = selectedLayer ?: return@detectDragGestures
                                    change.consume()
                                    val dx = drag.x / drawW
                                    val dy = drag.y / drawH
                                    ProjectStore.findLayer(id)?.let { (_, layer) ->
                                        ProjectStore.mutate {
                                            layer.x = (layer.x + dx).coerceIn(0f, 1f)
                                            layer.y = (layer.y + dy).coerceIn(0f, 1f)
                                        }
                                    }
                                }
                            }
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("t=${"%.2f".format(time)}s", fontSize = 11.sp)
                Slider(
                    value = time.toFloat(),
                    onValueChange = { time = it.toDouble() },
                    valueRange = 0f..(project.duration.toFloat().coerceAtLeast(1f)),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
            }

            // ---------- scenes ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Scenes", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {
                    val start = project.scenes.maxOfOrNull { it.end } ?: 0.0
                    ProjectStore.mutate { p ->
                        p.scenes.add(Scene(name = "scene ${p.scenes.size + 1}", start = start, end = start + 5))
                        p.duration = maxOf(p.duration, start + 5)
                    }
                }) { Text("+ scene", fontSize = 11.sp) }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(project.scenes) { s ->
                    FilterChip(
                        selected = s.id == selectedScene,
                        onClick = { selectedScene = s.id; time = s.start; selectedLayer = null },
                        label = { Text("${s.name} ${"%.1f".format(s.start)}", fontSize = 10.sp) }
                    )
                }
            }

            // ---------- layers ----------
            val scene = project.scenes.firstOrNull { it.id == selectedScene }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Layers", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { imagePicker.launch(arrayOf("image/*")) }) { Text("+ image", fontSize = 11.sp) }
                TextButton(onClick = {
                    scene?.let { s ->
                        ProjectStore.mutate {
                            s.layers.add(
                                Layer(type = "text", text = "TEXT", h = 0.06f,
                                    x = 0.5f, y = 0.25f, anim = "typeon",
                                    start = s.start, end = s.end)
                            )
                        }
                    }
                }) { Text("+ text", fontSize = 11.sp) }
            }
            scene?.layers?.sortedBy { it.z }?.forEach { layer ->
                val selected = layer.id == selectedLayer
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (layer.type == "text") "T: ${layer.text}" else "I: ${layer.asset}",
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { selectedLayer = layer.id }) { Text("edit", fontSize = 10.sp) }
                            TextButton(onClick = {
                                ProjectStore.mutate { proj ->
                                    proj.scenes.forEach { sc -> sc.layers.removeAll { it.id == layer.id } }
                                }
                                if (selectedLayer == layer.id) selectedLayer = null
                            }) { Text("del", fontSize = 10.sp) }
                        }
                        if (selected) LayerEditor(layer, assets) { composer.clear() }
                    }
                }
            }

            // ---------- project ----------
            Spacer(Modifier.height(12.dp))
            Text("Project", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { bgPicker.launch(arrayOf("image/*")) }) { Text("bg", fontSize = 11.sp) }
                TextButton(onClick = { audioPicker.launch(arrayOf("audio/*")) }) { Text("audio", fontSize = 11.sp) }
                TextButton(onClick = { fontPicker.launch(arrayOf("*/*")) }) { Text("font", fontSize = 11.sp) }
                TextButton(onClick = { imagePicker.launch(arrayOf("image/*")) }) { Text("asset", fontSize = 11.sp) }
            }
            Text(
                "bg=${project.background ?: "-"}  audio=${project.audio ?: "-"}  font=${project.font ?: "-"}",
                fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = port, onValueChange = { port = it.filter { c -> c.isDigit() } },
                    label = { Text("port", fontSize = 10.sp) }, modifier = Modifier.width(100.dp),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Text("LAN", fontSize = 11.sp)
                Switch(checked = lan, onCheckedChange = { lan = it })
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = {
                    Prefs.port = port.toIntOrNull() ?: 8777
                    Prefs.lan = lan
                    if (serverOn) { ServerService.start(ctx) }
                }) { Text("apply", fontSize = 11.sp) }
            }

            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    Renderer.start(ProjectStore.current(), File(ProjectStore.outputDir(), "final.mp4"))
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (renderState == "running") "Rendering ${(renderProgress * 100).toInt()}%" else "Render video") }

            if (renderState == "done") {
                Text("saved: ${ProjectStore.outputDir()}/final.mp4", fontSize = 10.sp)
            } else if (renderState == "error") {
                Text("error: ${Renderer.message}", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun LayerEditor(layer: Layer, assets: List<String>, onAssetChange: () -> Unit) {
    var text by remember(layer.id) { mutableStateOf(layer.text ?: "") }
    var menuOpen by remember { mutableStateOf(false) }

    Column {
        if (layer.type == "text") {
            OutlinedTextField(
                value = text,
                onValueChange = { v ->
                    text = v
                    ProjectStore.mutate { layer.text = v }
                },
                label = { Text("text", fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodySmall
            )
        } else {
            Box {
                TextButton(onClick = { menuOpen = true }) {
                    Text("asset: ${layer.asset ?: "none"} ▾", fontSize = 11.sp)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    assets.forEach { a ->
                        DropdownMenuItem(text = { Text(a, fontSize = 11.sp) }, onClick = {
                            ProjectStore.mutate { layer.asset = a }
                            menuOpen = false
                            onAssetChange()
                        })
                    }
                }
            }
        }

        LabeledSlider("x", layer.x, 0f..1f) { v -> ProjectStore.mutate { layer.x = v } }
        LabeledSlider("y", layer.y, 0f..1f) { v -> ProjectStore.mutate { layer.y = v } }
        LabeledSlider("size", layer.h, 0.02f..1.2f) { v -> ProjectStore.mutate { layer.h = v } }
        LabeledSlider("start", layer.start.toFloat(), 0f..240f) { v ->
            ProjectStore.mutate { layer.start = v.toDouble() }
        }
        LabeledSlider("end", layer.end.toFloat(), 0f..240f) { v ->
            ProjectStore.mutate { layer.end = v.toDouble() }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("cut", "fade", "pop", "slide_up", "typeon").forEach { a ->
                FilterChip(
                    selected = layer.anim == a,
                    onClick = { ProjectStore.mutate { layer.anim = a } },
                    label = { Text(a, fontSize = 9.sp) }
                )
            }
        }
    }
}

@Composable
fun LabeledSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 10.sp, modifier = Modifier.width(38.dp))
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier.weight(1f)
        )
        Text("%.2f".format(value), fontSize = 9.sp, modifier = Modifier.width(34.dp))
    }
}
