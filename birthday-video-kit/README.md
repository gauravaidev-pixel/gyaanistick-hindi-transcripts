# Gyaani Stick — "Birthday" video kit

Everything needed to rebuild the 04:04 Hindi explainer video from static PNGs.

| Folder | What it is |
|---|---|
| `spec/` | `Birthday_Video_Build_Spec.md` — the full build brief: render settings, asset manifest, motion rules, 56-shot timeline with in/out timecodes, text copy with cue frames, voiceover map, verification checklist, and a JSON timeline an agent can load directly |
| `shotlist/` | `Birthday_Video_Shotlist.xlsx` — the same timeline as a spreadsheet (Shot List / On-screen text / Voiceover / Method tabs) |
| `pack/` | `Birthday_Elements_Pack.zip` — background, voiceover (WAV + MP3), 24 transparent PNG cut-outs, README |
| `assets/` | `bg_paper.png` (paper texture) and `vo_hindi.mp3` (narration) as loose files |
| `frames/` | `Birthday_ContactSheets_6x6.zip` — 82 contact sheets, 36 frames each, every frame labelled with its number and timecode (12 fps grid over the whole video) |

## How the timings were produced

The source video (640x360, 30 fps, 04:04.19) was re-sampled at 12 fps into 2,930 frames.
Frames were tiled 6x6 into 82 labelled contact sheets and read in order; a per-frame
pixel-diff pass over all 2,930 frames pinned scene cuts to a single frame. Voiceover
timings come from the supplied SRT. Accuracy is about ±1 frame (±0.083 s).

## Notes

- The font is not included — supply the .ttf used in the original.
- `pack/README.md` lists which assets are clean cut-outs and which should come from your
  own library.
