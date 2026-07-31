#!/bin/sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$project_dir"

for forbidden in \
  google-services.json \
  paperphone-release.keystore \
  OneSignalSDKWorker.js
do
  if find . -path './node_modules' -prune -o -path './android/.gradle' -prune -o \
    -path './android/app/build' -prune -o -name "$forbidden" -print | grep -q .
  then
    echo "F-Droid check failed: forbidden file found: $forbidden" >&2
    exit 1
  fi
done

if grep -n -E \
  'com\.google\.gms|firebase|onesignal|cdn\.onesignal\.com|fonts\.googleapis\.com|storePassword|keyPassword' \
  package.json package-lock.json index.html capacitor.config.ts \
  android/build.gradle android/app/build.gradle android/app/capacitor.build.gradle \
  android/app/src/main/AndroidManifest.xml
then
  echo "F-Droid check failed: proprietary service or signing secret reference found" >&2
  exit 1
fi

echo "F-Droid source checks passed"
