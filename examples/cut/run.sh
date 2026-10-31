#!/usr/bin/env bash
# Example: cut 'sample.mp3' into tracks listed in 'tracks.txt'
# Uses the built binary, build it first with: ./gradlew installDist
# Run from anywhere: examples/cut/run.sh
set -euo pipefail

# --- Values (edit these) ---
MAP="tracks.txt"          # cut map, relative to this folder
TRACK="sample.mp3"        # input media, relative to this folder
OUTPUT="out"              # output folder, relative to this folder, created if missing
EXPORT_FORMAT="mp3"       # mp3 | mp4
DRY_RUN="false"           # true = only parse and print tracks
FFMPEG_DIR=""             # directory with ffmpeg + ffprobe, empty = find automatically

# --- Run ---
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
BIN="$ROOT_DIR/build/install/mp3cutter/bin/mp3cutter"

if [[ ! -x "$BIN" ]]; then
    echo "No built binary at $BIN" >&2
    echo "Build it first: (cd \"$ROOT_DIR\" && ./gradlew installDist)" >&2
    exit 1
fi

mkdir -p "$SCRIPT_DIR/$OUTPUT"

ARGS=(
    cut
    -m "$SCRIPT_DIR/$MAP"
    -t "$SCRIPT_DIR/$TRACK"
    -o "$SCRIPT_DIR/$OUTPUT"
    -ext "$EXPORT_FORMAT"
)
if [[ "$DRY_RUN" == "true" ]]; then
    ARGS+=(-d)
fi
if [[ -n "$FFMPEG_DIR" ]]; then
    ARGS+=(-ffmpeg "$FFMPEG_DIR")
fi

"$BIN" "${ARGS[@]}"
