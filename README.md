# CMF Dial Controller — Android prototype

## What this first prototype does

- CMF Smart Dial configured to volume:
  - Volume UP -> scroll down
  - Volume DOWN -> scroll up
- Restores the previous media volume after detecting the dial movement.
- Provides test buttons for scroll up/down.
- Installs an Accessibility Service capable of performing gestures and clicking accessible UI elements.
- Listens for media-key events as a starting point for single/double/long press mapping.

## Build

Open this folder in Android Studio (recent version with Android Gradle Plugin 8.7.x support), let Gradle sync, then Run on an Android phone.

## Setup

1. Install the APK on the Android phone.
2. Open CMF/Nothing X and configure the Smart Dial rotation to control media volume.
3. Open "CMF Dial Controller".
4. Tap "Open Accessibility Settings".
5. Enable "CMF Dial Controller".
6. Open Brave and the target webpage.
7. Test the in-app Scroll Up/Down buttons.
8. Rotate the Smart Dial and verify that Brave scrolls.

## Important

Android and different phone vendors can restrict volume observation/background behavior. If the dial movement does not reach the app, the next prototype should capture the headset/media key events exposed by the device instead.

The exact labels exposed by a webpage to Android Accessibility vary. If "Next Chapter" is not found, the app will need either a different accessible label or a coordinate-based fallback.

This is a prototype for your own device and browser session; it does not bypass website access controls.
