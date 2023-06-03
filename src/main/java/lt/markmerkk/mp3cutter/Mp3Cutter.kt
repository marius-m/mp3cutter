package lt.markmerkk.mp3cutter

import com.google.common.base.Preconditions
import lt.markmerkk.mp3cutter.entities.*
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
    private val exportFormat: ExportFormat,
) {
    init {
        Preconditions.checkArgument(outputDir.isDirectory)
    }

    private val rootPath = "/opt/homebrew/bin"
    private val ffmpeg = FFmpeg("${rootPath}/ffmpeg")
    private val ffprobe = FFprobe("${rootPath}/ffprobe")
    private val executor = FFmpegExecutor(ffmpeg, ffprobe)

    fun cut(
        tracks: List<TrackItem>,
    ) {
        val convertJobs = convertTracksToJobs(
            exportFormat = exportFormat,
            tracks = tracks,
        )
        convertJobs
            .forEach { it.run() }
    }

    private fun convertTracksToJobs(
        exportFormat: ExportFormat,
        tracks: List<TrackItem>,
    ): List<FFmpegJob> {
        val ffProbeResult = ffprobe.probe(inputFile.absolutePath)
        Preconditions.checkArgument(inputFile.isFile)
        val cutJobs = tracks
            .mapIndexed { index, track ->
                when (track) {
                    is TrackItemLast -> {
                        val regularFromLast = TrackItemRegular.fromLastWithMaxDuration(
                            index = index,
                            trackLast = track,
                            maxDuration = Duration.ofSeconds(ffProbeResult.format.duration.toLong()),
                        )
                        convertTrackRegular(exportFormat, ffProbeResult, regularFromLast)
                    }
                    is TrackItemRegular -> convertTrackRegular(exportFormat, ffProbeResult, track)
                }
            }
        return cutJobs
    }

    private fun convertTrackRegular(
        exportFormat: ExportFormat,
        ffProbeResult: FFmpegProbeResult,
        track: TrackItemRegular,
    ): FFmpegJob {
        val targetOutput = File(outputDir, "${track.name}.mp3")
        val durationRangeStart = Duration.between(
            LocalTime.MIN,
            track.startOffset,
        )
        val filterStart = TrackFilterStart.fromTrack(track)
        val filterEnd = TrackFilterEnd.fromTrack(track)
        return toFFmpegJob(
            exportFormat = exportFormat,
            ffProbeResult = ffProbeResult,
            targetOutput = targetOutput,
            offsetStart = durationRangeStart,
            duration = track.duration,
            filterStart = filterStart,
            filterEnd = filterEnd,
        )
    }

    private fun toFFmpegJob(
        exportFormat: ExportFormat,
        ffProbeResult: FFmpegProbeResult,
        targetOutput: File,
        offsetStart: Duration,
        duration: Duration,
        filterStart: TrackFilterStart,
        filterEnd: TrackFilterEnd,
    ): FFmpegJob {
        val filterString = "afade=t=in:st=%d:d=%d,afade=t=out:st=%d:d=%d"
            .format(
                filterStart.startOffset.toSecondOfDay(),
                filterStart.duration.toSeconds(),
                filterEnd.startOffset.toSecondOfDay(),
                filterStart.duration.toSeconds(),
            )
        val builder: FFmpegBuilder = FFmpegBuilder()
            .setInput(ffProbeResult)
            .addOutput(targetOutput.absolutePath)
            .setFormat(exportFormat.toRaw())
            .setFilename(targetOutput.absolutePath)
            .setStartOffset(offsetStart.toMillis(), TimeUnit.MILLISECONDS)
            .setDuration(duration.toMillis(), TimeUnit.MILLISECONDS)
            .setAudioFilter(filterString)
            .done()
        return executor.createJob(builder)
    }

    companion object {
        private val l = LoggerFactory.getLogger(Mp3Cutter::class.java)
    }
}