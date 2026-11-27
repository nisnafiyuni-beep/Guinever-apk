# Guinevere Auto Free — Android prototype source

This is an early Android Studio prototype for recognizing the `FREE` text beneath a hero card in the MCGG shop. It uses Android MediaProjection for screen capture, ML Kit Text Recognition for OCR, and an AccessibilityService gesture for tapping.

## Important prototype limitations
- This is source code, **not a compiled APK**. An Android SDK/Gradle build environment is required to compile it.
- The tap coordinate is a first estimate based on the supplied landscape screenshot. It must be calibrated on the actual device before use.
- Current prototype taps one detected `FREE` target and rescans. It intentionally does not tap several stale coordinates from one frame because the shop can shift after a selection. A production version should wait for the shop to settle, rescan, and continue until no `FREE` label remains.
- Enable the Accessibility service manually in Android Settings and approve screen capture when prompted. Android shows a persistent notification while screen capture is active.
- Use only where permitted by the game's rules. This project is not affiliated with or endorsed by MCGG/Moonton.
