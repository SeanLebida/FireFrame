# Ubuntu on the photographs

## What changed

Version 1.1.11. Photo captions, the clock, dock labels, and menu titles use the bundled Ubuntu Medium and Ubuntu Bold faces. The screensaver caption uses Ubuntu Medium. Inter is no longer in the app.

## Why it mattered

The home caption was Inter Medium. The screensaver caption set no typeface, so Fire OS drew it in the system font. The text on the photographs should be Ubuntu.

## Gotchas / pitfalls

- Load the faces from `app/assets/fonts`. Fire OS does not ship Ubuntu.
- Ubuntu Medium is the weight used where Inter Medium was. Ubuntu Bold replaces Inter SemiBold. Ubuntu has no SemiBold file in this set.
- Keep `Ubuntu-UFL.txt` with the font files. The Ubuntu Font Licence travels with the fonts.

## Verification

- `fc-query` on the bundled files reports Ubuntu Medium and Ubuntu Bold.
- Parent builds and installs 1.1.11. A home or dream capture should show the Ubuntu caption.
