import com.google.common.base.Preconditions
import entities.TrackItem
import entities.TrackItemLast
import entities.TrackItemRegular
import entities.TrackItemRaw
import org.slf4j.LoggerFactory
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
        val tracksRaw = parseLines(
            artistSongSeparator = artistSongSeparator,
            lines = lines,
        )
        val tracks = tracksRawToTracks(tracksRaw)
        assertNoDuplicateNames(tracks)
        return tracks
    }

    private fun tracksRawToTracks(
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

    @kotlin.jvm.Throws(IllegalArgumentException::class)
    private fun assertNoDuplicateNames(tracks: List<TrackItem>) {
        val hasDuplicates = tracks.size == tracks.distinctBy { it.name }.count()
        Preconditions.checkArgument(hasDuplicates, "Found duplicates in track names")
    }

    companion object {
        private val l = LoggerFactory.getLogger(Mp3Cutter::class.java)

        const val DEFAULT_ARTIST_SONG_SEPARATOR = "-"
        const val DEFAULT_NEXT_LINE = "\n"
        const val DEFAULT_TIME_FORMAT = "H:mm:ss"

        private fun regexLine(songSeperator: String): Regex {
            return "^([0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}) (.+)$songSeperator(.+)$"
                .toRegex()
        }
    }
}
