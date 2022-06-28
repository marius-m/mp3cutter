package lt.markmerkk.mp3cutter

import lt.markmerkk.mp3cutter.time.TimeParserHours
import lt.markmerkk.mp3cutter.time.TimeParserMinutesOnly
import java.time.LocalTime

object TimeUtils {
    private val formatters = listOf(
        TimeParserHours(),
        TimeParserMinutesOnly(),
    )

    fun parseTime(rawTime: String): LocalTime? {
        return formatters
            .mapNotNull { f -> f.parseTime(rawTime) }
            .firstOrNull()
    }
}