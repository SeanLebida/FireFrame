# Hubble and NASA pictures on the home backdrop

## What changed

Version 1.1.7. While the home screen is up, FireFrame checks once an hour for a new picture and saves one at most every 12 hours. It alternates a recent ESA/Hubble image with a recent NASA image of the day, stores it as the custom wallpaper, and shows a short credit.

## Why it mattered

The backdrop was one of five bundled stills. The latest telescope pictures never reached the dock.

## Gotchas / pitfalls

The download runs on the television, so this computer does not have to stay on. A failed download leaves the current picture in place and tries again on the next hourly check. Pictures larger than 12 MB are skipped. The personal dock list is not in this repo. The same signing key is required for adb install -r.

## Verification

Installed 1.1.7 on the Fire TV. A capture after install showed a star-cluster picture behind the same dock, in place of the previous Earth-horizon still. The feeds are https://esahubble.org/images/feed/ and https://www.nasa.gov/feeds/iotd-feed.
