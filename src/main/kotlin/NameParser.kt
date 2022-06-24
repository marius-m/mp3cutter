import entities.TrackItem
import entities.TrackItemLast
import entities.TrackItemRegular
import entities.TrackItemRaw
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NameParser(
    private val artistSongSeparator: String = DEFAULT_ARTIST_SONG_SEPARATOR,
    private val nextLineSeparator: String = DEFAULT_NEXT_LINE,
    private val timeFormat: String = DEFAULT_TIME_FORMAT,
) {
    private val timeFormatter = DateTimeFormatter.ofPattern(timeFormat)

    fun parse(rawText: String): List<TrackItem> {
        val lines = splitLines(
            lineSeperator = nextLineSeparator,
            rawText = rawText,
        )
        val songsRaw = parseLines(
            artistSongSeparator = artistSongSeparator,
            lines = lines,
        )
        return songsRawToSongs(songsRaw)
    }

    private fun songsRawToSongs(
        songsRaw: List<TrackItemRaw>,
    ): List<TrackItem> {
        val tracks = mutableListOf<TrackItem>()
        for (songIndex in songsRaw.indices) {
            val songCurrent: TrackItemRaw = songsRaw[songIndex]
            val songNextIndex = songIndex + 1
            val songNext: TrackItemRaw? = if (songNextIndex < songsRaw.size) {
                songsRaw[songNextIndex]
            } else {
                null
            }
            if (songNext != null) {
                tracks.add(TrackItemRegular.from(songCurrent, songNext))
            } else {
                tracks.add(TrackItemLast.from(songCurrent))
            }
        }
        return tracks.toList()
    }

    private fun splitLines(
        lineSeperator: String,
        rawText: String,
    ): List<String> {
        return rawText.split(lineSeperator)
    }

    private fun parseLines(
        artistSongSeparator: String,
        lines: List<String>,
    ): List<TrackItemRaw> {
        return lines.mapNotNull { line ->
            regexLine(songSeperator = artistSongSeparator)
                .find(line)
        }.map { match ->
            TrackItemRaw(
                cut = LocalTime.parse(match.groupValues[1], timeFormatter),
                artist = match.groupValues[2].trim(),
                track = match.groupValues[3].trim(),
            )
        }
    }

    companion object {
        const val DEFAULT_ARTIST_SONG_SEPARATOR = "-"
        const val DEFAULT_NEXT_LINE = "\n"
        const val DEFAULT_TIME_FORMAT = "H:mm:ss"

        private fun regexLine(songSeperator: String): Regex {
            return "^([0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}) (.+)$songSeperator(.+)$"
                .toRegex()
        }
    }
}
