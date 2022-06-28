package lt.markmerkk.mp3cutter

import com.google.common.base.Preconditions
import lt.markmerkk.mp3cutter.entities.TrackItem
import lt.markmerkk.mp3cutter.entities.TrackItemLast
import lt.markmerkk.mp3cutter.entities.TrackItemRaw
import lt.markmerkk.mp3cutter.entities.TrackItemRegular
import org.slf4j.LoggerFactory
import java.time.LocalTime

class NameParser(
    private val artistTrackSeparator: String = DEFAULT_ARTIST_SONG_SEPARATOR,
    private val nextLineSeparator: String = DEFAULT_NEXT_LINE,
) {

    fun parse(rawText: String): List<TrackItem> {
        val lines = splitLines(
            lineSeperator = nextLineSeparator,
            rawText = rawText,
        )
        val tracksRaw = parseLines(
            artistTrackSeparator = artistTrackSeparator,
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
        artistTrackSeparator: String,
        lines: List<String>,
    ): List<TrackItemRaw> {
        return lines.mapNotNull { line ->
            parseLine(
                artistTrackSeparator = artistTrackSeparator,
                rawLine = line,
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

        private val regexTime = "\\d{1,2}?:?\\d{1,2}:\\d{1,2}"
            .toRegex()

        fun parseLine(
            artistTrackSeparator: String,
            rawLine: String,
        ): TrackItemRaw? {
            val match = regexLine(songSeperator = artistTrackSeparator)
                .find(rawLine)
            if (match != null) {
                val cutStart = TimeUtils.parseTime(match.groupValues[1].trim())!!
                val cutEnd = TimeUtils.parseTime(match.groupValues[2].trim())
                if (cutEnd != null && cutEnd.isBefore(cutStart)) {
                    return null
                }
                return TrackItemRaw(
                    cutStart = cutStart,
                    cutEnd = cutEnd,
                    artist = match.groupValues[3].trim(),
                    track = match.groupValues[4].trim(),
                )
            }
            return null
        }

        fun regexLine(songSeperator: String): Regex {
            return "^(${regexTime.pattern})( ${regexTime.pattern})? (.+)$songSeperator(.+)$"
                .toRegex()
        }
    }
}
