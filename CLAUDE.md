# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

`mp3cutter` is a Kotlin CLI that splits one long audio/video file into many smaller files, driven by a text "map" file listing cut points. The actual cutting is delegated to the system `ffmpeg`/`ffprobe` binaries via the `net.bramp.ffmpeg` wrapper, so ffmpeg must be installed locally.

## Commands

Gradle 8.10 wrapper (can itself run on JDK 8–23, e.g. the default JDK 21), Kotlin 1.9.25. Code targets Java 11 via `kotlin { jvmToolchain(11) }`; Gradle picks up (or provisions) a JDK 11 for compiling, testing and `run`.

```sh
./gradlew build                     # compile + test
./gradlew test                      # all tests (JUnit Platform)
./gradlew test --tests 'lt.markmerkk.mp3cutter.NameParserParseTest'          # single class
./gradlew test --tests 'lt.markmerkk.mp3cutter.NameParserParseTest.<method>'   # single method (wildcards allowed)
./gradlew run --args='-m map.txt -t input.mp3 -o outdir [-ext mp3|mp4] [-ffmpeg /path/to/bin] [-d]'
./gradlew installDist               # runnable script in build/install/mp3cutter/bin
```

CLI flags (see `Main.kt`): `--map/-m` cut map file, `--track/-t` input media, `--output/-o` existing output dir, `--export-format/-ext`, `--override-ffmpeg-path/-ffmpeg` (directory containing `ffmpeg` and `ffprobe`; when omitted, `FfmpegResolver` searches `PATH`, then OS-specific install dirs), `--dry-run/-d` (parse and print tracks only).

Note: `ExportFormat.fromRaw(null)` returns `INVALID`, so omitting `-ext` currently fails on a non-dry run despite the help text saying the default is mp3.

`Mp3CutterIntegrationTest` is not ignored: it invokes the real ffmpeg (resolved via `FfmpegResolver`) and writes output files into `build/`. It fails on machines without ffmpeg installed; use `--tests` to run only unit tests in that case.

## Layout quirks

- Kotlin **main** sources live under `src/main/java/` (not `src/main/kotlin/`); tests are under `src/test/kotlin/`.
- Plain classpath project: no `module-info.java` / JPMS (an earlier module setup was deliberately removed; don't reintroduce it).
- Logging: java.util.logging is bridged to SLF4J/Logback in `main()`; config in `src/main/resources/logback.xml`.

## Architecture / data flow

1. **`Main`** parses args (kotlinx-cli), reads the map file, calls `NameParser`, logs the parsed tracks, and (unless dry-run) runs `Mp3Cutter`.
2. **`NameParser`** turns map text into `List<TrackItem>`:
   - Each line matches `^<time>( <endTime>)? <artist><sep><track>$` (separator default `-`). Lines that don't match, or whose end time precedes start, are silently dropped.
   - Times are parsed by `TimeUtils`, which tries `TimeParser` implementations in order (`TimeParserHours` = `H:mm:ss`, then `TimeParserMinutesOnly` = `m:ss`) and returns a `LocalTime` used as an offset from the start of the file.
   - Artist/track are sanitized to `[A-Za-z0-9(),.'" ]` since `"$artist - $track"` becomes the output filename. Duplicate names throw.
   - Track duration: explicit end time if given, else the next line's start. The final line with no end becomes `TrackItemLast` (sealed `TrackItem` hierarchy in `entities/TrackItemRegular.kt`).
3. **`Mp3Cutter`** probes the input with ffprobe, converts `TrackItemLast` into a `TrackItemRegular` using the probed total duration, then builds one `FFmpegJob` per track:
   - `-ss`/`-t` from start offset and duration; output `<outputDir>/<name>.<format>`.
   - Fade in/out audio filters (`afade`) derived from `TrackFilterStart`/`TrackFilterEnd` (default 2s, skipped for short tracks).
   - For `ExportFormat.MP4`, also a video filter: `drawtext` overlay of the track name plus video `fade` in/out.
   - Jobs run sequentially.

## Tests

JUnit 5 + kotlin-test, AssertJ, MockK. Test resources in `src/test/resources/` include sample map files (`*-tracks.txt`) paired with sample media (`.mp3`, `.mp4`); `TestUtils` loads them from the classpath.
