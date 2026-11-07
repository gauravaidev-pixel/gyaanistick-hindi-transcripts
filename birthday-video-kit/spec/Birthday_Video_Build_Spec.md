# Video Build Spec — Gyaani Stick: "Birthday" (Hindi VO)

**Purpose of this file:** it is a complete, machine-readable brief for an AI coding agent to
assemble the video from static PNGs on a single background. Nothing here needs image
generation or audio analysis — every asset is a still PNG and the voiceover is supplied.

Reference video: `GyaaniStick_birthday_360p.mp4` — 640x360, 30 fps, 04:04.19.
All timings below were read off that video at 12 fps (frame `fN` = `(N-1)/12` seconds), so
they are frame-accurate to about ±1 frame (±0.083 s).

---

## 1. Render settings

| Setting | Value |
|---|---|
| Canvas | 1920 x 1080 (16:9, same ratio as the reference 640x360) |
| Frame rate | 30 fps |
| Duration | 244.19 s (00:04:04.19) |
| Codec | H.264, CRF 18, yuv420p, AAC 128 kbps 48 kHz stereo |
| Background | one PNG, full-bleed, unchanged for the whole video |
| Layer order | 1) background 2) scene PNGs 3) text 4) watermark/logo if any |
| Safe area | keep all text inside 8% margins |

## 2. Asset manifest

Everything is a **still PNG with transparency**. No GIFs, no sprite sheets, no separate
animation frames — motion is produced by moving/scaling/fading the stills.

| Group | Files the agent needs |
|---|---|
| Background | `bg_paper.png` (one only, full video) |
| Stick poses | `stick_stand`, `stick_walk`, `stick_walk_sad`, `stick_sad`, `stick_happy`, `stick_run`, `stick_run_happy`, `stick_sit`, `stick_sit_sad`, `stick_sit_chair`, `stick_meditate`, `stick_shrug`, `stick_wave`, `stick_confident`, `stick_laptop`, `stick_child`, `stick_offer_cake` |
| Props | `png_balloon`, `png_confetti`, `png_streamer`, `png_cake`, `png_cake_table`, `png_gift`, `png_phone`, `png_phone_screen`, `png_crowd`, `png_crowd_phone`, `png_crowd_party`, `png_chair`, `png_table`, `png_calendar`, `png_dinner`, `png_book_stack`, `png_family`, `png_office`, `png_college`, `png_meeting`, `png_assignment`, `png_call`, `png_paper`, `png_desk`, `png_treadmill` |
| Symbols | `png_arrow`, `png_arrow_red`, `png_cross_red`, `png_qmark`, `png_heart`, `png_heart_broken`, `png_brain`, `png_gear`, `png_clock`, `png_melt`, `png_cloud`, `png_rain`, `png_spotlight_purple`, `png_mirror`, `png_stairs`, `png_crown`, `png_trophy`, `png_bubble_thought`, `png_bubble_speech`, `png_bubble_small`, `png_banner_birthday` |
| Brand | `png_logo_stick`, `png_bulb`, `png_checklist` |
| Text | not baked into PNGs — render text live (see §4) |
| Audio | `vo_hindi.wav` (full narration, starts at 00:00.000) |

If a filename is missing, ask the user for it; do not substitute or generate artwork.

## 3. Global motion rules

- **Entering assets:** fade-in 0.20–0.40 s, optionally scale 0.85 → 1.00. Never slide across
  more than 15% of the canvas unless the row says so.
- **Exiting assets:** fade-out 0.20 s. When the next shot is a hard cut, do not fade — cut.
- **Hold:** every asset stays still for at least 2 s so the viewer can read it.
- **Text:** always type-on (characters revealed left to right), never a plain fade, unless
  the row says hold. Speed 22–26 characters per second.
- **Text must be on screen at least 3 s** — if a line would appear later than 3 s before the
  shot ends, move it 0.5 s earlier rather than cutting it short.
- **Rain / gear loops:** because assets are stills, loop the PNG with a 2-frame vertical
  offset and a 6-frame cycle; do not import GIFs.
- **Cuts:** the reference video uses hard cuts at scene changes, not dissolves. A white flash
  is used only once, at 01:13.83 (f887).

## 4. Text style

| Property | Value |
|---|---|
| Font | the hand-written face used in the reference (user to supply the .ttf; fall back to a marker font) |
| Colour | near-black `#1A1A1A`, no outline, no shadow |
| Weight | regular; all-caps only where the copy below is in caps |
| Position | right column at 62%/32% when the figure is centre-left; top-centre at 50%/18% for title lines |
| Max width | 30% of canvas per line, wrap at word boundaries |
| Animation | type-on 22–26 cps, then hold; no bounce, no rotation |

## 5. Master timeline

Positions are written as `x%/y%` (centre of the asset) with `h` = height as a share of the
canvas. They are close approximations read off the reference frames — fine-tune each one
against the frame at the shot's in-point.

