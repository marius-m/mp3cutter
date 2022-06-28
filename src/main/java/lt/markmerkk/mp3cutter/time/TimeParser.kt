package lt.markmerkk.mp3cutter.time

import java.time.LocalTime

interface TimeParser {
    fun parseTime(rawTime: String): LocalTime?
}