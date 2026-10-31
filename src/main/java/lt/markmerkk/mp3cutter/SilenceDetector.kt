package lt.markmerkk.mp3cutter

import lt.markmerkk.mp3cutter.entities.TrackSegment
import net.bramp.ffmpeg.FFprobe
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Duration
import java.util.Locale

/**
 * Runs ffmpeg 'silencedetect' filter on the input and turns found silences into tracks
 * ffmpeg wrapper does not expose filter output, so ffmpeg is run as a separate process
 */
class SilenceDetector(
    private val overrideFfmpegRootPath: String? = null,
    private val noise: String = DEFAULT_NOISE,
    private val minSilence: Duration = DEFAULT_MIN_SILENCE,
    private val minTrackLength: Duration = SilenceTracklistGenerator.DEFAULT_MIN_TRACK_LENGTH,
) {

    private val ffmpegBinaries = FfmpegResolver().resolve(overrideFfmpegRootPath)
    private val ffprobe = FFprobe(ffmpegBinaries.ffprobe.absolutePath)

    /**
     * @return tracks found between silences or throw [IllegalStateException] if ffmpeg fails
     */
    @Throws(IllegalStateException::class)
    fun detect(inputFile: File): List<TrackSegment> {
        val totalDuration = probeDuration(inputFile)
        val rawOutput = runSilenceDetect(inputFile)
        val silences = SilenceIntervalsUtils.merge(
            SilenceLogParser().parse(rawText = rawOutput, totalDuration = totalDuration)
        )
        l.debug("Found silences: {}", silences)
        return SilenceTracklistGenerator(minTrackLength = minTrackLength)
            .generate(silences = silences, totalDuration = totalDuration)
    }

    private fun probeDuration(inputFile: File): Duration {
        val durationSeconds = ffprobe.probe(inputFile.absolutePath).format.duration
        return Duration.ofMillis(Math.round(durationSeconds * 1000))
    }

    private fun runSilenceDetect(inputFile: File): String {
        val filter = "silencedetect=noise=%s:d=%.3f"
            .format(Locale.ROOT, noise, minSilence.toMillis() / 1000.0)
        val command = listOf(
            ffmpegBinaries.ffmpeg.absolutePath,
            "-hide_banner",
            "-nostats",
            "-i", inputFile.absolutePath,
            "-vn",
            "-af", filter,
            "-f", "null",
            "-",
        )
        l.info("Detecting silence: {}", command.joinToString(" "))
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw IllegalStateException(
                "ffmpeg silence detection failed (exit code %d): %s"
                    .format(exitCode, output.lines().takeLast(5).joinToString("\n"))
            )
        }
        return output
    }

    companion object {
        private val l = LoggerFactory.getLogger(SilenceDetector::class.java)

        const val DEFAULT_NOISE = "-30dB"
        val DEFAULT_MIN_SILENCE: Duration = Duration.ofMillis(500)
    }
}
