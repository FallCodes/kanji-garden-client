# Client for Kanji Garden

A minimal, unofficial Android app for [kanji.garden](https://kanji.garden/). It opens the site's mobile interface full-screen and keeps you signed in.

> [!IMPORTANT]
> **This is an unofficial project.** It is not affiliated with, endorsed by, or supported by Kanji Garden or its developer. "Kanji Garden" and all content shown in the app belong to their respective owners. The app contains none of the site's code or content: it only loads `https://kanji.garden/` in a WebView. You need your own Kanji Garden account, plus a subscription where the site requires one.

## Why

The official Kanji Garden Android app is no longer available, but the website has a good mobile interface. Using it in a regular browser means signing in again every time you clear your browsing data. This app keeps the site in its own private storage, separate from your browser, so your session survives.

## Features

- Stays signed in: the session lives in the app's private storage, not in your browser
- Full-screen, with no browser toolbars
- The back button navigates within the site
- Rotating the screen doesn't reload the page, so you don't lose your place mid-review
- Links you tap to other websites open in your regular browser
- Follows the system light/dark theme for the window background

## Privacy

The app itself collects nothing. It has no analytics, no ads and no tracking, and it only requests the `INTERNET` permission. It shows kanji.garden and nothing else. For how the site handles your data, see the [Kanji Garden privacy policy](https://kanji.garden/privacy).

## Install

Download the APK from the [Releases](../../releases) page and open it on your phone. Android will ask you to allow installs from that source.

Requires Android 8.0 (API 26) or newer.

## Build

Requirements: JDK 17 or newer and the Android SDK with platform 36.

```bash
echo "sdk.dir=$HOME/Android/Sdk" > local.properties   # adjust to your SDK path
./gradlew assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```

The release build is signed with your local debug key so it can be sideloaded directly. If you distribute your own builds, set up your own release keystore.

## Support

This repository only covers the app wrapper. For problems with Kanji Garden itself (your account, reviews, subscriptions), contact the Kanji Garden developer through the site's [support page](https://kanji.garden/support).

## License

[MIT](LICENSE) © 2026 FallCodes
