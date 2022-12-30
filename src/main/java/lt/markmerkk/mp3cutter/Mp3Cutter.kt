package lt.markmerkk.mp3cutter

import com.google.common.base.Preconditions
import lt.markmerkk.mp3cutter.entities.TrackFilterEnd
import lt.markmerkk.mp3cutter.entities.TrackFilterStart
import lt.markmerkk.mp3cutter.entities.TrackItem
import lt.markmerkk.mp3cutter.entities.TrackItemLast
import lt.markmerkk.mp3cutter.entities.TrackItemRaw
import lt.markmerkk.mp3cutter.entities.TrackItemRegular
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

    private val rootPath = "/opt/homebrew/bin"
    private val ffmpeg = FFmpeg("${rootPath}/ffmpeg")
    private val ffprobe = FFprobe("${rootPath}/ffprobe")
    private val executor = FFmpegExecutor(ffmpeg, ffprobe)

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
                    is TrackItemLast -> {
                        val regularFromLast = TrackItemRegular.fromLastWithMaxDuration(
                            trackLast = track,
                            maxDuration = Duration.ofSeconds(ffProbeResult.format.duration.toLong()),
                        )
                        convertTrackRegular(ffProbeResult, regularFromLast)
                    }
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
            track.startOffset,
        )
        val filterStart = TrackFilterStart.fromTrack(track)
        val filterEnd = TrackFilterEnd.fromTrack(track)
        return toFFmpegJob(
            ffProbeResult = ffProbeResult,
            targetOutput = targetOutput,
            offsetStart = durationRangeStart,
            duration = track.duration,
            filterStart = filterStart,
            filterEnd = filterEnd,
        )
    }

    private fun toFFmpegJob(
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
            .setFormat("mp3")
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