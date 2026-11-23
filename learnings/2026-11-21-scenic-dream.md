# Scenic daydream

## What changed
- Added `ScenicDream` (`android.service.dreams.DreamService`) and `res/xml/scenic_dream.xml`. It shows twelve bundled `assets/screensaver/*.jpg` stills full screen (Yosemite, Grand Canyon, Monument Valley, Zion, the Tetons, Big Sur, a redwood grove, Crater Lake, Acadia, Denali, White Sands, the Great Smoky Mountains), captioned from the filename, advancing every 20 seconds. No network. The stills are original black-and-white photographs in a large-format landscape style, not scans of Ansel Adams prints.
- `HomeService.onDreamStarted` / `onDreamStopped` hide the home overlay while the dream is up and show it again unless `paused()` or `searchResults`. `cover()` and `show()` no-op while `dreamShowing`.
- Manifest `versionCode` 11, `versionName` 1.1.8. Service is exported with `BIND_DREAM_SERVICE` and the dream intent filter. Same process as the app.

## Why it mattered
- FireFrame's overlay would otherwise paint over a daydream. The dream needs the cover down for the stills, then the cover back when the dream ends, without changing Home, Search, or voice-results key handling.

## Gotchas / pitfalls
- Empty or missing `assets/screensaver` stays black. Do not spin.
- Recycle only the bitmap that is no longer on the `ImageView`. `inSampleSize` targets about a 1920 long edge. Do not fade the `ImageView` alpha. On this set the dream window dropped the animation and left the photograph transparent over black while the caption stayed bright.
- `d8` from build-tools 34 can NPE on anonymous classes; `build.sh` retries with build-tools 35. Java 8 only.
- The parent installs the apk and checks it on the TV. Do not run adb from this change.

## Verification
- `./build.sh` with `ANDROID_HOME=/home/vic/Android/Sdk` and the existing keystore. Signed apk copied to `/home/vic/.local/share/fireframe/fireframe.apk` on success.
- Parent installs and checks the dream on the TV.
