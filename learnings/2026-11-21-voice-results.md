# Voice search results stay visible

## What changed

Version 1.1.6. When Amazon's launcher opens `DirectiveSearchResultsActivityDI`, the home overlay hides and stays down. Home, or the launcher's own home activity coming forward, covers again.

## Why it mattered

The remote mic already opens Alexa. The results screen is part of `com.amazon.tv.launcher`, so the cover treated it as Amazon's home and replaced it with the FireFrame dock.

## Gotchas / pitfalls

`com.amazon.vizzini` is the microphone and stays enabled. `com.amazon.vizzini.ftvcds` stays disabled. The personal dock list is not in this repo. Same signing key required for `adb install -r`.

## Verification

Installed 1.1.6 on the Fire TV with the existing signing key. Accessibility stayed on. The shell is not allowed to open the results activity, so the mic press itself was not replayed. The results activity from the earlier voice searches was `com.amazon.tv.launcher/.deeplink.activity.DirectiveSearchResultsActivityDI`.