| # | In | Out | Dur | Visual | Assets and placement | Text | Animation | VO cue |
|---|---|---|---|---|---|---|---|---|
| 1 | 00:00.000 | 00:02.083 | 2.2s | Empty board. Stick walks in from the left, stops centre-left. | stick_walk.png -> stick_stand.png  |  centre 50%/62%, h 46% | — | figure slides in from left edge, ease-out, 0.6 s | 1-1 |
| 2 | 00:02.167 | 00:07.667 | 5.6s | Party builds: balloons rise, confetti bursts, cake table, crowd pops in around the hero. | png_balloon.png x4 (20-80%/25-40%), png_confetti.png (50%/20%), png_cake_table.png (50%/70%), png_crowd.png (55%/60%), png_gift.png x3 (30-70%/75%) | — | each PNG: fade + scale 0.85->1.0, 0.25 s, staggered 0.3 s apart | 1-2 |
| 3 | 00:07.750 | 00:10.583 | 2.9s | Full party frame; someone holds up a phone. | png_phone.png (72%/45%), png_streamer.png (30%/15%) | — | fade in 0.2 s | 2-2 |
| 4 | 00:10.667 | 00:16.000 | 5.4s | Hard cut. Second figure waves goodbye; crowd drifts out. | stick_wave.png (58%/62%), png_bubble_speech.png (70%/40%) | BYE | bubble pops (scale 0.7->1.0, 0.2 s) at f163 | 3-3 |
| 5 | 00:16.083 | 00:18.333 | 2.3s | Hero walks alone; crossed-out icons appear one by one. | stick_walk.png (45%/62%), png_cross_red.png over png_cake.png (30%/35%), png_balloon.png (50%/30%), png_dinner.png (70%/35%) | — | crosses draw on left-to-right 0.3 s each, 0.4 s apart | 3-3 |
| 6 | 00:18.417 | 00:19.833 | 1.5s | Large question mark appears beside the figure. | png_qmark.png (62%/45%) | ? | hand-drawn reveal, 0.4 s | 4-4 |
| 7 | 00:19.917 | 00:23.500 | 3.7s | Thought bubble splits into three labelled arrows to the head. | png_bubble_thought.png (60%/30%), png_arrow.png x3 (35/50/65% -> 55%/48%) | Sadness / Maturity / Something else? | bubble grows 0.4 s, labels fade in 0.2 s apart | 4-5 |
| 8 | 00:23.583 | 00:26.083 | 2.6s | Heart / broken-heart beat; figure looks down. | stick_sad.png (50%/62%), png_heart_broken.png (62%/45%) | — | fade 0.3 s | 5-5 |
| 9 | 00:26.167 | 00:28.667 | 2.6s | Title card drawn on the board. | png_bubble_thought.png (52%/40%) | Let's Decode... (find out your psychology) | type-on, 22 chars/s | 5-6 |
| 10 | 00:28.750 | 00:35.417 | 6.8s | Figure slumped, rain cloud overhead. | stick_sit_sad.png (48%/68%), png_cloud.png (50%/30%), png_rain.png (50%/38%) | — | cloud fades 0.4 s, rain loops (see note) | 6-7 |
| 11 | 00:35.500 | 00:39.250 | 3.8s | Figure walks holding a broken heart; thought bubble. | stick_walk_sad.png (45%/62%), png_heart_broken.png (52%/58%), png_bubble_thought.png (60%/32%) | not worth celebrating | bubble 0.3 s, text type-on 20 cps | 8-8 |
| 12 | 00:39.333 | 00:42.083 | 2.8s | Child-size figure alone at a birthday table with cake. | stick_child.png (45%/65%), png_table.png (50%/72%), png_cake.png (50%/66%) | — | fade 0.3 s | 8-9 |
| 13 | 00:42.167 | 00:45.167 | 3.1s | Flashback: phone screen with zero messages. | png_phone_screen.png (52%/55%), png_calendar.png (35%/30%) | — | fade 0.3 s | 9-10 |
| 14 | 00:45.250 | 00:47.917 | 2.8s | Friends walk past; figure watches. | stick_walk.png x3 (60-80%/60%), stick_stand.png (40%/62%) | — | figures slide right, 1.2 s | 10-10 |
| 15 | 00:48.000 | 00:49.750 | 1.8s | Party planned, nobody came: empty chairs. | png_chair.png x4 (30-70%/68%), png_balloon_deflated.png (45%/30%) | — | fade 0.3 s | 10-11 |
| 16 | 00:49.833 | 00:52.833 | 3.1s | Empty beat / big cross-out. | png_cross_red.png (50%/50%, scale 1.6) | — | scale-in 0.3 s | 11-11 |
| 17 | 00:52.917 | 00:55.583 | 2.8s | Brain diagram with gears. | png_brain.png (50%/50%), png_gear.png x2 (40/60%/45%) | — | gears fade 0.3 s | 11-12 |
| 18 | 00:55.667 | 00:59.667 | 4.1s | Expectation icons drop away one by one. | png_bubble_small.png x5 (30-70%/35%), png_cross_red.png x5 | — | each bubble fades out then cross pops, 0.5 s apart | 12-13 |
| 19 | 00:59.750 | 01:04.250 | 4.6s | Concept label written next to the brain. | png_brain.png (40%/50%) | learned helplessness | type-on 22 cps, underline sweep after | 12-13 |
| 20 | 01:04.333 | 01:09.333 | 5.1s | Text stamp; figure stands still. | stick_stand.png (45%/62%) | no more expectations | type-on 24 cps | 13-13 |
| 21 | 01:09.417 | 01:13.750 | 4.4s | Flat ordinary-day beat. | png_calendar.png (60%/45%) | Birthday? Who cares? | type-on 24 cps | 14-15 |
| 22 | 01:13.833 | 01:16.250 | 2.5s | Fast wipe transition. | — | — | white-flash 4 frames + 6-frame slide wipe | 15-15 |
| 23 | 01:16.333 | 01:20.417 | 4.2s | Fast-paced life: figure runs between work/college/family icons. | stick_run.png (40%/62%), png_office.png (20%/40%), png_college.png (75%/40%), png_family.png (55%/28%) | Fast-Paced Life | type-on 24 cps at 60%/18% | 15-16 |
| 24 | 01:20.500 | 01:26.917 | 6.5s | Run cycle past stacked books, family, laptop. | stick_run.png (35%/62%), png_book_stack.png (20%/70%), png_family.png (65%/30%), png_laptop.png (80%/65%) | — | icons fade 0.25 s as figure passes | 16-17 |
| 25 | 01:27.000 | 01:33.833 | 6.9s | Figure buried behind laptop at desk. | stick_laptop.png (50%/65%), png_desk.png (50%/75%), png_paper.png x3 | Birthday? (f1027) then Birthday? F*** it (f1040) | two-step type-on, 26 cps | 17-17 |
| 26 | 01:33.917 | 01:39.250 | 5.4s | Overload: meetings, assignments, calls pile on. | png_meeting.png, png_assignment.png, png_call.png stacked (45-65%/25-40%) | — | each drops in from above 0.25 s, 0.5 s apart | 18-19 |
| 27 | 01:39.333 | 01:45.667 | 6.4s | Clock / melting time. | png_clock.png (55%/35%), png_melt.png (55%/45%) | TIME BLINDNESS | type-on 22 cps, all caps | 20-20 |
| 28 | 01:45.750 | 01:51.167 | 5.5s | Figure shrugs it off. | stick_shrug.png (48%/62%) | No Biggie | type-on 24 cps | 21-21 |
| 29 | 01:51.250 | 01:58.083 | 6.9s | Treadmill / survive-not-enjoy. | stick_run.png (45%/62%), png_treadmill.png (45%/78%), png_gift.png (70%/40%) | — | fade 0.3 s | 21-22 |
| 30 | 01:58.167 | 02:00.750 | 2.7s | Quick labels one after another. | stick_stand.png (35%/62%) | So what? (f1354) / not loneliness (f1425) | type-on 26 cps each, 0.7 s apart | 23-24 |
| 31 | 02:00.833 | 02:03.417 | 2.7s | Second label lands, calm title beat starts. | stick_confident.png (45%/62%) | not Stress (f1441) -> Emotional Maturity (f1451) | type-on 26 cps | 24-24 |
| 32 | 02:03.500 | 02:08.250 | 4.8s | Calm held title frame. | stick_confident.png (45%/62%) | Emotional Maturity (hold) | no change - hold the frame | 24-25 |
| 33 | 02:08.333 | 02:11.917 | 3.7s | Birthday banner over the figure. | png_banner_birthday.png (50%/20%), stick_stand.png (50%/62%) | HAPPY BIRTHDAY (banner art) | banner drops 0.3 s | 26-26 |
| 34 | 02:12.000 | 02:20.583 | 8.7s | Contrast: chasing parties, attention, gifts; mood swings. | stick_happy.png <-> stick_sad.png (50%/62%), png_party_hat.png, png_gift.png, png_phone.png | Emotionally Unstable | type-on 22 cps | 24-27 |
| 35 | 02:20.667 | 02:22.417 | 1.8s | Transition into theory board. | — | — | fade to background 0.3 s | 27-28 |
| 36 | 02:22.500 | 02:29.500 | 7.1s | Theory board written step by step. | stick_stand.png (30%/62%) | Self-Determination Theory (f1711) / Control (f1766) / Growth (f1781) / Satisfaction (f1796) | type-on 22 cps each | 28-29 |
| 37 | 02:29.583 | 02:32.083 | 2.6s | Figure stands confident. | stick_confident.png (50%/62%) | — | fade 0.3 s | 29-30 |
| 38 | 02:32.167 | 02:36.167 | 4.1s | Cake offered, refused - red no-symbol over cake. | stick_offer_cake.png (60%/62%), png_cross_red.png over png_cake.png (60%/55%) | red 'no' over cake (f1827) | cake slides in 0.4 s, cross pops 0.2 s | 30-30 |
| 39 | 02:36.250 | 02:41.333 | 5.2s | Stairs to a crown on a pedestal; mirror appears. | png_stairs.png (55%/70%), png_crown.png (55%/35%), png_mirror.png (35%/55%) | — | crown fades 0.3 s, mirror 0.3 s at f1903 | 31-32 |
| 40 | 02:41.417 | 02:43.583 | 2.2s | Mirror beat. | png_mirror.png (50%/55%), stick_stand.png (62%/62%) | not only limited to BIRTHDAYS (f1938) | type-on 24 cps | 32-32 |
| 41 | 02:43.667 | 02:50.083 | 6.5s | Mirror scene continues, side note on the right. | png_mirror.png (35%/55%), stick_stand.png (48%/62%) | People who treat their Birthday as a NORMAL DAY (f1965) | type-on 24 cps, right column 68%/30% | 32-33 |
| 42 | 02:50.167 | 02:54.167 | 4.1s | Crowd of phones surrounds the hero; purple spotlight. | stick_stand.png (50%/62%), png_crowd_phone.png x6, png_spotlight_purple.png (50%/60%) | — | crowd fades 0.3 s, spotlight fades 0.4 s at f2053 | 33-34 |
| 43 | 02:54.250 | 02:56.750 | 2.6s | Spotlight crossed out; figure left holding a heart. | png_cross_red.png (50%/55%), png_heart.png (50%/70%) | red X over spotlight (f2055) | cross scale-in 0.25 s | 34-34 |
| 44 | 02:56.833 | 03:03.250 | 6.5s | Figure smiles, red X cancels the idea. | stick_stand.png (50%/62%), png_cross_red.png (50%/55%) | Never lonely ? (f2123) | label type-on 24 cps, X pops 0.2 s | 35-35 |
| 45 | 03:03.333 | 03:10.417 | 7.2s | Figure sits; thought bubble grows and fills. | stick_sit.png (45%/68%), png_bubble_thought.png (55%/30%) | Every day is special (f2229) | bubble grows 0.5 s, text type-on 22 cps | 36-36 |
| 46 | 03:10.500 | 03:13.167 | 2.8s | Text types on while figure runs joyfully. | stick_run_happy.png (50%/62%) | Every Day is a Celebration (f2287-f2295) | type-on 26 cps | 37-37 |
| 47 | 03:13.250 | 03:20.917 | 7.8s | Crowd celebrates; cut to laptop figure; arrows label both. | png_crowd_party.png (35%/60%), png_banner_birthday.png (35%/25%), stick_laptop.png (75%/60%), png_arrow.png x2 | Birthday Celebration banner (f2341) / Your Psychology (f2399) | arrows draw 0.3 s, label type-on 24 cps | 37-39 |
| 48 | 03:21.000 | 03:24.333 | 3.4s | Figure sits in a chair, label on the right. | stick_sit_chair.png (45%/65%) | Past Pain (f2449) | type-on 24 cps | 39-39 |
| 49 | 03:24.417 | 03:29.417 | 5.1s | Running figure settles into meditation at f2473. | stick_run.png -> stick_meditate.png (50%/65%) | — | cut at f2473, no transition | 40-40 |
| 50 | 03:29.500 | 03:32.417 | 3.0s | Meditating figure, bubble fills. | stick_meditate.png (45%/68%), png_bubble_thought.png (58%/30%) | It's not about the cake (f2515) | type-on 22 cps | 40-40 |
| 51 | 03:32.500 | 03:34.833 | 2.4s | Bubble swaps to a new question. | png_bubble_thought.png (58%/30%) | What are you celebrating? (f2551) | type-on 24 cps | 40-41 |
| 52 | 03:34.917 | 03:37.583 | 2.8s | Bubble swaps again. | png_bubble_thought.png (58%/30%) | Outside validation? (f2580) | type-on 24 cps | 41-41 |
| 53 | 03:37.667 | 03:41.917 | 4.3s | Mirror scene: figure looks at itself. | png_mirror.png (40%/55%), stick_stand.png (55%/62%) | or inner peace ? (f2596) | type-on 24 cps, mirror fade 0.3 s | 41-41 |
| 54 | 03:42.000 | 03:46.167 | 4.2s | Red arrow sticker points up at the figure. | png_arrow_red.png (62%/40%), stick_stand.png (50%/62%) | LIKE & SUBSCRIBE (f2665) | arrow pops 0.2 s, text type-on 24 cps | 43-43 |
| 55 | 03:46.250 | 03:51.250 | 5.1s | Logo: bulb over head + glasses. | png_logo_stick.png (45%/55%), png_bulb.png (45%/35%) | Gyaani Stick (logo art) | logo fades 0.4 s, bulb glows on 0.3 s | 43-44 |
| 56 | 03:51.333 | 04:04.083 | 12.8s | End card builds in three steps and holds. | png_logo_stick.png (30%/55%), png_checklist.png (62%/45%) | Choose your Category: (f2741) / Birthday Celebration (f2759) / Normal Day (f2780) | three-step type-on 22 cps, then hold 12 s | 44-46 |

