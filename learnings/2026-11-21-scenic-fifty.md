# Scenic slide length and fifty stills

## What changed
- `ScenicDream.SLIDE_MS` is 60000, so each slide, including the Ken Burns zoom, lasts 60 seconds.
- `app/AndroidManifest.xml` is versionName 1.1.10 and versionCode 13.
- The parent is adding original black-and-white U.S. landscape jpgs under `app/assets/screensaver/` to reach 50 files. This change did not add or replace image files.

## Why it mattered
- Twenty seconds was too short for a still on the frame. Sixty seconds keeps one photograph on screen long enough to look at without changing the zoom amount or the matrix drawing.

## Gotchas / pitfalls
- Do not reintroduce `View.animate()`, alpha fades, or `setAlpha(0)`. The slide stays opaque and advances on `SLIDE_MS`.
- Zoom, matrix drawing, caption, and HomeService calls stay as they were.

## Verification
- `app/assets/screensaver/` has 50 jpgs, each 1920x1080. `./build.sh` printed `Built out/fireframe.apk` (19228 KB) after the usual build-tools 34 d8 NPE. `unzip -l` counted 50 `assets/screensaver/*.jpg`.
- `adb install -r` reported Success, versionName 1.1.10, versionCode 13. screensaver component settings stayed on ScenicDream. `screen_off_timeout` was put back to 300000.
- Tubi was in front during the install, and a screencap of that window is black, so the new dream was not checked on the panel. The earlier 1.1.9 dream path (Acadia Coast, Sys2023:dream) is unchanged apart from slide length and the asset list.
