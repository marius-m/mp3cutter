package lt.markmerkk.mp3cutter.time

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Parse time "hours:minutes:seconds"
 */
class TimeParserHours: TimeParser {

    override fun parseTime(rawTime: String): LocalTime? {
        return try {
            LocalTime.parse(rawTime, timeFormatter)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    companion object {
        const val DEFAULT_TIME_FORMAT = "H:mm:ss"
        private val timeFormatter = DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)
    }
}