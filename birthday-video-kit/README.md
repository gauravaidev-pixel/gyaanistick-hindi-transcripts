# Gyaani Stick — "Birthday" video kit

Everything needed to rebuild the 04:04 Hindi explainer video from static PNGs.

## In this folder

| Path | What it is |
|---|---|
| `spec/Birthday_Video_Build_Spec.md` | The build brief: render settings, asset manifest, motion rules, 56-shot timeline with in/out timecodes, exact text copy with cue frames, voiceover map, verification checklist, and a JSON timeline an agent can load directly |
| `shotlist/Birthday_Video_Shotlist.xlsx` | The same timeline as a spreadsheet — Shot List / On-screen text / Voiceover / Method tabs |
| `assets/bg_paper.png` | The paper texture used for the whole video, nothing else on it |

## Download the big files

They live in the [release](../../releases/tag/birthday-video-kit-v1):

| Asset | What it is |
|---|---|
| `Birthday_Elements_Pack.zip` | Background, voiceover (48 kHz WAV + MP3), 24 transparent PNG cut-outs, README |
| `Birthday_ContactSheets_6x6.zip` | 82 contact sheets, 36 frames each, every frame labelled with its number and timecode |
| `vo_hindi.mp3` | Hindi narration only, 192 kbps |

## How the timings were produced

The source video (640x360, 30 fps, 04:04.19) was re-sampled at 12 fps into 2,930 frames.
Frames were tiled 6x6 into 82 labelled contact sheets and read in order; a per-frame
pixel-diff pass over all 2,930 frames pinned scene cuts to a single frame. Voiceover
timings come from the supplied SRT. Accuracy is about ±1 frame (±0.083 s).

## Notes

- The font is not included — supply the .ttf used in the original.
- `README.md` inside the elements pack lists which assets are clean cut-outs and which
  should come from your own library.
