package lt.markmerkk.mp3cutter.entities

import java.time.Duration

/**
 * Silent part of the input, offsets from the start of the file
 */
data class SilenceInterval(
    val start: Duration,
    val end: Duration,
)
