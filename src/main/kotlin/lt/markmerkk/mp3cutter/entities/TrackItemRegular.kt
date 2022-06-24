package lt.markmerkk.mp3cutter.entities

import java.time.LocalTime

sealed class TrackItem {
    abstract val name: String
}

/**
 * Raw track data that has cut points
 */
data class TrackItemRaw(
    val cut: LocalTime,
    val artist: String,
    val track: String,
) : TrackItem() {
    override val name: String = "$artist - $track"
}

/**
 * Regular track that has a start and an ending
 */
data class TrackItemRegular(
    val start: LocalTime,
    val end: LocalTime,
    val artist: String,
    val track: String,
) : TrackItem() {

    override val name: String = "$artist - $track"

    companion object {
        fun from(songCurrent: TrackItemRaw, songNext: TrackItemRaw): TrackItemRegular {
            return TrackItemRegular(
                start = songCurrent.cut,
                end = songNext.cut,
                artist = songCurrent.artist,
                track = songCurrent.track,
            )
        }
    }
}

/**
 * Defines a last track
 * Does not have an ending
 */
data class TrackItemLast(
    val start: LocalTime,
    val artist: String,
    val track: String,
) : TrackItem() {

    override val name: String = "$artist - $track"

    companion object {
        fun from(songCurrent: TrackItemRaw): TrackItemLast {
            return TrackItemLast(
                start = songCurrent.cut,
                artist = songCurrent.artist,
                track = songCurrent.track,
            )
        }
    }
}
