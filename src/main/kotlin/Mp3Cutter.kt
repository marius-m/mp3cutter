import com.google.common.base.Preconditions
import entities.TrackItem
import entities.TrackItemLast
import entities.TrackItemRaw
import entities.TrackItemRegular
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.job.FFmpegJob
import net.bramp.ffmpeg.probe.FFmpegProbeResult
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class Mp3Cutter(
    private val inputFile: File,
    private val outputDir: File,
) {
    init {
        Preconditions.checkArgument(outputDir.isDirectory)
    }

    private val ffmpeg = FFmpeg("/usr/local/bin/ffmpeg")
    private val ffprobe = FFprobe("/usr/local/bin/ffprobe")
    private val executor = FFmpegExecutor(ffmpeg, ffprobe)

    fun scan(inputFile: File): FFmpegProbeResult {
        val probeResult = ffprobe.probe(inputFile.absolutePath)
        // val format: FFmpegFormat = probeResult.getFormat()
        // System.out.format(
        //     "%nFile: '%s' ; Format: '%s' ; Duration: %.3fs",
        //     format.filename,
        //     format.format_long_name,
        //     format.duration
        // )
        return probeResult
    }

    fun cut(
        tracks: List<TrackItem>,
    ) {
        val convertJobs = convertTracksToJobs(tracks = tracks)
        convertJobs
            .forEach { it.run() }
    }

    private fun convertTracksToJobs(
        tracks: List<TrackItem>,
    ): List<FFmpegJob> {
        val ffProbeResult = ffprobe.probe(inputFile.absolutePath)
        Preconditions.checkArgument(inputFile.isFile)
        val cutJobs = tracks
            .map { track ->
                when (track) {
                    is TrackItemRaw -> throw IllegalStateException("Cannot convert TrackItemRaw")
                    is TrackItemLast -> convertTrackLast(ffProbeResult, track)
                    is TrackItemRegular -> convertTrackRegular(ffProbeResult, track)
                }
            }
        return cutJobs
    }

    private fun convertTrackRegular(
        ffProbeResult: FFmpegProbeResult,
        track: TrackItemRegular,
    ): FFmpegJob {
        val targetOutput = File(outputDir, "${track.name}.mp3")
        val durationRangeStart = Duration.between(
            LocalTime.MIN,
            track.start,
        )
        val durationRangeEnd = Duration.between(
            track.start,
            track.end,
        )
        return toFFmpegJob(
            ffProbeResult = ffProbeResult,
            targetOutput = targetOutput,
            offsetStart = durationRangeStart,
            duration = durationRangeEnd,
        )
    }

    private fun convertTrackLast(
        ffProbeResult: FFmpegProbeResult,
        track: TrackItemLast,
    ): FFmpegJob {
        val targetOutput = File(outputDir, "${track.name}.mp3")
        val durationRangeStart = Duration.between(
            LocalTime.MIN,
            track.start,
        )
        val durationRangeEnd = Duration.ofSeconds(ffProbeResult.format.duration.toLong())
        return toFFmpegJob(
            ffProbeResult = ffProbeResult,
            targetOutput = targetOutput,
            offsetStart = durationRangeStart,
            duration = durationRangeEnd,
        )
    }

    private fun toFFmpegJob(
        ffProbeResult: FFmpegProbeResult,
        targetOutput: File,
        offsetStart: Duration,
        duration: Duration,
    ): FFmpegJob {
        val builder: FFmpegBuilder = FFmpegBuilder()
            .setInput(ffProbeResult)
            .addOutput(targetOutput.absolutePath)
            .setFormat("mp3")
            .setFilename(targetOutput.absolutePath)
            .setStartOffset(offsetStart.toMillis(), TimeUnit.MILLISECONDS)
            .setDuration(duration.toMillis(), TimeUnit.MILLISECONDS)
            .setVideoCodec("copy")
            .setAudioCodec("copy")
            .done()
        return executor.createJob(builder)
    }

    companion object {
        private val l = LoggerFactory.getLogger(Mp3Cutter::class.java)
    }
}