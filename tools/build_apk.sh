#!/usr/bin/env bash
set -euo pipefail

output="$(realpath -m "${1:?Usage: bash tools/build_apk.sh OUTPUT.apk}")"
repo="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
tools="${ANDROID_HOME:?Set ANDROID_HOME to the Android SDK}/build-tools/35.0.0"
platform="$ANDROID_HOME/platforms/android-35/android.jar"
source_dir="$repo/app/src/main"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

# AGP normally adds the application ID when it merges the source manifest.
python3 - "$source_dir/AndroidManifest.xml" "$tmp/AndroidManifest.xml" <<'PY'
from sys import argv
from xml.etree import ElementTree

ElementTree.register_namespace("android", "http://schemas.android.com/apk/res/android")
manifest = ElementTree.parse(argv[1])
manifest.getroot().set("package", "org.retrocase.engine")
manifest.write(argv[2], encoding="utf-8", xml_declaration=True)
PY

mkdir -p "$tmp/classes" "$tmp/dex" "$(dirname "$output")"
mapfile -d '' sources < <(find "$source_dir/java" -name '*.java' -print0 | sort -z)
javac --release 17 -encoding UTF-8 -cp "$platform" -d "$tmp/classes" "${sources[@]}"
jar --create --file "$tmp/classes.jar" -C "$tmp/classes" .
"$tools/d8" --release --min-api 23 --lib "$platform" --output "$tmp/dex" "$tmp/classes.jar"
"$tools/aapt2" link -I "$platform" --manifest "$tmp/AndroidManifest.xml" \
  --min-sdk-version 23 --target-sdk-version 35 --version-code 2 \
  --version-name '0.1.1 Prototype' -A "$source_dir/assets" -o "$tmp/unaligned.apk"
zip -q -j "$tmp/unaligned.apk" "$tmp/dex/"*.dex
"$tools/zipalign" -f 4 "$tmp/unaligned.apk" "$tmp/aligned.apk"

keytool -genkeypair -keystore "$tmp/prototype.jks" -storepass android -keypass android \
  -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 \
  -dname 'CN=RetroCase Prototype,O=RetroCase,C=US'
"$tools/apksigner" sign --ks "$tmp/prototype.jks" --ks-key-alias androiddebugkey \
  --ks-pass pass:android --key-pass pass:android --out "$output" "$tmp/aligned.apk"
