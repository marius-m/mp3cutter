package lt.markmerkk.mp3cutter

import lt.markmerkk.mp3cutter.entities.JobSpec
import lt.markmerkk.mp3cutter.entities.TrackFilterEnd
import lt.markmerkk.mp3cutter.entities.TrackFilterStart
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.probe.FFmpegProbeResult

/**
 * Builds ffmpeg arguments to cut one track
 * Each implementation is responsible for a different export (audio only, video, video encoded on GPU)
 */
interface JobBuilder {
    fun create(
        ffProbeResult: FFmpegProbeResult,
        cut: JobSpec,
    ): FFmpegBuilder

    companion object {
        /**
         * Audio fade in at the start and fade out at the end of the track
         * Note: fade out uses start filter duration, kept as is from [Mp3Cutter]
         */
        fun audioFilterFade(
            filterStart: TrackFilterStart,
            filterEnd: TrackFilterEnd,
        ): String {
            return "afade=t=in:st=%d:d=%d,afade=t=out:st=%d:d=%d"
                .format(
                    filterStart.startOffset.toSecondOfDay(),
                    filterStart.duration.toSeconds(),
                    filterEnd.startOffset.toSecondOfDay(),
                    filterStart.duration.toSeconds(),
                )
        }

        /**
         * Track name drawn on top left, video fade in at the start and fade out at the end of the track
         * Note: fade out uses start filter duration, kept as is from [Mp3Cutter]
         */
        fun videoFilterOverlay(
            trackName: String,
            filterStart: TrackFilterStart,
            filterEnd: TrackFilterEnd,
        ): String {
            return StringBuilder("drawtext=text='${trackName}':x=10:y=10:fontcolor=white:fontsize=24")
                .append(",")
                .append(
                    "fade=t=in:st=%d:d=%d,fade=t=out:st=%d:d=%d"
                        .format(
                            filterStart.startOffset.toSecondOfDay(),
                            filterStart.duration.toSeconds(),
                            filterEnd.startOffset.toSecondOfDay(),
                            filterStart.duration.toSeconds(),
                        )
                )
                .toString()
        }
    }
}
