package lt.markmerkk.mp3cutter.entities

import java.time.Duration
import java.time.LocalTime

/**
 * Raw track data that has cut points
 */
data class TrackItemRaw(
    val cutStart: LocalTime,
    val cutEnd: LocalTime?,
    val artist: String,
    val track: String,
) {
    val name: String = "$artist - $track"
}

sealed class TrackItem {
    abstract val name: String
}

/**
 * Regular track that has a start and an ending
 */
data class TrackItemRegular(
    val startOffset: LocalTime,
    val duration: Duration,
    val artist: String,
    val track: String,
) : TrackItem() {

    override val name: String = "$artist - $track"

    private val end = startOffset.plusSeconds(duration.toSeconds())

    companion object {
        fun withStartEnd(
            start: LocalTime,
            end: LocalTime,
            artist: String,
            track: String,
        ): TrackItemRegular {
            return TrackItemRegular(
                startOffset = start,
                duration = Duration.between(start, end),
                artist = artist,
                track = track,
            )
        }

        fun from(trackCurrent: TrackItemRaw, trackNext: TrackItemRaw): TrackItemRegular {
            val trackDuration = if (trackCurrent.cutEnd != null) {
                Duration.between(trackCurrent.cutStart, trackCurrent.cutEnd)
            } else {
                Duration.between(trackCurrent.cutStart, trackNext.cutStart)
            }
            return TrackItemRegular(
                startOffset = trackCurrent.cutStart,
                duration = trackDuration,
                artist = trackCurrent.artist,
                track = trackCurrent.track,
            )
        }

        fun fromLastWithMaxDuration(
            trackLast: TrackItemLast,
            maxDuration: Duration,
        ): TrackItemRegular {
            return TrackItemRegular(
                startOffset = trackLast.startOffset,
                duration = maxDuration,
                artist = trackLast.artist,
                track = trackLast.track,
            )
        }
    }
}

/**
 * Defines a last track
 * Does not have an ending
 */
data class TrackItemLast(
    val startOffset: LocalTime,
    val artist: String,
    val track: String,
) : TrackItem() {

    override val name: String = "$artist - $track"

    companion object {
        fun from(songCurrent: TrackItemRaw): TrackItemLast {
            return TrackItemLast(
                startOffset = songCurrent.cutStart,
                artist = songCurrent.artist,
                track = songCurrent.track,
            )
        }
    }
}
