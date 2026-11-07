# Stick Studio

An Android video editor that hosts its own HTTP API. You edit on the phone, an AI
assistant edits over the same API, and both work on one project file.

There is no separate backend. The app starts a server on a port, keeps it alive with a
foreground service, and every route reads and writes the same `timeline.json` that the
UI is looking at.

## What it does

- **Timeline project**: scenes, layers (PNG or text), start/end times, animation, geometry.
- **Manual editing**: live preview, drag a layer on the canvas, sliders for position/size/time,
  text field, asset picker, render button.
- **API editing**: the same operations over HTTP, plus a `/api/frame` endpoint that returns a
  PNG of any moment so an assistant can actually look at what it built.
- **Rendering**: frames are drawn with `FrameComposer` and encoded with `MediaCodec` +
  `MediaMuxer`. Audio is decoded, re-encoded to AAC and muxed in. No FFmpeg, no native libs,
  so the APK stays small and there is nothing to install.

## Install

Download `StickStudio-debug.apk` from the releases page and open it on the phone
(allow "install unknown apps" for your browser or file manager).

## Using it

1. Open the app. The chip at the top says `API off` — tap it to start the server.
   It shows `API on :8777` and a token underneath.
2. Tap `bg` to pick the background image, `audio` for the narration, `font` for the
   handwriting TTF, `asset` to import every PNG you want to use.
3. Tap `+ scene` to add a scene, `+ image` / `+ text` to add layers to the selected scene.
4. Select a layer: drag it on the preview, or use the sliders for `x`, `y`, `size`,
   `start`, `end`. Text layers get a text box; image layers get an asset picker.
5. Tap **Render video**. It writes `final.mp4` into the app's `out` folder.

## API

Base URL `http://127.0.0.1:8777`. Send header `X-Token: <token from the app>` on everything
except `/health`. `GET /api` returns a self-describing schema, so an assistant can discover
the surface without this README.

| Method | Path | Body / params | What it does |
|---|---|---|---|
| GET | `/health` | — | liveness, current render state |
| GET | `/api` | — | endpoint list + the whole project |
| GET | `/api/project` | — | the project JSON |
| PUT | `/api/project` | full project JSON | replace everything |
| GET | `/api/assets` | — | files in the asset folder |
| POST | `/api/assets?name=x.png` | raw bytes | upload a PNG / audio / font |
| DELETE | `/api/assets?name=x.png` | — | delete an asset |
| GET | `/api/frame?t=1.5&w=640` | — | PNG of that moment |
| POST | `/api/render` | — | start rendering |
| GET | `/api/render` | — | `{state, progress, message, file}` |
| GET | `/api/file?name=final.mp4` | — | download the render |
| POST | `/api/scenes` | `{name,start,end}` | add a scene |
| PATCH | `/api/scenes/{id}` | `{name|start|end}` | edit a scene |
| DELETE | `/api/scenes/{id}` | — | remove a scene |
| POST | `/api/layers?scene={id}` | layer fields | add a layer |
| PATCH | `/api/layers/{id}` | any layer fields | edit a layer |
| DELETE | `/api/layers/{id}` | — | remove a layer |

Layer fields: `type` (`image`|`text`), `asset`, `text`, `x`, `y` (centre as a fraction of the
canvas), `h` (height as a fraction of canvas height), `z`, `start`, `end` (seconds),
`anim` (`cut`|`fade`|`pop`|`slide_up`|`typeon`), `animDur`, `cps`, `color`.

Example — add a text line that types on at 12.5 s:

```bash
curl -X POST "http://127.0.0.1:8777/api/layers" \
  -H "X-Token: $TOKEN" -H "Content-Type: application/json" \
  -d '{"type":"text","text":"learned helplessness","x":0.62,"y":0.3,"h":0.06,
       "start":12.5,"end":17.0,"anim":"typeon","cps":22}'
```

## Access from outside the phone

The server binds to `127.0.0.1` by default. To let a desktop agent reach it over Wi-Fi, flip
the `LAN` switch and tap `apply` — it rebinds to `0.0.0.0` on the port you set. Only do this on
a network you trust; the token is the only thing protecting it.

## Building

```bash
# Android SDK 34 + JDK 17 required
./gradlew :app:assembleDebug
```

The project is plain Gradle + Kotlin + Compose, no code generation. `local.properties` needs
`sdk.dir`.

## Layout

```
app/src/main/java/com/gyaani/stickstudio/
  Models.kt        Project / Scene / Layer + JSON
  ProjectStore.kt  the single source of truth, shared by UI and API
  ApiServer.kt     hand-rolled HTTP server and routes
  ServerService.kt foreground service that keeps the port open
  FrameComposer.kt draws any frame at any time (preview == render)
  TextPainter.kt   text to bitmap, typewriter crop
  Renderer.kt      MediaCodec video + AAC audio + MediaMuxer
  MainActivity.kt  the editor UI
```

## Limits worth knowing

- Debug build, unsigned for release. Fine for sideloading, not for the Play Store.
- Rendering is single-threaded on the phone; a 4-minute 1280x720 render takes a few minutes.
- The preview renders at 720 px wide; the final uses the project's `width`/`height`.
- No undo stack yet — `timeline.json` in the app's `project/` folder is the state to back up.
