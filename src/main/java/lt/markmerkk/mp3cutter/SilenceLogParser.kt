package lt.markmerkk.mp3cutter

import lt.markmerkk.mp3cutter.entities.SilenceInterval
import java.time.Duration

/**
 * Parses ffmpeg 'silencedetect' filter output into silent intervals
 * Lines look like:
 * - [silencedetect @ 0x...] silence_start: 259.042
 * - [silencedetect @ 0x...] silence_end: 261.156 | silence_duration: 2.11454
 * Progress output may be glued after the number, it is ignored
 */
class SilenceLogParser {

    /**
     * @param totalDuration closes the last silence, if it runs until the end of the file
     */
    fun parse(
        rawText: String,
        totalDuration: Duration,
    ): List<SilenceInterval> {
        val intervals = mutableListOf<SilenceInterval>()
        var currentStart: Duration? = null
        rawText.lines()
            .forEach { line ->
                val start = regexStart.find(line)
                val end = regexEnd.find(line)
                if (start != null) {
                    currentStart = secondsToDuration(start.groupValues[1])
                }
                if (end != null && currentStart != null) {
                    intervals.add(
                        SilenceInterval(
                            start = currentStart!!,
                            end = secondsToDuration(end.groupValues[1]),
                        )
                    )
                    currentStart = null
                }
            }
        if (currentStart != null) {
            intervals.add(SilenceInterval(start = currentStart!!, end = totalDuration))
        }
        return intervals.toList()
    }

    companion object {
        private val regexStart = "silence_start: (-?\\d+(?:\\.\\d+)?)".toRegex()
        private val regexEnd = "silence_end: (-?\\d+(?:\\.\\d+)?)".toRegex()

        private fun secondsToDuration(rawSeconds: String): Duration {
            val millis = Math.round(rawSeconds.toDouble() * 1000)
            return Duration.ofMillis(millis.coerceAtLeast(0))
        }
    }
}
