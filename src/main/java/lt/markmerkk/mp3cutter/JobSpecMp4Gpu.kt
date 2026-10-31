package lt.markmerkk.mp3cutter

import lt.markmerkk.mp3cutter.entities.ExportFormat
import lt.markmerkk.mp3cutter.entities.Gpu
import lt.markmerkk.mp3cutter.entities.JobSpec
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.probe.FFmpegProbeResult
import java.util.concurrent.TimeUnit

/**
 * Same as [JobSpecMp4], but video is encoded on [gpu]
 * GPU args go before input, its filter suffix is appended to the video filter chain
 */
class JobSpecMp4Gpu(
    val gpu: Gpu,
) : JobBuilder {
    override fun create(
        ffProbeResult: FFmpegProbeResult,
        cut: JobSpec,
    ): FFmpegBuilder {
        val targetPath = cut.targetOutput.absolutePath
        val videoFilter = StringBuilder(JobBuilder.videoFilterOverlay(cut.trackName, cut.filterStart, cut.filterEnd))
            .apply {
                if (gpu.videoFilterSuffix.isNotEmpty()) {
                    append(",").append(gpu.videoFilterSuffix)
                }
            }
            .toString()
        val outputBuilder = FFmpegBuilder()
            .setInput(ffProbeResult)
            .addOutput(targetPath)
            .setFormat(ExportFormat.MP4.toRaw())
            .setFilename(targetPath)
            .setStartOffset(cut.offsetStart.toMillis(), TimeUnit.MILLISECONDS)
            .setDuration(cut.duration.toMillis(), TimeUnit.MILLISECONDS)
            .setAudioFilter(JobBuilder.audioFilterFade(cut.filterStart, cut.filterEnd))
            .setVideoFilter(videoFilter)
        if (gpu.videoCodec != null) {
            outputBuilder.setVideoCodec(gpu.videoCodec)
        }
        val builder = outputBuilder.done()
        if (gpu.globalArgs.isNotEmpty()) {
            builder.addExtraArgs(*gpu.globalArgs.toTypedArray())
        }
        return builder
    }
}
