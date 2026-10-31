package lt.markmerkk.mp3cutter

import lt.markmerkk.mp3cutter.entities.SilenceInterval
import lt.markmerkk.mp3cutter.entities.TrackSegment
import java.time.Duration

/**
 * Turns silences into tracks, where each track is the audio between two silences
 * Silences themselves are trimmed off
 */
class SilenceTracklistGenerator(
    private val minTrackLength: Duration = DEFAULT_MIN_TRACK_LENGTH,
) {

    /**
     * @param silences already merged silences (see [SilenceIntervalsUtils.merge])
     * @param totalDuration length of the input, closes the last track
     * @return tracks with times rounded to whole seconds, as map file supports only seconds
     */
    fun generate(
        silences: List<SilenceInterval>,
        totalDuration: Duration,
    ): List<TrackSegment> {
        val segments = mutableListOf<TrackSegment>()
        var cursor = Duration.ZERO
        silences
            .sortedBy { it.start }
            .forEach { silence ->
                segments.add(segmentRounded(start = cursor, end = silence.start))
                cursor = silence.end
            }
        segments.add(segmentRounded(start = cursor, end = totalDuration))
        return segments
            .filter { it.duration >= minTrackLength }
    }

    companion object {
        val DEFAULT_MIN_TRACK_LENGTH: Duration = Duration.ofSeconds(30)

        private fun segmentRounded(start: Duration, end: Duration): TrackSegment {
            return TrackSegment(
                start = roundToSeconds(start),
                end = roundToSeconds(end),
            )
        }

        private fun roundToSeconds(duration: Duration): Duration {
            return Duration.ofSeconds(Math.round(duration.toMillis() / 1000.0))
        }
    }
}
