# RetroCase · v0.1.1 Prototype

[English](README.md) · [日本語](README.ja.md) · **繁體中文**

RetroCase 是原生 Android 推理遊戲引擎。內建獨立試玩案件「**消失的筆記本**」，從辦公室的一張舊車票查起，找出筆記本的下落。**尚未收錄原版遊戲資料。** 本原型內附的劇情、對白與線稿均為原創。

[**宣傳海報**](docs/RetroCase-v0.1.1-Poster.png)

## 安裝

從 [GitHub Releases](https://github.com/marvinsir/portopia-android/releases/tag/v0.1.1) 下載 `RetroCase-v0.1.1-Prototype.apk`，在 Android 6.0 以上的裝置開啟。系統可能會要求允許開啟 APK 的應用程式安裝套件。也可以在電腦執行 `adb install RetroCase-v0.1.1-Prototype.apk`。

APK 為可手動安裝的原型版。不同建置環境可能使用不同簽章憑證。要覆蓋更新，必須使用與現有版本相同的憑證。

## 操作

- 透過**語言**選單切換日文、繁體中文、英文及簡體中文。語言設定會保留。
- 調查場景，根據線索解鎖行動，找出案件結局。每次操作後會自動存檔。
- 支援離線玩、直向及橫向畫面。**匯入**可載入四語 JSON 案件，取代現有案件及進度前會顯示確認視窗。

## 建置

使用 JDK 17、Gradle 8.9 和 Android SDK Platform 35。

```sh
python3 tools/validate_case.py
gradle :app:assembleDebug
```

APK 輸出至 `app/build/outputs/apk/debug/app-debug.apk`。[GitHub Actions](https://github.com/marvinsir/portopia-android/actions) 會建置 APK、驗證簽章並保存建置產物。案件格式範例請參考 [demo.json](app/src/main/assets/demo.json)。
