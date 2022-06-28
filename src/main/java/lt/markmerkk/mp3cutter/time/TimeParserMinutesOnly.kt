package lt.markmerkk.mp3cutter.time

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Parse time "minutes:seconds"
 */
class TimeParserMinutesOnly: TimeParser {

    override fun parseTime(rawTime: String): LocalTime? {
        val matchFormat = regexFormat.find(rawTime) ?: return null
        return try {
            val minutes = matchFormat.groupValues[1].toInt()
            val seconds = matchFormat.groupValues[2].toInt()
            LocalTime.parse("00:%02d:%02d".format(minutes, seconds), timeFormatter)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    companion object {
        const val DEFAULT_TIME_FORMAT = "H:mm:ss"
        private val timeFormatter = DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)
        private val regexFormat = "^([0-9]?[0-9]):([0-9]{2})$"
            .toRegex()
    }
}