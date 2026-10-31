package lt.markmerkk.mp3cutter

import lt.markmerkk.mp3cutter.entities.ExportFormat
import lt.markmerkk.mp3cutter.entities.JobSpec
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.probe.FFmpegProbeResult
import java.util.concurrent.TimeUnit

/**
 * Cuts video with audio fade in / out, track name overlay and video fade in / out
 * ffmpeg picks the video encoder itself
 */
class JobSpecMp4 : JobBuilder {
    override fun create(
        ffProbeResult: FFmpegProbeResult,
        cut: JobSpec,
    ): FFmpegBuilder {
        val targetPath = cut.targetOutput.absolutePath
        return FFmpegBuilder()
            .setInput(ffProbeResult)
            .addOutput(targetPath)
            .setFormat(ExportFormat.MP4.toRaw())
            .setFilename(targetPath)
            .setStartOffset(cut.offsetStart.toMillis(), TimeUnit.MILLISECONDS)
            .setDuration(cut.duration.toMillis(), TimeUnit.MILLISECONDS)
            .setAudioFilter(JobBuilder.audioFilterFade(cut.filterStart, cut.filterEnd))
            .setVideoFilter(JobBuilder.videoFilterOverlay(cut.trackName, cut.filterStart, cut.filterEnd))
            .done()
    }
}