## 6. On-screen text — exact copy and cue frame

`fN` is the frame at which the first character appears; convert with `t = (N-1)/12`.

| Copy (exact) | First frame | Time | Notes |
|---|---|---|---|
| BYE | f163 | 00:13.500 | speech bubble |
| ? | f222 | 00:18.417 | large hand-drawn mark |
| Sadness / Maturity / Something else? | f240 | 00:19.917 | three labels, one bubble |
| Let's Decode... (find out your psychology) | f315 | 00:26.167 | title card |
| not worth celebrating | f440 | 00:36.583 | thought bubble |
| learned helplessness | f718 | 00:59.750 | term 1 - lowercase as shown |
| no more expectations | f773 | 01:04.333 |  |
| Birthday? Who cares? | f834 | 01:09.417 |  |
| Fast-Paced Life | f917 | 01:16.333 |  |
| Birthday? | f1027 | 01:25.500 | then replaced at f1040 |
| Birthday? F*** it | f1040 | 01:26.583 | censored on screen, keep the asterisks |
| TIME BLINDNESS | f1193 | 01:39.333 | all caps |
| No Biggie | f1290 | 01:47.417 |  |
| So what? | f1354 | 01:52.750 |  |
| not loneliness | f1425 | 01:58.667 |  |
| not Stress | f1441 | 02:00.000 |  |
| Emotional Maturity | f1451 | 02:00.833 | term 3 |
| HAPPY BIRTHDAY | f1541 | 02:08.333 | banner artwork, not live text |
| Emotionally Unstable | f1585 | 02:12.000 |  |
| Self-Determination Theory | f1711 | 02:22.500 | term 4 |
| Control | f1766 | 02:27.083 |  |
| Growth | f1781 | 02:28.333 |  |
| Satisfaction | f1796 | 02:29.583 |  |
| red no-symbol over cake | f1827 | 02:32.167 | graphic, not text |
| not only limited to BIRTHDAYS | f1938 | 02:41.417 |  |
| People who treat their Birthday as a NORMAL DAY | f1965 | 02:43.667 | two-line wrap |
| purple spotlight | f2053 | 02:51.000 | graphic |
| red X over spotlight | f2055 | 02:51.167 | graphic |
| Never lonely ? | f2123 | 02:56.833 | + red X graphic |
| Every day is special | f2229 | 03:05.667 |  |
| Every Day is a Celebration | f2287 | 03:10.500 | type-on completes f2295 |
| Birthday Celebration | f2341 | 03:15.000 | banner over crowd |
| Your Psychology | f2399 | 03:19.833 |  |
| Past Pain | f2449 | 03:24.000 |  |
| It's not about the cake | f2515 | 03:29.500 | thesis line |
| What are you celebrating? | f2551 | 03:32.500 |  |
| Outside validation? | f2580 | 03:34.917 |  |
| or inner peace ? | f2596 | 03:36.250 |  |
| LIKE & SUBSCRIBE | f2665 | 03:42.000 | + red arrow graphic |
| Gyaani Stick | f2716 | 03:46.250 | logo artwork |
| Choose your Category: | f2741 | 03:48.333 |  |
| Birthday Celebration | f2759 | 03:49.833 | bullet line |
| Normal Day | f2780 | 03:51.583 | bullet line |

