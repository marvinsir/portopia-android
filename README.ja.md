# RetroCase · v0.1.1 Prototype

[English](README.md) · **日本語** · [繁體中文](README.zh-Hant.md)

RetroCaseは、Androidネイティブの推理ゲームエンジンです。体験シナリオ「**消えた手帳**」では、事務室から消えた手帳の行方を、古い切符を手がかりに追います。**原作のゲームデータは未収録です。** 収録のシナリオ、テキスト、線画は本プロトタイプ用のオリジナルです。

[**告知ポスター**](docs/RetroCase-v0.1.1-Poster.png)

## インストール

[GitHub Releases](https://github.com/marvinsir/portopia-android/releases/tag/v0.1.1) から `RetroCase-v0.1.1-Prototype.apk` をダウンロードし、Android 6.0以降の端末で開いてください。APKを開いたアプリに対して、インストールの許可が求められる場合があります。PCからは `adb install RetroCase-v0.1.1-Prototype.apk` でインストールできます。

APKは手動でインストールできるプロトタイプ版です。署名証明書はビルド環境によって異なる場合があります。上書き更新には既存アプリと同じ証明書が必要です。

## 遊び方

- **言語**メニューで日本語、繁体字中国語、英語、簡体字中国語を切り替えられます。選択した言語は保存されます。
- 現場を調べ、手がかりを見つけると新しい行動が選べるようになります。操作のたびに進行状況を自動保存します。
- オフラインで、縦画面と横画面の両方に対応します。**読込**では4言語のJSONシナリオを読み込めます。現在のシナリオと進行状況を置き換える前に確認が表示されます。

## ビルド

JDK 17、Gradle 8.9、Android SDK Platform 35を使用します。

```sh
python3 tools/validate_case.py
gradle :app:assembleDebug
```

APKの出力先は `app/build/outputs/apk/debug/app-debug.apk` です。[GitHub Actions](https://github.com/marvinsir/portopia-android/actions) でもAPKをビルドして署名を検証し、成果物として保存します。シナリオの書式は [demo.json](app/src/main/assets/demo.json) を参照してください。
