---
name: gyaani-stick-video-builder
description: Rebuild the Gyaani Stick "Birthday" Hindi explainer video (04:04.19, stick-figure whiteboard style) from static PNGs on a single background, using the frame-accurate build spec and asset pack in the gyaanistick-hindi-transcripts repository. Use when asked to render, re-render, rebuild or edit that video, to change its text/timings/assets, or to produce another video in the same style.
---

# Gyaani Stick video builder

This skill builds a finished MP4 from a spec + a pile of still PNGs. There is no image
generation and no audio analysis: the artwork is supplied, the narration is supplied, and
the only job is placing the right PNG at the right frame with the right text on top.

## 0. Source of truth

Repository: `gauravaidev-pixel/gyaanistick-hindi-transcripts`, folder `birthday-video-kit/`.

| What | Where |
|---|---|
| Build spec (read this first) | `https://raw.githubusercontent.com/gauravaidev-pixel/gyaanistick-hindi-transcripts/main/birthday-video-kit/spec/Birthday_Video_Build_Spec.md` |
| Shot list spreadsheet | `https://raw.githubusercontent.com/gauravaidev-pixel/gyaanistick-hindi-transcripts/main/birthday-video-kit/shotlist/Birthday_Video_Shotlist.xlsx` |
| Background | `https://raw.githubusercontent.com/gauravaidev-pixel/gyaanistick-hindi-transcripts/main/birthday-video-kit/assets/bg_paper.png` |
| Elements pack (bg + audio + 24 PNGs) | `https://github.com/gauravaidev-pixel/gyaanistick-hindi-transcripts/releases/download/birthday-video-kit-v1/Birthday_Elements_Pack.zip` |
| Narration only (mp3) | `https://github.com/gauravaidev-pixel/gyaanistick-hindi-transcripts/releases/download/birthday-video-kit-v1/vo_hindi.mp3` |
| Contact sheets, 82 x 36 frames | `https://github.com/gauravaidev-pixel/gyaanistick-hindi-transcripts/releases/download/birthday-video-kit-v1/Birthday_ContactSheets_6x6.zip` |

If you have `git` and repo access, `git clone https://github.com/gauravaidev-pixel/gyaanistick-hindi-transcripts.git`
is faster than downloading each file.

**The spec is authoritative.** It carries the 56-shot timeline, the exact on-screen copy,
the cue frame for every text line, and the voiceover map. Do not re-derive timings from
the video, and do not paraphrase the copy.

## 1. What you must not do

- Do not generate, redraw or approximate artwork. If a PNG is missing, stop and ask for it.
- Do not time-stretch, trim or re-record the narration. If a shot drifts out of sync,
  move the picture, never the audio.
- Do not change the copy. Spelling, capitalisation and the censored `F*** it` are deliberate.
- Do not invent motion. Everything is a still PNG; motion is only position, scale, opacity
  and a left-to-right text reveal.
- Do not import GIFs or sprite sheets — the source has none.

## 2. Inputs you need before starting

1. The spec (above).
2. The elements pack, unzipped.
3. **The font.** It is not in the repo. Ask for the `.ttf`; without it the hand-drawn look
   is lost. A marker-style fallback is acceptable only for a draft render.
4. The user's own PNG library for anything the pack does not cover — the pack README lists
   the 31 names you may have to source elsewhere.

## 3. Output contract

| Setting | Value |
|---|---|
| Resolution | 1920x1080 if the assets are high-res; **1280x720 if you are using the pack cut-outs** (they are 360p, and 3x upscale looks mushy) |
| Frame rate | 30 fps |
| Duration | exactly 244.19 s |
| Video | H.264, CRF 18, `yuv420p`, faststart |
| Audio | AAC 128 kbps, 48 kHz, stereo, starting at 0.000 |
| Layers | background → scene PNGs → text → logo |

## 4. Build order

### 4.1 Timeline as data, before any rendering

Read the spec's §5 table and write it to `timeline.json`: one record per shot with `in`,
`out`, and a list of layers. Keep it separate from the render code so a timing change is a
one-line edit.

```json
{
  "n": 4,
  "in": 10.667, "out": 16.0,
  "layers": [
    {"file": "stick_wave_with_bubble.png", "x": 0.58, "y": 0.62, "h": 0.46, "anim": "fade", "start": 10.667, "dur": 0.3},
    {"text": "BYE", "x": 0.70, "y": 0.40, "h": 0.06, "anim": "typeon", "start": 13.583, "cps": 24}
  ]
}
```

