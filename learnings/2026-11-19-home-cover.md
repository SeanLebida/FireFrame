# Sticky home cover

## What changed

Fork keeps the accessibility overlay up across Home. cover() no longer starts MainActivity. appShown() no longer hides the overlay. The overlay is shown on KEYCODE_HOME down (event still propagates), on both HOME_PRESSED broadcasts, and when the service connects if it is not paused. Version 1.1.5 (versionCode 8). Updater.LATEST points at SeanLebida/FireFrame. build.sh falls back to Android build-tools 35.0.0 when d8 34.0.0 crashes.

## Why it mattered

On this Fire OS 7 set, starting the dock activity from the cover is held for about five seconds, and hiding the overlay when the dock draws lets Amazon's home paint in that gap. The overlay has to stay opaque until some other real app is in front.

## Gotchas / pitfalls

Do not commit a personal dock list or the signing key. The key lives only at ~/.local/share/fireframe/key.jks. adb install -r keeps dock prefs only when signed with that same key. Do not uninstall. Do not disable com.amazon.tv.launcher. Do not write the tvconfig partition. d8 34.0.0 NPEs on anonymous classes. Changing the accessibility service XML can require the service to be toggled before key filtering is granted. No GitHub release is published, so the in-app updater fails closed. This branch is worktree-home-cover. Do not open an upstream PR. Do not commit on main.

## Verification

Signed APK built at out/fireframe.apk and copied to ~/.local/share/fireframe/fireframe.apk (mode 600). apksigner verify passed (v1, v2, and v3, one signer). aapt badging: package com.fireframe.home, versionCode 8, versionName 1.1.5. d8 34.0.0 NPE'd on HomeScreen$1.class; the script retried with build-tools 35.0.0 and finished.

Installed with adb install -r over the existing app. The certificate SHA-256 matched the installed 1.1.4 build, so the package was not uninstalled and the saved dock stayed. After install, dumpsys reported versionName 1.1.5, enabled_accessibility_services was com.fireframe.home/com.fireframe.home.HomeService, and accessibility_enabled was 1. The service capabilities value was 9, which includes key-event filtering.

One Home press was screen-recorded. Amazon's HomeActivity_vNext resumed underneath, and the focused window stayed com.fireframe.home. Across the decoded frames the top of the picture stayed dark. Sampled frames at the start and at 8 seconds were the FireFrame dock, not Amazon's home banner. A later still after reinstalling that same APK was the dock again.
