# RetroCase · v0.1.1 Prototype

**English** · [日本語](README.ja.md) · [繁體中文](README.zh-Hant.md)

RetroCase is a native Android mystery engine. Start with **The Missing Notebook**, an independent demo where an old railway ticket may help you find a notebook that vanished from an office. **Original game data is not included.** The prototype ships with its own scenario, dialogue and line art.

[**Promotional poster**](docs/RetroCase-v0.1.1-Poster.png)

## Install

Download `RetroCase-v0.1.1-Prototype.apk` from [GitHub Releases](https://github.com/marvinsir/portopia-android/releases/tag/v0.1.1) and open it on Android 6.0 or later. Android may ask you to allow installation from the app you opened the APK with. To install from a computer, run `adb install RetroCase-v0.1.1-Prototype.apk`.

The APK is a sideloadable prototype build. The prototype signing certificate can differ between build environments. An update over an existing install requires the same certificate.

## Play

- Switch between Japanese, Traditional Chinese, English and Simplified Chinese from **Language**. The selected language is saved.
- Investigate scenes, unlock actions with clues, and follow the case to its conclusion. The app saves progress after each action.
- Play offline in portrait or landscape. **Import** loads a four-language JSON case after confirming replacement of the current case and progress.

## Build

Use JDK 17, Gradle 8.9, and Android SDK platform 35.

```sh
python3 tools/validate_case.py
gradle :app:assembleDebug
```

Gradle writes `app/build/outputs/apk/debug/app-debug.apk`. [GitHub Actions](https://github.com/marvinsir/portopia-android/actions) builds the APK, verifies its signature, and attaches it as a workflow artifact.

For a case file example, see [demo.json](app/src/main/assets/demo.json). Schema 1 uses `start` and `scenes`, with localized `title`, `text`, action `label`, and optional `reply` objects keyed by `ja`, `zh-Hant`, `en`, and `zh-Hans`. Actions can use `requires`, `forbids`, `set`, `clear`, and `goto`. A scene with no actions is an ending. Import is for JSON case files.
