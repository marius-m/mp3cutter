#!/usr/bin/env bash
# Example: detect silences in 'sample.mp3' and generate a map file for 'cut'
# 'sample.mp3' is 1:49 long, 3 tones (35s each) separated by 2s of silence
# Uses the built binary, build it first with: ./gradlew installDist
# Run from anywhere: examples/inspect-silence/run.sh
set -euo pipefail

# --- Values (edit these) ---
TRACK="sample.mp3"        # input media, relative to this folder
OUTPUT="out/tracks.txt"   # map file to write, relative to this folder, replaced on each run
NOISE="-30dB"             # volume below which audio is considered silence
MIN_SILENCE="0.5"         # minimum silence length (seconds) to count as a gap between tracks
MIN_TRACK="30"            # minimum track length (seconds), shorter parts are dropped
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

# Command refuses to override an existing map file, previous example result is replaced
mkdir -p "$(dirname "$SCRIPT_DIR/$OUTPUT")"
rm -f "$SCRIPT_DIR/$OUTPUT"

ARGS=(
    inspect-silence
    -t "$SCRIPT_DIR/$TRACK"
    -o "$SCRIPT_DIR/$OUTPUT"
    --noise "$NOISE"
    --min-silence "$MIN_SILENCE"
    --min-track "$MIN_TRACK"
)
if [[ -n "$FFMPEG_DIR" ]]; then
    ARGS+=(-ffmpeg "$FFMPEG_DIR")
fi

"$BIN" "${ARGS[@]}"
