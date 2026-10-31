package lt.markmerkk.mp3cutter.entities

import java.io.File
import java.time.Duration

/**
 * Everything needed to cut one track out of the input file
 */
data class JobSpec(
    val targetOutput: File,
    val offsetStart: Duration,
    val duration: Duration,
    val filterStart: TrackFilterStart,
    val filterEnd: TrackFilterEnd,
    val trackName: String,
)
