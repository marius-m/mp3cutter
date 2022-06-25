package lt.markmerkk.mp3cutter.entities

import java.time.Duration
import java.time.LocalTime

sealed class TrackFilter {
    abstract val startOffset: LocalTime
    abstract val duration: Duration

    companion object {
        const val DEFAULT_FILTER_SEC = 3
        val defaultDuration = Duration.ofSeconds(DEFAULT_FILTER_SEC.toLong())
    }
}

/**
 * Ads filter at the start of the track
 * Will not apply filter if track duration is lower than [TrackFilter.DEFAULT_FILTER_SEC]
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
        fun fromTrack(trackItem: TrackItemRegular): TrackFilterStart {
            return if (trackItem.duration > defaultDuration) {
                return TrackFilterStart(
                    startOffset = LocalTime.MIN,
                    duration = defaultDuration,
                )
            } else {
                return asEmpty()
            }
        }
    }
}

/**
 * Adds filter at the end of the track
 * Will not apply filter if duration is lower than [TrackFilter.DEFAULT_FILTER_SEC]
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
        fun fromTrack(trackItem: TrackItemRegular): TrackFilterEnd {
            val timeEnd = trackItem.startOffset
                .plusSeconds(trackItem.duration.seconds)
            return if (timeEnd.toSecondOfDay() > defaultDuration.seconds) {
                TrackFilterEnd(
                    startOffset = timeEnd.minusSeconds(defaultDuration.seconds),
                    duration = defaultDuration,
                )
            } else {
                asEmpty()
            }
        }
    }
}
