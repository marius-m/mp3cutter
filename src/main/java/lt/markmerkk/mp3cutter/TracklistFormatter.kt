package lt.markmerkk.mp3cutter

import lt.markmerkk.mp3cutter.entities.TrackSegment
import java.time.Duration

/**
 * Formats track segments into map file text, readable by [NameParser]
 * Each line looks like: "0:00:02 0:04:19 <artist> - Track 1"
 */
class TracklistFormatter(
    private val artistTrackSeparator: String = NameParser.DEFAULT_ARTIST_SONG_SEPARATOR,
    private val nextLineSeparator: String = NameParser.DEFAULT_NEXT_LINE,
) {

    fun format(
        segments: List<TrackSegment>,
        artist: String,
    ): String {
        val artistSanitized = NameParser.sanitizeTitle(artist)
            .trim()
            .ifEmpty { DEFAULT_ARTIST }
        return segments
            .mapIndexed { index, segment ->
                "%s %s %s %s %s".format(
                    formatTime(segment.start),
                    formatTime(segment.end),
                    artistSanitized,
                    artistTrackSeparator,
                    "Track ${index + 1}",
                )
            }
            .joinToString(separator = nextLineSeparator)
    }

    companion object {
        const val DEFAULT_ARTIST = "Unknown"

        /**
         * @return time in "H:mm:ss" format
         */
        fun formatTime(duration: Duration): String {
            return "%d:%02d:%02d".format(
                duration.toHours(),
                duration.toMinutesPart(),
                duration.toSecondsPart(),
            )
        }
    }
}
