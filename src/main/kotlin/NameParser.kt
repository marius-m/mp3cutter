import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NameParser(
    private val artistSongSeparator: String = DEFAULT_ARTIST_SONG_SEPARATOR,
    private val nextLineSeparator: String = DEFAULT_NEXT_LINE,
    private val timeFormat: String = DEFAULT_TIME_FORMAT,
) {
    private val timeFormatter = DateTimeFormatter.ofPattern(timeFormat)

    fun parse(rawText: String): List<Song> {
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
        songsRaw: List<SongRaw>,
    ): List<Song> {
        val songs = mutableListOf<Song>()
        for (songIndex in songsRaw.indices) {
            val songCurrent: SongRaw = songsRaw[songIndex]
            val songNextIndex = songIndex + 1
            val songNext: SongRaw? = if (songNextIndex < songsRaw.size) {
                songsRaw[songNextIndex]
            } else {
                null
            }
            if (songNext != null) {
                songs.add(Song.from(songCurrent, songNext))
            } else {
                songs.add(Song.from(songCurrent, songCurrent))
            }
        }
        return songs.toList()
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
    ): List<SongRaw> {
        return lines.mapNotNull { line ->
            regexLine(songSeperator = artistSongSeparator)
                .find(line)
        }.map { match ->
            SongRaw(
                cut = LocalTime.parse(match.groupValues[1], timeFormatter),
                artist = match.groupValues[2].trim(),
                song = match.groupValues[3].trim(),
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

data class SongRaw(
    val cut: LocalTime,
    val artist: String,
    val song: String,
)

data class Song(
    val start: LocalTime,
    val end: LocalTime,
    val artist: String,
    val song: String,
) {
    companion object {
        fun from(songCurrent: SongRaw, songNext: SongRaw): Song {
            return Song(
                start = songCurrent.cut,
                end = songNext.cut,
                artist = songCurrent.artist,
                song = songCurrent.song,
            )
        }
    }
}