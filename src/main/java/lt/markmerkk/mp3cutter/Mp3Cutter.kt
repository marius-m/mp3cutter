package lt.markmerkk.mp3cutter

import com.google.common.base.Preconditions
import lt.markmerkk.mp3cutter.entities.*
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.job.FFmpegJob
import net.bramp.ffmpeg.probe.FFmpegProbeResult
import java.io.File
import java.time.Duration
import java.time.LocalTime

class Mp3Cutter(
    private val overrideFfmpegRootPath: String? = null,
    private val inputFile: File,
    private val outputDir: File,
    private val exportFormat: ExportFormat,
    private val gpu: Gpu = Gpu.NONE,
) {
    init {
        Preconditions.checkArgument(outputDir.isDirectory)
    }

    private val jobBuilder: JobBuilder = JobBuilder.fromFormat(exportFormat, gpu)

    private val ffmpegBinaries = FfmpegResolver().resolve(overrideFfmpegRootPath)
    private val ffmpeg = FFmpeg(ffmpegBinaries.ffmpeg.absolutePath)
    private val ffprobe = FFprobe(ffmpegBinaries.ffprobe.absolutePath)
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
        val targetOutput = File(outputDir, "${track.name}.${exportFormat.name.lowercase()}")
        val durationRangeStart = Duration.between(
            LocalTime.MIN,
            track.startOffset,
        )
        val jobSpec = JobSpec(
            targetOutput = targetOutput,
            offsetStart = durationRangeStart,
            duration = track.duration,
            filterStart = TrackFilterStart.fromTrack(track),
            filterEnd = TrackFilterEnd.fromTrack(track),
            trackName = track.name,
        )
        return executor.createJob(jobBuilder.create(ffProbeResult, jobSpec))
    }
}
