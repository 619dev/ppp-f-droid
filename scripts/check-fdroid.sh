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

if grep -R -n -E \
  'com\.google\.gms|com\.google\.firebase|firebase|onesignal|@capacitor/push-notifications|capacitor-push-notifications|fonts\.googleapis\.com|fonts\.gstatic\.com|storePassword|keyPassword' \
  package.json package-lock.json index.html capacitor.config.ts \
  src \
  public \
  android/build.gradle android/settings.gradle android/capacitor.settings.gradle \
  android/app/build.gradle android/app/capacitor.build.gradle \
  android/app/src/main/AndroidManifest.xml android/app/src/main/java
then
  echo "F-Droid check failed: proprietary service or signing secret reference found" >&2
  exit 1
fi

echo "F-Droid source checks passed"
