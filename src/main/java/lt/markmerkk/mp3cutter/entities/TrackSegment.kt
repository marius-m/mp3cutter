package lt.markmerkk.mp3cutter.entities

import java.time.Duration

/**
 * Audible part of the input between silences, offsets from the start of the file
 */
data class TrackSegment(
    val start: Duration,
    val end: Duration,
) {
    val duration: Duration = end.minus(start)
}
