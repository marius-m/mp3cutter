package lt.markmerkk.mp3cutter.entities

import java.time.Duration
import java.time.LocalTime

sealed class TrackFilter {
    abstract val startOffset: LocalTime
    abstract val duration: Duration

    companion object {
        const val DEFAULT_FILTER_SEC = 2
        val defaultDuration = Duration.ofSeconds(DEFAULT_FILTER_SEC.toLong())
    }
}

/**
 * Ads filter at the start of the track
 * Will not apply filter if track duration is lower than 2x[TrackFilter.DEFAULT_FILTER_SEC]
 * Note: Due to fade starts / and ends with a filter
 */
data class TrackFilterStart(
    override val startOffset: LocalTime,
    override val duration: Duration,
): TrackFilter() {

    companion object {
        fun asEmpty(): TrackFilterStart {
            return TrackFilterStart(
                startOffset = LocalTime.MIN,
                duration = Duration.ZERO,
            )
        }
        fun fromTrack(
            trackItem: TrackItemRegular,
            filterDuration: Duration = defaultDuration,
        ): TrackFilterStart {
            return if (trackItem.duration > filterDuration) {
                TrackFilterStart(
                    startOffset = trackItem.startOffset,
                    duration = filterDuration,
                )
            } else {
                asEmpty()
            }
        }
    }
}

/**
 * Adds filter at the end of the track
 * Will not apply filter if duration is lower than 2x[TrackFilter.DEFAULT_FILTER_SEC]
 * Note: Due to fade starts / and ends with a filter
 */
data class TrackFilterEnd(
    override val startOffset: LocalTime,
    override val duration: Duration,
): TrackFilter() {

    companion object {
        fun asEmpty(): TrackFilterEnd {
            return TrackFilterEnd(
                startOffset = LocalTime.MIN,
                duration = Duration.ZERO,
            )
        }
        fun fromTrack(
            trackItem: TrackItemRegular,
            filterDuration: Duration = defaultDuration,
        ): TrackFilterEnd {
            val timeEnd = trackItem.startOffset
                .plusSeconds(trackItem.duration.seconds)
            return if (timeEnd.toSecondOfDay() > filterDuration.seconds) {
                TrackFilterEnd(
                    startOffset = timeEnd.minusSeconds(filterDuration.seconds),
                    duration = filterDuration,
                )
            } else {
                asEmpty()
            }
        }
    }
}
