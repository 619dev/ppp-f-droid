#!/bin/sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$project_dir"

sh scripts/check-fdroid.sh
npm ci --ignore-scripts
npm run build
npx --no-install cap sync android
sh scripts/check-fdroid.sh

cd android
./gradlew --no-daemon assembleRelease
