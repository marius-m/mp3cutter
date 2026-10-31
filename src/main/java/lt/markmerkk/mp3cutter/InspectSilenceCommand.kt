package lt.markmerkk.mp3cutter

import com.google.common.base.Preconditions
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.cli.required
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Duration

/**
 * Detects silences in the input track and generates a map file, usable by 'cut' command
 */
@OptIn(ExperimentalCli::class)
class InspectSilenceCommand : Subcommand(
    name = "inspect-silence",
    actionDescription = "Generate a map file from silences between tracks",
) {
    private val inputFilePathTrack by option(
        ArgType.String,
        fullName = "track",
        shortName = "t",
        description = "Input track file to inspect for silences",
    ).required()
    private val outputFilePathMap by option(
        ArgType.String,
        fullName = "output",
        shortName = "o",
        description = "Output map file to write. Default: map is only printed out",
    )
    private val noise by option(
        ArgType.String,
        fullName = "noise",
        description = "Volume below which audio is considered silence",
    ).default(SilenceDetector.DEFAULT_NOISE)
    private val minSilenceSeconds by option(
        ArgType.Double,
        fullName = "min-silence",
        description = "Minimum silence length in seconds to count as a gap between tracks",
    ).default(SilenceDetector.DEFAULT_MIN_SILENCE.toMillis() / 1000.0)
    private val minTrackSeconds by option(
        ArgType.Int,
        fullName = "min-track",
        description = "Minimum track length in seconds, shorter parts are dropped",
    ).default(SilenceTracklistGenerator.DEFAULT_MIN_TRACK_LENGTH.toSeconds().toInt())
    private val overrideFfmpegRootPath by option(
        ArgType.String,
        fullName = "override-ffmpeg-path",
        shortName = "ffmpeg",
        description = "Directory containing ffmpeg and ffprobe. Default: resolved from PATH or common install locations",
    )

    override fun execute() {
        val inputFileTrack = File(inputFilePathTrack)
        Preconditions.checkArgument(
            inputFileTrack.isFile,
            "No track file provided (%s)".format(inputFileTrack.absolutePath),
        )
        val outputFileMap = outputFilePathMap?.let { File(it) }
        Preconditions.checkArgument(
            outputFileMap == null || !outputFileMap.exists(),
            "Output map file already exists, will not override it (%s)".format(outputFileMap?.absolutePath),
        )
        val detector = SilenceDetector(
            overrideFfmpegRootPath = overrideFfmpegRootPath,
            noise = noise,
            minSilence = Duration.ofMillis(Math.round(minSilenceSeconds * 1000)),
            minTrackLength = Duration.ofSeconds(minTrackSeconds.toLong()),
        )
        val segments = detector.detect(inputFileTrack)
        val mapText = TracklistFormatter().format(
            segments = segments,
            artist = inputFileTrack.nameWithoutExtension,
        )
        l.info("Found {} tracks", segments.size)
        println(mapText)
        if (outputFileMap != null) {
            FileUtils.writeStringToFile(outputFileMap, mapText + NameParser.DEFAULT_NEXT_LINE, Charsets.UTF_8)
            l.info("Map file written to {}", outputFileMap.absolutePath)
        }
    }

    companion object {
        private val l = LoggerFactory.getLogger(InspectSilenceCommand::class.java)
    }
}