## 7. Voiceover

One Hindi narration track, 46 lines, starting at 00:00.000 — the supplied SRT is the
authoritative timing. Place the audio once on the timeline and do not time-stretch it;
if a shot is out of sync, move the picture, not the audio.

| Cue | In | Out | Frame in | Line |
|---|---|---|---|---|
| 1 | 00:00.080 | 00:04.480 | f2 | क्या तुमने कभी नोटिस किया है? कुछ लोग अपना बर्थडे ऐसे मनाते हैं जैसे कोई |
| 2 | 00:04.480 | 00:10.639 | f55 | फेस्टिवल हो। बैलूंस, केक्स, रील्स और एंडलेस Instagram स्टोरीज। और फिर कुछ लोग |
| 3 | 00:10.639 | 00:15.679 | f129 | होते हैं जिन्हें अपने बर्थडे के दिन भी कोई फर्क नहीं पड़ता। ना केक, ना पार्टी, |
| 4 | 00:15.679 | 00:21.680 | f189 | ना डिनर। बस एक नॉर्मल दिन। लेकिन सवाल यह है, क्यों? क्यों कुछ लोग अपना बर्थडे एक |
| 5 | 00:21.680 | 00:27.119 | f261 | ऑर्डिनरी दिन की तरह ट्रीट करते हैं? क्या यह सैडनेस है? मैच्योरिटी या कुछ और चल |
| 6 | 00:27.119 | 00:32.000 | f326 | रहा होता है उनके दिमाग के अंदर? आज हम इस वीडियो में एग्जैक्टली यह समझेंगे और शायद |
| 7 | 00:32.000 | 00:37.120 | f385 | इसके एंड तक तुम्हें अपनी भी साइकोलॉजी समझ आ जाए। कुछ लोगों के लिए स्टोरी सिंपल |
| 8 | 00:37.120 | 00:42.079 | f446 | नहीं होती, दर्द भरी होती है। वह अपना बर्थडे नहीं मनाते क्योंकि उन्हें कभी फील |
| 9 | 00:42.079 | 00:46.719 | f506 | ही नहीं हुआ कि उनका बर्थडे वर्थ सेलिब्रेटिंग है। सोचो जरा बचपन में जब |
| 10 | 00:46.719 | 00:51.440 | f562 | तुम्हारा बर्थडे था पर किसी ने याद नहीं रखा। दोस्तों ने विश नहीं किया। फैमिली |
| 11 | 00:51.440 | 00:56.640 | f618 | बिजी थी और एक बार जब पार्टी प्लान की थी तो कोई आया ही नहीं। दिमाग धीरे-धीरे |
| 12 | 00:56.640 | 01:02.160 | f681 | एडजस्ट कर लेता है। साइकोलॉजी में इसे कहते हैं लर्नड हेल्पलेसनेस। जब बार-बार |
| 13 | 01:02.160 | 01:06.320 | f747 | डिसपॉइंटमेंट मिलती है तो इंसान एक्सपेक्टेशन ही छोड़ देता है। फिर एक दिन |
| 14 | 01:06.320 | 01:12.000 | f797 | आता है जब बर्थडे भी बस एक दिन बन जाता है। ना एक्साइटमेंट ना प्लानिंग। क्योंकि |
| 15 | 01:12.000 | 01:17.119 | f865 | केयर करना हर्ट करने लगता है। फिर एक दूसरी कैटेगरी है वो लोग जो जिंदगी के |
| 16 | 01:17.119 | 01:22.720 | f926 | फास्ट ट्रैक में अटक गए हैं। वर्क, डेडलाइंस, कॉलेज, फैमिली, स्ट्रेस यह सब |
| 17 | 01:22.720 | 01:27.759 | f994 | इतना ओवरलोड कर देता है ब्रेन को कि वो स्पेशल डेज स्पेशल लगना बंद हो जाते हैं। |
| 18 | 01:27.759 | 01:32.560 | f1054 | उनका बर्थडे इग्नोर नहीं होता। बस स्क्वीज़ हो जाता है उनके स्केेड्यूल के बीच। |
| 19 | 01:32.560 | 01:38.560 | f1112 | मॉर्निंग मीटिंग्स, असाइनमेंट्स, कॉल्स और पता चलता है शाम हो गई। इसको साइकोलॉजिस्ट |
| 20 | 01:38.560 | 01:44.079 | f1184 | कहते हैं टाइम ब्लाइंडनेस। जब हम इतने बिजी हो जाते हैं कि टाइम और इमोशन दोनों |
| 21 | 01:44.079 | 01:48.560 | f1250 | स्लिप कर जाते हैं। ऐसे लोगों के लिए बर्थडे कोई बिग डील नहीं लगता क्योंकि वह |
| 22 | 01:48.560 | 01:53.520 | f1304 | सिर्फ सर्वाइव कर रहे होते हैं। एंजॉय नहीं। और फिर आते हैं वो लोग जो जानते हैं |
| 23 | 01:53.520 | 01:58.560 | f1363 | कि आज उनका बर्थडे है पर उन्हें सेलिब्रेट करने की जरूरत महसूस नहीं होती। ना यह |
| 24 | 01:58.560 | 02:05.040 | f1424 | लोनलीनेस है ना स्ट्रेस। यह है एक लेवल की इमोशनल मैच्योरिटी। रिसर्च कहती है जिन |
| 25 | 02:05.040 | 02:10.479 | f1501 | लोगों की खुशी एक्सटर्नल चीजों पर डिपेंड करती है जैसे पार्टी, अटेंशन या गिफ्ट्स |
| 26 | 02:10.479 | 02:15.680 | f1567 | वो इमोशनली अनस्टेबल होते हैं। लेकिन जिन्हें अपनी वैल्यू खुद पता होती है, |
| 27 | 02:15.680 | 02:20.319 | f1629 | उन्हें कैलेंडर पे एक डेट चाहिए ही नहीं होती अपनाेंस फील करने के लिए। |
| 28 | 02:20.319 | 02:25.360 | f1685 | साइकोलॉजिस्ट इसे कहते हैं सेल्फ डिटरमिनेशन थ्योरी जिसमें इंसान की रियल |
| 29 | 02:25.360 | 02:31.360 | f1745 | हैप्पीनेस आती है अपने कंट्रोल, अपनी ग्रोथ और अपने सेटिस्फेक्शन से। ऐसे लोग |
| 30 | 02:31.360 | 02:36.000 | f1817 | कहते हैं, "मुझे कैंडल्स और केक से अपनी वैल्यू प्रूव नहीं करनी। मुझे पता है मैं |
| 31 | 02:36.000 | 02:41.280 | f1873 | इंपॉर्टेंट हूं बिना किसी रिमाइंडर के। सबसे इंटरेस्टिंग बात यह है यह माइंडसेट |
| 32 | 02:41.280 | 02:46.080 | f1936 | सिर्फ बर्थडे तक लिमिटेड नहीं होता। जो लोग अपना बर्थडे नॉर्मल ट्रीट करते हैं, |
| 33 | 02:46.080 | 02:51.840 | f1994 | वह जिंदगी के हर एरिया में सेम अप्रोच ले चलते हैं। वो स्पॉटलाइट नहीं ढूंढते। वो |
| 34 | 02:51.840 | 02:56.879 | f2063 | लाइक्स और अटेंशन के पीछे नहीं भागते। वो अपनी खुशी अंदर से निकालते हैं। क्या इसका |
| 35 | 02:56.879 | 03:02.560 | f2124 | मतलब है वह कभी लोनली नहीं होते? बिल्कुल नहीं। वह भी इंसान है। बस डिफरेंस इतना |
| 36 | 03:02.560 | 03:08.720 | f2192 | है। वह समझ गए हैं कि हर दिन स्पेशल है। अगर तुम उसे स्पेशल बना दो। उनके लिए लाइफ |
| 37 | 03:08.720 | 03:14.080 | f2266 | सेलिब्रेशन सिर्फ साल में एक दिन नहीं रोज का एक इमोशन है। तो चाहे तुम बर्थडे पर |
| 38 | 03:14.080 | 03:19.040 | f2330 | धूम मचाते हो या उसे एक नॉर्मल ट्यूसडे की तरह ट्रीट करते हो। यह दोनों ही तुम्हारे |
| 39 | 03:19.040 | 03:24.159 | f2389 | अंदर की साइकोलॉजी रिवील करते हैं। कुछ लोग के लिए यह उनके पास्ट का पेन होता है। |
| 40 | 03:24.159 | 03:29.680 | f2451 | कुछ के लिए प्रायोरिटीज और कुछ के लिए पीस। सवाल यह नहीं है कि तुम केक काट रहे |
| 41 | 03:29.680 | 03:34.879 | f2517 | हो या नहीं? सवाल यह है तुम्हें अपने अस्तित्व का सेलिब्रेशन कहां मिलता है? |
| 42 | 03:34.879 | 03:40.319 | f2580 | बाहरों में या खुद के अंदर? अगर इस वीडियो ने तुम्हें अपने बारे में सोचने पर मजबूर |
| 43 | 03:40.319 | 03:45.680 | f2645 | किया तो सब्सक्राइब करो ज्ञानी स्टिक को। क्योंकि यहां हम दिखावा नहीं इनसाइड देते |
| 44 | 03:45.680 | 03:50.799 | f2709 | हैं और कमेंट में लिखो तुम किस कैटेगरी में आते हो? बर्थडे सेलिब्रेशन वाले या |
| 45 | 03:50.799 | 03:56.080 | f2771 | नॉर्मल डे वाले। मैं मिलता हूं तुमसे अगले वीडियो में जहां हम और एक ह्यूमन हैबिट को |
| 46 | 03:56.080 | 04:03.799 | f2834 | डिकोड करेंगे और समझेंगे क्यों हम वैसे बिहेव करते हैं जैसे हम करते हैं। |