`x`/`y` are the asset's centre as a fraction of the canvas, `h` is its height as a fraction
of canvas height. The spec's positions are approximations read off the reference frames —
nudge them against the contact sheets until a shot looks right.

### 4.2 Background

One still, full-bleed, on screen for the whole 244 s. Scale with Lanczos, never crop.
If you render above the source resolution, add a very light grain (2–3% opacity) after
upscaling so the paper texture does not look smeared.

### 4.3 Text

Render each line to a transparent PNG at 2x final size with the supplied font, then reveal
it by growing a horizontal crop from 0 to full width at 22–26 characters per second. That
is the only text animation in the reference — no fades, no bounces.

```python
from PIL import Image, ImageDraw, ImageFont

def text_png(line, font_path, px, colour="#1A1A1A"):
    f = ImageFont.truetype(font_path, px)
    box = f.getbbox(line)
    img = Image.new("RGBA", (box[2] + 20, box[3] + 20), (0, 0, 0, 0))
    ImageDraw.Draw(img).text((10, 10), line, font=f, fill=colour)
    return img  # then crop width = full_width * progress(t)

def progress(t, start, cps, chars):
    return max(0.0, min(1.0, (t - start) * cps / max(chars, 1)))
```

Wrap long lines at word boundaries inside 30% of canvas width, and keep every line on
screen for at least 3 s — if a line would land too late, move it 0.5 s earlier.

### 4.4 Motion rules

- Enter: fade 0.20–0.40 s, optionally scale 0.85 → 1.00.
- Exit: fade 0.20 s, unless the next shot is a hard cut — then cut, never fade.
- Hold: every asset stays still for at least 2 s.
- Transitions: hard cuts. Exactly one white flash, at 01:13.83.
- Loops (rain, gears): because assets are stills, offset the PNG vertically 2 px on a
  6-frame cycle. No GIFs.

### 4.5 Render

Any compositor that puts PNGs on a timeline works — ffmpeg `overlay` with `enable=`
expressions, MoviePy `CompositeVideoClip`, or Remotion. Render shot by shot, concat, then
mux the audio. Do not render the whole 4 minutes in one pass if your tool cannot seek
randomly; you will fight sync drift.

## 5. Verify before delivering

Pull these frames from the render and compare with the reference (contact sheets, or
`t = (N-1)/12` on the original video):

| Check | Time |
|---|---|
| party fully built | 00:10.58 |
| BYE bubble visible | 00:13.58 |
| title card complete | 00:28.67 |
| "learned helplessness" fully typed | 01:04.25 |
| "TIME BLINDNESS" fully typed | 01:45.67 |
| "Emotional Maturity" on screen | 02:08.25 |
| crown on pedestal | 02:41.33 |
| "Never lonely ?" + red X | 03:03.25 |
| "Every Day is a Celebration" complete | 03:13.25 |
| end card complete | 03:53.33 |

Then check, mechanically:

- [ ] duration is 244.19 s ±0.05
- [ ] audio starts at 0.000 and ends by 04:04.1
- [ ] background identical in every frame, never shifts or scales
- [ ] no asset appears before or after its frame in the spec
- [ ] every text line readable for at least 3 s
- [ ] no half-faded asset sitting on a hard cut
- [ ] last frame is the end card, held, not a fade to black

## 6. When the user asks for a change

Work in the timeline data, not in the render code:

- "move this text 2 seconds earlier" → edit that layer's `start`.
- "swap the stick pose" → edit that layer's `file`.
- "this shot is too long" → edit the shot's `out` and the next shot's `in` together, so
  nothing overlaps or gaps.
- "change the copy" → edit the text layer only; the voiceover does not change.

After any edit, re-render only the affected shots and re-verify that shot's checkpoint.

## 7. Honest limits

- Cut-outs from the pack are 360p; at 1080p they will look soft. Prefer the user's own
  higher-resolution PNGs for a final render.
- The reference's hand-drawn feel comes from the artwork and the font, not from the code.
  No amount of easing will fix the wrong assets.
- If the font or a required PNG is missing, say so and stop — a draft with substitutes is
  fine only if the user asked for a draft.
