package lt.markmerkk.mp3cutter

import lt.markmerkk.mp3cutter.entities.ExportFormat
import lt.markmerkk.mp3cutter.entities.JobSpec
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.probe.FFmpegProbeResult
import java.util.concurrent.TimeUnit

/**
 * Cuts audio only, with fade in / out
 */
class JobBuilderMp3 : JobBuilder {
    override fun create(
        ffProbeResult: FFmpegProbeResult,
        cut: JobSpec,
    ): FFmpegBuilder {
        val targetPath = cut.targetOutput.absolutePath
        return FFmpegBuilder()
            .setInput(ffProbeResult)
            .addOutput(targetPath)
            .setFormat(ExportFormat.MP3.toRaw())
            .setFilename(targetPath)
            .setStartOffset(cut.offsetStart.toMillis(), TimeUnit.MILLISECONDS)
            .setDuration(cut.duration.toMillis(), TimeUnit.MILLISECONDS)
            .done()
            .setAudioFilter(JobBuilder.audioFilterFade(cut.filterStart, cut.filterEnd))
    }
}