## 8. How the agent should build it

1. Read this file top to bottom. Do not invent assets, timings or copy.
2. Build a timeline as data first — one record per shot with `in`, `out`, and a list of
   layers `{file, x, y, h, anim, anim_start}` — and keep it in a JSON file next to the script.
3. Render with any compositor that can place PNGs on a timeline (ffmpeg `overlay` + `enable`,
   MoviePy `CompositeVideoClip`, or Remotion). Keep every asset as a still; animate only
   position, scale and opacity.
4. Text: render each line to a transparent PNG with the supplied font at 2x the final size,
   then reveal it with a horizontal crop that grows at 22–26 cps. This is the type-on effect.
5. Mix audio last, then export H.264 CRF 18.
6. Verify against the reference (see §9) before delivering.

```python
# skeleton - one shot, one layer
shot = {"in": 10.667, "out": 16.000, "layers": [
    {"file": "stick_wave.png",     "x": 0.58, "y": 0.62, "h": 0.46,
     "anim": "fade", "start": 10.667, "dur": 0.30},
    {"file": "png_bubble_speech.png", "x": 0.70, "y": 0.40, "h": 0.22,
     "anim": "pop",  "start": 13.500, "dur": 0.20},
    {"text": "BYE", "x": 0.70, "y": 0.40, "h": 0.06,
     "anim": "typeon", "start": 13.583, "cps": 24},
]}
```

## 9. Verification checklist

Pull these frames from the reference and compare side by side (12 fps grid, so `t=(N-1)/12`):

| Check | Reference frame | Time |
|---|---|---|
| party fully built | f128 | 00:10.583 |
| BYE bubble | f163 | 00:13.500 |
| title card | f345 | 00:28.667 |
| learned helplessness | f772 | 01:04.250 |
| TIME BLINDNESS | f1269 | 01:45.667 |
| Emotional Maturity | f1540 | 02:08.250 |
| Self-Determination Theory | f1795 | 02:29.500 |
| crown on pedestal | f1937 | 02:41.333 |
| Never lonely + X | f2200 | 03:03.250 |
| Every Day is a Celebration | f2319 | 03:13.167 |
| end card complete | f2800 | 03:53.250 |

- [ ] background identical in every frame, never shifts
- [ ] no asset pops in earlier or later than its frame in §5
- [ ] every text line readable for at least 3 s
- [ ] voiceover starts at 0.000 and ends by 4:04.1
- [ ] no frame shows a half-faded asset at a hard cut

## 10. Appendix — timeline as JSON

Machine-readable version of §5, safe to load directly.

