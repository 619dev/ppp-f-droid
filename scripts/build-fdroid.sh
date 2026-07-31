#!/bin/sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$project_dir"

npm ci --ignore-scripts
npm run build
npx cap sync android

cd android
./gradlew --no-daemon assembleRelease
