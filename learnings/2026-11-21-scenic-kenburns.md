# Scenic Ken Burns motion

## What changed
- `ScenicDream` draws each screensaver jpg with `Canvas.concat(Matrix)` on a custom view instead of an `ImageView`.
- Even slides zoom 1.00 to 1.08 and odd slides zoom 1.08 to 1.00 over the existing 20s slide, with a small corner drift. The caption stays fixed. Advance is still a hard swap.
- `AndroidManifest.xml` is versionCode 12 / versionName 1.1.9.

## Why it mattered
- Motion is a matrix draw each frame because the dream window drops view alpha animations. A previous fade left the photograph transparent over black while the caption stayed bright. The bitmap stays fully opaque from the first frame.

## Gotchas / pitfalls
- Do not call `setAlpha(0)` or `View.animate()` / `ObjectAnimator` / `ValueAnimator` on the photo. The dream window can drop that animation.
- Pan is a fraction of the extra zoom overflow and is clamped so the bitmap still covers the view. Recycling the bitmap that `onDraw` still holds blanks the frame.
- `d8` from build-tools 34 can NPE on anonymous classes and still print a successful apk.

## Verification
- `./build.sh` printed `Built out/fireframe.apk` (5824 KB) after the usual build-tools 34 d8 NPE. Installed with `adb install -r` as versionName 1.1.9, versionCode 12. Dock prefs stayed.
- After a real input event and a 15s screen-off timeout, DreamManager ran `com.fireframe.home/.ScenicDream` with focus `Sys2023:dream`. The first frames were a bright Acadia coast and the caption, with no dock and no promo slide. Two captures 8 seconds apart differed (mean absolute difference about 32 on a 1920x1080 gray frame). `screen_off_timeout` was put back to 300000.
