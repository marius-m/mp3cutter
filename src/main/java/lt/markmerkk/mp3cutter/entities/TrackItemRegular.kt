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

    companion object {
        fun from(trackCurrent: TrackItemRaw, trackNext: TrackItemRaw): TrackItemRegular {
            return TrackItemRegular(
                startOffset = trackCurrent.cutStart,
                duration = Duration.between(trackCurrent.cutStart, trackNext.cutStart),
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
