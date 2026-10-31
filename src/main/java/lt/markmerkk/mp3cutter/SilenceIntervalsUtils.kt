package lt.markmerkk.mp3cutter

import lt.markmerkk.mp3cutter.entities.SilenceInterval
import java.time.Duration

object SilenceIntervalsUtils {

    val DEFAULT_MAX_GAP: Duration = Duration.ofMillis(500)

    /**
     * Joins silences that overlap or are closer than [maxGap] to each other
     */
    fun merge(
        intervals: List<SilenceInterval>,
        maxGap: Duration = DEFAULT_MAX_GAP,
    ): List<SilenceInterval> {
        val merged = mutableListOf<SilenceInterval>()
        intervals
            .sortedBy { it.start }
            .forEach { interval ->
                val previous = merged.lastOrNull()
                if (previous != null && interval.start <= previous.end.plus(maxGap)) {
                    merged[merged.lastIndex] = previous.copy(end = maxOf(previous.end, interval.end))
                } else {
                    merged.add(interval)
                }
            }
        return merged.toList()
    }
}