```json
[
 {
  "n": 1,
  "in": 0.0,
  "out": 2.083,
  "visual": "Empty board. Stick walks in from the left, stops centre-left.",
  "assets": "stick_walk.png -> stick_stand.png  |  centre 50%/62%, h 46%",
  "text": null,
  "anim": "figure slides in from left edge, ease-out, 0.6 s",
  "vo_cues": [
   1,
   1
  ]
 },
 {
  "n": 2,
  "in": 2.167,
  "out": 7.667,
  "visual": "Party builds: balloons rise, confetti bursts, cake table, crowd pops in around the hero.",
  "assets": "png_balloon.png x4 (20-80%/25-40%), png_confetti.png (50%/20%), png_cake_table.png (50%/70%), png_crowd.png (55%/60%), png_gift.png x3 (30-70%/75%)",
  "text": null,
  "anim": "each PNG: fade + scale 0.85->1.0, 0.25 s, staggered 0.3 s apart",
  "vo_cues": [
   1,
   2
  ]
 },
 {
  "n": 3,
  "in": 7.75,
  "out": 10.583,
  "visual": "Full party frame; someone holds up a phone.",
  "assets": "png_phone.png (72%/45%), png_streamer.png (30%/15%)",
  "text": null,
  "anim": "fade in 0.2 s",
  "vo_cues": [
   2,
   2
  ]
 },
 {
  "n": 4,
  "in": 10.667,
  "out": 16.0,
  "visual": "Hard cut. Second figure waves goodbye; crowd drifts out.",
  "assets": "stick_wave.png (58%/62%), png_bubble_speech.png (70%/40%)",
  "text": "BYE",
  "anim": "bubble pops (scale 0.7->1.0, 0.2 s) at f163",
  "vo_cues": [
   3,
   3
  ]
 },
 {
  "n": 5,
  "in": 16.083,
  "out": 18.333,
  "visual": "Hero walks alone; crossed-out icons appear one by one.",
  "assets": "stick_walk.png (45%/62%), png_cross_red.png over png_cake.png (30%/35%), png_balloon.png (50%/30%), png_dinner.png (70%/35%)",
  "text": null,
  "anim": "crosses draw on left-to-right 0.3 s each, 0.4 s apart",
  "vo_cues": [
   3,
   3
  ]
 },
 {
  "n": 6,
  "in": 18.417,
  "out": 19.833,
  "visual": "Large question mark appears beside the figure.",
  "assets": "png_qmark.png (62%/45%)",
  "text": "?",
  "anim": "hand-drawn reveal, 0.4 s",
  "vo_cues": [
   4,
   4
  ]
 },
 {
  "n": 7,
  "in": 19.917,
  "out": 23.5,
  "visual": "Thought bubble splits into three labelled arrows to the head.",
  "assets": "png_bubble_thought.png (60%/30%), png_arrow.png x3 (35/50/65% -> 55%/48%)",
  "text": "Sadness / Maturity / Something else?",
  "anim": "bubble grows 0.4 s, labels fade in 0.2 s apart",
  "vo_cues": [
   4,
   5
  ]
 },
 {
  "n": 8,
  "in": 23.583,
  "out": 26.083,
  "visual": "Heart / broken-heart beat; figure looks down.",
  "assets": "stick_sad.png (50%/62%), png_heart_broken.png (62%/45%)",
  "text": null,
  "anim": "fade 0.3 s",
  "vo_cues": [
   5,
   5
  ]
 },
 {
  "n": 9,
  "in": 26.167,
  "out": 28.667,
  "visual": "Title card drawn on the board.",
  "assets": "png_bubble_thought.png (52%/40%)",
  "text": "Let's Decode... (find out your psychology)",
  "anim": "type-on, 22 chars/s",
  "vo_cues": [
   5,
   6
  ]
 },
 {
  "n": 10,
  "in": 28.75,
  "out": 35.417,
  "visual": "Figure slumped, rain cloud overhead.",
  "assets": "stick_sit_sad.png (48%/68%), png_cloud.png (50%/30%), png_rain.png (50%/38%)",
  "text": null,
  "anim": "cloud fades 0.4 s, rain loops (see note)",
  "vo_cues": [
   6,
   7
  ]
 },
 {
  "n": 11,
  "in": 35.5,
  "out": 39.25,
  "visual": "Figure walks holding a broken heart; thought bubble.",
  "assets": "stick_walk_sad.png (45%/62%), png_heart_broken.png (52%/58%), png_bubble_thought.png (60%/32%)",
  "text": "not worth celebrating",
  "anim": "bubble 0.3 s, text type-on 20 cps",
  "vo_cues": [
   8,
   8
  ]
 },
 {
  "n": 12,
  "in": 39.333,
  "out": 42.083,
  "visual": "Child-size figure alone at a birthday table with cake.",
  "assets": "stick_child.png (45%/65%), png_table.png (50%/72%), png_cake.png (50%/66%)",
  "text": null,
  "anim": "fade 0.3 s",
  "vo_cues": [
   8,
   9
  ]
 },
 {
  "n": 13,
  "in": 42.167,
  "out": 45.167,
  "visual": "Flashback: phone screen with zero messages.",
  "assets": "png_phone_screen.png (52%/55%), png_calendar.png (35%/30%)",
  "text": null,
  "anim": "fade 0.3 s",
  "vo_cues": [
   9,
   10
  ]
 },
 {
  "n": 14,
  "in": 45.25,
  "out": 47.917,
  "visual": "Friends walk past; figure watches.",
  "assets": "stick_walk.png x3 (60-80%/60%), stick_stand.png (40%/62%)",
  "text": null,
  "anim": "figures slide right, 1.2 s",
  "vo_cues": [
   10,
   10
  ]
 },
 {
  "n": 15,
  "in": 48.0,
  "out": 49.75,
  "visual": "Party planned, nobody came: empty chairs.",
  "assets": "png_chair.png x4 (30-70%/68%), png_balloon_deflated.png (45%/30%)",
  "text": null,
  "anim": "fade 0.3 s",
  "vo_cues": [
   10,
   11
  ]
 },
 {
  "n": 16,
  "in": 49.833,
  "out": 52.833,
  "visual": "Empty beat / big cross-out.",
  "assets": "png_cross_red.png (50%/50%, scale 1.6)",
  "text": null,
  "anim": "scale-in 0.3 s",
  "vo_cues": [
   11,
   11
  ]
 },
 {
  "n": 17,
  "in": 52.917,
  "out": 55.583,
  "visual": "Brain diagram with gears.",
  "assets": "png_brain.png (50%/50%), png_gear.png x2 (40/60%/45%)",
  "text": null,
  "anim": "gears fade 0.3 s",
  "vo_cues": [
   11,
   12
  ]
 },
 {
  "n": 18,
  "in": 55.667,
  "out": 59.667,
  "visual": "Expectation icons drop away one by one.",
  "assets": "png_bubble_small.png x5 (30-70%/35%), png_cross_red.png x5",
  "text": null,
  "anim": "each bubble fades out then cross pops, 0.5 s apart",
  "vo_cues": [
   12,
   13
  ]
 },
 {
  "n": 19,
  "in": 59.75,
  "out": 64.25,
  "visual": "Concept label written next to the brain.",
  "assets": "png_brain.png (40%/50%)",
  "text": "learned helplessness",
  "anim": "type-on 22 cps, underline sweep after",
  "vo_cues": [
   12,
   13
  ]
 },
 {
  "n": 20,
  "in": 64.333,
  "out": 69.333,
  "visual": "Text stamp; figure stands still.",
  "assets": "stick_stand.png (45%/62%)",
  "text": "no more expectations",
  "anim": "type-on 24 cps",
  "vo_cues": [
   13,
   13
  ]
 },
 {
  "n": 21,
  "in": 69.417,
  "out": 73.75,
  "visual": "Flat ordinary-day beat.",
  "assets": "png_calendar.png (60%/45%)",
  "text": "Birthday? Who cares?",
  "anim": "type-on 24 cps",
  "vo_cues": [
   14,
   15
  ]
 },
 {
  "n": 22,
  "in": 73.833,
  "out": 76.25,
  "visual": "Fast wipe transition.",
  "assets": "—",
  "text": null,
  "anim": "white-flash 4 frames + 6-frame slide wipe",
  "vo_cues": [
   15,
   15
  ]
 },
 {
  "n": 23,
  "in": 76.333,
  "out": 80.417,
  "visual": "Fast-paced life: figure runs between work/college/family icons.",
  "assets": "stick_run.png (40%/62%), png_office.png (20%/40%), png_college.png (75%/40%), png_family.png (55%/28%)",
  "text": "Fast-Paced Life",
  "anim": "type-on 24 cps at 60%/18%",
  "vo_cues": [
   15,
   16
  ]
 },
 {
  "n": 24,
  "in": 80.5,
  "out": 86.917,
  "visual": "Run cycle past stacked books, family, laptop.",
  "assets": "stick_run.png (35%/62%), png_book_stack.png (20%/70%), png_family.png (65%/30%), png_laptop.png (80%/65%)",
  "text": null,
  "anim": "icons fade 0.25 s as figure passes",
  "vo_cues": [
   16,
   17
  ]
 },
 {
  "n": 25,
  "in": 87.0,
  "out": 93.833,
  "visual": "Figure buried behind laptop at desk.",
  "assets": "stick_laptop.png (50%/65%), png_desk.png (50%/75%), png_paper.png x3",
  "text": "Birthday? (f1027) then Birthday? F*** it (f1040)",
  "anim": "two-step type-on, 26 cps",
  "vo_cues": [
   17,
   17
  ]
 },
 {
  "n": 26,
  "in": 93.917,
  "out": 99.25,
  "visual": "Overload: meetings, assignments, calls pile on.",
  "assets": "png_meeting.png, png_assignment.png, png_call.png stacked (45-65%/25-40%)",
  "text": null,
  "anim": "each drops in from above 0.25 s, 0.5 s apart",
  "vo_cues": [
   18,
   19
  ]
 },
 {
  "n": 27,
  "in": 99.333,
  "out": 105.667,
  "visual": "Clock / melting time.",
  "assets": "png_clock.png (55%/35%), png_melt.png (55%/45%)",
  "text": "TIME BLINDNESS",
  "anim": "type-on 22 cps, all caps",
  "vo_cues": [
   20,
   20
  ]
 },
 {
  "n": 28,
  "in": 105.75,
  "out": 111.167,
  "visual": "Figure shrugs it off.",
  "assets": "stick_shrug.png (48%/62%)",
  "text": "No Biggie",
  "anim": "type-on 24 cps",
  "vo_cues": [
   21,
   21
  ]
 },
 {
  "n": 29,
  "in": 111.25,
  "out": 118.083,
  "visual": "Treadmill / survive-not-enjoy.",
  "assets": "stick_run.png (45%/62%), png_treadmill.png (45%/78%), png_gift.png (70%/40%)",
  "text": null,
  "anim": "fade 0.3 s",
  "vo_cues": [
   21,
   22
  ]
 },
 {
  "n": 30,
  "in": 118.167,
  "out": 120.75,
  "visual": "Quick labels one after another.",
  "assets": "stick_stand.png (35%/62%)",
  "text": "So what? (f1354) / not loneliness (f1425)",
  "anim": "type-on 26 cps each, 0.7 s apart",
  "vo_cues": [
   23,
   24
  ]
 },
 {
  "n": 31,
  "in": 120.833,
  "out": 123.417,
  "visual": "Second label lands, calm title beat starts.",
  "assets": "stick_confident.png (45%/62%)",
  "text": "not Stress (f1441) -> Emotional Maturity (f1451)",
  "anim": "type-on 26 cps",
  "vo_cues": [
   24,
   24
  ]
 },
 {
  "n": 32,
  "in": 123.5,
  "out": 128.25,
  "visual": "Calm held title frame.",
  "assets": "stick_confident.png (45%/62%)",
  "text": "Emotional Maturity (hold)",
  "anim": "no change - hold the frame",
  "vo_cues": [
   24,
   25
  ]
 },
 {
  "n": 33,
  "in": 128.333,
  "out": 131.917,
  "visual": "Birthday banner over the figure.",
  "assets": "png_banner_birthday.png (50%/20%), stick_stand.png (50%/62%)",
  "text": "HAPPY BIRTHDAY (banner art)",
  "anim": "banner drops 0.3 s",
  "vo_cues": [
   26,
   26
  ]
 },
 {
  "n": 34,
  "in": 132.0,
  "out": 140.583,
  "visual": "Contrast: chasing parties, attention, gifts; mood swings.",
  "assets": "stick_happy.png <-> stick_sad.png (50%/62%), png_party_hat.png, png_gift.png, png_phone.png",
  "text": "Emotionally Unstable",
  "anim": "type-on 22 cps",
  "vo_cues": [
   24,
   27
  ]
 },
 {
  "n": 35,
  "in": 140.667,
  "out": 142.417,
  "visual": "Transition into theory board.",
  "assets": "—",
  "text": null,
  "anim": "fade to background 0.3 s",
  "vo_cues": [
   27,
   28
  ]
 },
 {
  "n": 36,
  "in": 142.5,
  "out": 149.5,
  "visual": "Theory board written step by step.",
  "assets": "stick_stand.png (30%/62%)",
  "text": "Self-Determination Theory (f1711) / Control (f1766) / Growth (f1781) / Satisfaction (f1796)",
  "anim": "type-on 22 cps each",
  "vo_cues": [
   28,
   29
  ]
 },
 {
  "n": 37,
  "in": 149.583,
  "out": 152.083,
  "visual": "Figure stands confident.",
  "assets": "stick_confident.png (50%/62%)",
  "text": null,
  "anim": "fade 0.3 s",
  "vo_cues": [
   29,
   30
  ]
 },
 {
  "n": 38,
  "in": 152.167,
  "out": 156.167,
  "visual": "Cake offered, refused - red no-symbol over cake.",
  "assets": "stick_offer_cake.png (60%/62%), png_cross_red.png over png_cake.png (60%/55%)",
  "text": "red 'no' over cake (f1827)",
  "anim": "cake slides in 0.4 s, cross pops 0.2 s",
  "vo_cues": [
   30,
   30
  ]
 },
 {
  "n": 39,
  "in": 156.25,
  "out": 161.333,
  "visual": "Stairs to a crown on a pedestal; mirror appears.",
  "assets": "png_stairs.png (55%/70%), png_crown.png (55%/35%), png_mirror.png (35%/55%)",
  "text": null,
  "anim": "crown fades 0.3 s, mirror 0.3 s at f1903",
  "vo_cues": [
   31,
   32
  ]
 },
 {
  "n": 40,
  "in": 161.417,
  "out": 163.583,
  "visual": "Mirror beat.",
  "assets": "png_mirror.png (50%/55%), stick_stand.png (62%/62%)",
  "text": "not only limited to BIRTHDAYS (f1938)",
  "anim": "type-on 24 cps",
  "vo_cues": [
   32,
   32
  ]
 },
 {
  "n": 41,
  "in": 163.667,
  "out": 170.083,
  "visual": "Mirror scene continues, side note on the right.",
  "assets": "png_mirror.png (35%/55%), stick_stand.png (48%/62%)",
  "text": "People who treat their Birthday as a NORMAL DAY (f1965)",
  "anim": "type-on 24 cps, right column 68%/30%",
  "vo_cues": [
   32,
   33
  ]
 },
 {
  "n": 42,
  "in": 170.167,
  "out": 174.167,
  "visual": "Crowd of phones surrounds the hero; purple spotlight.",
  "assets": "stick_stand.png (50%/62%), png_crowd_phone.png x6, png_spotlight_purple.png (50%/60%)",
  "text": null,
  "anim": "crowd fades 0.3 s, spotlight fades 0.4 s at f2053",
  "vo_cues": [
   33,
   34
  ]
 },
 {
  "n": 43,
  "in": 174.25,
  "out": 176.75,
  "visual": "Spotlight crossed out; figure left holding a heart.",
  "assets": "png_cross_red.png (50%/55%), png_heart.png (50%/70%)",
  "text": "red X over spotlight (f2055)",
  "anim": "cross scale-in 0.25 s",
  "vo_cues": [
   34,
   34
  ]
 },
 {
  "n": 44,
  "in": 176.833,
  "out": 183.25,
  "visual": "Figure smiles, red X cancels the idea.",
  "assets": "stick_stand.png (50%/62%), png_cross_red.png (50%/55%)",
  "text": "Never lonely ? (f2123)",
  "anim": "label type-on 24 cps, X pops 0.2 s",
  "vo_cues": [
   35,
   35
  ]
 },
 {
  "n": 45,
  "in": 183.333,
  "out": 190.417,
  "visual": "Figure sits; thought bubble grows and fills.",
  "assets": "stick_sit.png (45%/68%), png_bubble_thought.png (55%/30%)",
  "text": "Every day is special (f2229)",
  "anim": "bubble grows 0.5 s, text type-on 22 cps",
  "vo_cues": [
   36,
   36
  ]
 },
 {
  "n": 46,
  "in": 190.5,
  "out": 193.167,
  "visual": "Text types on while figure runs joyfully.",
  "assets": "stick_run_happy.png (50%/62%)",
  "text": "Every Day is a Celebration (f2287-f2295)",
  "anim": "type-on 26 cps",
  "vo_cues": [
   37,
   37
  ]
 },
 {
  "n": 47,
  "in": 193.25,
  "out": 200.917,
  "visual": "Crowd celebrates; cut to laptop figure; arrows label both.",
  "assets": "png_crowd_party.png (35%/60%), png_banner_birthday.png (35%/25%), stick_laptop.png (75%/60%), png_arrow.png x2",
  "text": "Birthday Celebration banner (f2341) / Your Psychology (f2399)",
  "anim": "arrows draw 0.3 s, label type-on 24 cps",
  "vo_cues": [
   37,
   39
  ]
 },
 {
  "n": 48,
  "in": 201.0,
  "out": 204.333,
  "visual": "Figure sits in a chair, label on the right.",
  "assets": "stick_sit_chair.png (45%/65%)",
  "text": "Past Pain (f2449)",
  "anim": "type-on 24 cps",
  "vo_cues": [
   39,
   39
  ]
 },
 {
  "n": 49,
  "in": 204.417,
  "out": 209.417,
  "visual": "Running figure settles into meditation at f2473.",
  "assets": "stick_run.png -> stick_meditate.png (50%/65%)",
  "text": null,
  "anim": "cut at f2473, no transition",
  "vo_cues": [
   40,
   40
  ]
 },
 {
  "n": 50,
  "in": 209.5,
  "out": 212.417,
  "visual": "Meditating figure, bubble fills.",
  "assets": "stick_meditate.png (45%/68%), png_bubble_thought.png (58%/30%)",
  "text": "It's not about the cake (f2515)",
  "anim": "type-on 22 cps",
  "vo_cues": [
   40,
   40
  ]
 },
 {
  "n": 51,
  "in": 212.5,
  "out": 214.833,
  "visual": "Bubble swaps to a new question.",
  "assets": "png_bubble_thought.png (58%/30%)",
  "text": "What are you celebrating? (f2551)",
  "anim": "type-on 24 cps",
  "vo_cues": [
   40,
   41
  ]
 },
 {
  "n": 52,
  "in": 214.917,
  "out": 217.583,
  "visual": "Bubble swaps again.",
  "assets": "png_bubble_thought.png (58%/30%)",
  "text": "Outside validation? (f2580)",
  "anim": "type-on 24 cps",
  "vo_cues": [
   41,
   41
  ]
 },
 {
  "n": 53,
  "in": 217.667,
  "out": 221.917,
  "visual": "Mirror scene: figure looks at itself.",
  "assets": "png_mirror.png (40%/55%), stick_stand.png (55%/62%)",
  "text": "or inner peace ? (f2596)",
  "anim": "type-on 24 cps, mirror fade 0.3 s",
  "vo_cues": [
   41,
   41
  ]
 },
 {
  "n": 54,
  "in": 222.0,
  "out": 226.167,
  "visual": "Red arrow sticker points up at the figure.",
  "assets": "png_arrow_red.png (62%/40%), stick_stand.png (50%/62%)",
  "text": "LIKE & SUBSCRIBE (f2665)",
  "anim": "arrow pops 0.2 s, text type-on 24 cps",
  "vo_cues": [
   43,
   43
  ]
 },
 {
  "n": 55,
  "in": 226.25,
  "out": 231.25,
  "visual": "Logo: bulb over head + glasses.",
  "assets": "png_logo_stick.png (45%/55%), png_bulb.png (45%/35%)",
  "text": "Gyaani Stick (logo art)",
  "anim": "logo fades 0.4 s, bulb glows on 0.3 s",
  "vo_cues": [
   43,
   44
  ]
 },
 {
  "n": 56,
  "in": 231.333,
  "out": 244.083,
  "visual": "End card builds in three steps and holds.",
  "assets": "png_logo_stick.png (30%/55%), png_checklist.png (62%/45%)",
  "text": "Choose your Category: (f2741) / Birthday Celebration (f2759) / Normal Day (f2780)",
  "anim": "three-step type-on 22 cps, then hold 12 s",
  "vo_cues": [
   44,
   46
  ]
 }
]
```
