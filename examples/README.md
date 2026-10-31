# Examples

Runnable examples of `mp3cutter` subcommands. Each folder has a `run.sh` with editable values at the top,
and can be run from anywhere (needs `ffmpeg` installed). Results are written into the example's `out/` folder.

Scripts use the built binary in `build/install/mp3cutter/bin/`, build it first (and again after code changes):

```sh
./gradlew installDist
```

- `cut/`: cuts `sample.mp3` (45s) into 3 tracks listed in `tracks.txt`

```sh
examples/cut/run.sh
ls examples/cut/out
```

  For video input set `EXPORT_FORMAT="mp4"`; add `GPU="amd"` to encode the video on an AMD GPU (VAAPI, needs `/dev/dri/renderD128`)

- `inspect-silence/`: detects silences in `sample.mp3` (1:49, 3 tones separated by 2s silence)
  and writes a map file with 3 tracks into `out/tracks.txt`

```sh
examples/inspect-silence/run.sh
cat examples/inspect-silence/out/tracks.txt
```

The generated map can be fed straight to `cut`, e.g. set `MAP="../inspect-silence/out/tracks.txt"`
and `TRACK="../inspect-silence/sample.mp3"` in `cut/run.sh`.
