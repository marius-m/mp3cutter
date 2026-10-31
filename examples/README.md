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
