package lt.markmerkk.mp3cutter

import lt.markmerkk.mp3cutter.entities.TrackItemRegular
import lt.markmerkk.mp3cutter.entities.TrackSegment
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalTime

class TracklistFormatterFormatTest {

    private val formatter = TracklistFormatter()

    /**
     * No segments -> empty map text
     */
    @Test
    fun empty() {
        // Act
        val result = formatter.format(emptyList(), artist = "darkjazz2")

        // Assert
        Assertions.assertThat(result).isEmpty()
    }

    /**
     * 4 segments, both under and over an hour
     * Each segment is expected on its own line as "H:mm:ss H:mm:ss <artist> - Track N",
     * numbered from 1 in the given order
     */
    @Test
    fun lines() {
        // Assemble
        val segments = listOf(
            segment(startSeconds = 2, endSeconds = 259),
            segment(startSeconds = 261, endSeconds = 548),
            segment(startSeconds = 3599, endSeconds = 3661),
            segment(startSeconds = 5539, endSeconds = 5834),
        )

        // Act
        val result = formatter.format(segments, artist = "darkjazz2")

        // Assert
        Assertions.assertThat(result).isEqualTo(
            """
            0:00:02 0:04:19 darkjazz2 - Track 1
            0:04:21 0:09:08 darkjazz2 - Track 2
            0:59:59 1:01:01 darkjazz2 - Track 3
            1:32:19 1:37:14 darkjazz2 - Track 4
            """.trimIndent()
        )
    }

    /**
     * Artist comes from the input file name, which may contain characters not allowed in map file
     * - "dark-jazz2" -> "darkjazz2" (the '-' would clash with artist / track separator)
     * - "!@#" -> sanitized to nothing -> "Unknown"
     */
    @Test
    fun artistSanitized() {
        // Assemble
        val segments = listOf(segment(startSeconds = 0, endSeconds = 60))

        // Act
        val resultDashed = formatter.format(segments, artist = "dark-jazz2")
        val resultInvalid = formatter.format(segments, artist = "!@#")

        // Assert
        Assertions.assertThat(resultDashed).isEqualTo("0:00:00 0:01:00 darkjazz2 - Track 1")
        Assertions.assertThat(resultInvalid).isEqualTo("0:00:00 0:01:00 Unknown - Track 1")
    }

    /**
     * Formatted text is fed back to [NameParser], as 'cut -m' would read it
     * All 4 tracks are expected back as regular tracks (explicit end times),
     * with the same start offsets, durations and names
     */
    @Test
    fun readableByNameParser() {
        // Assemble
        val segments = listOf(
            segment(startSeconds = 2, endSeconds = 259),
            segment(startSeconds = 261, endSeconds = 548),
            segment(startSeconds = 3599, endSeconds = 3661),
            segment(startSeconds = 5539, endSeconds = 5834),
        )

        // Act
        val mapText = formatter.format(segments, artist = "darkjazz2")
        val result = NameParser().parse(mapText)

        // Assert
        Assertions.assertThat(result).containsExactly(
            TrackItemRegular(0, LocalTime.of(0, 0, 2), Duration.ofSeconds(257), "darkjazz2", "Track 1"),
            TrackItemRegular(1, LocalTime.of(0, 4, 21), Duration.ofSeconds(287), "darkjazz2", "Track 2"),
            TrackItemRegular(2, LocalTime.of(0, 59, 59), Duration.ofSeconds(62), "darkjazz2", "Track 3"),
            TrackItemRegular(3, LocalTime.of(1, 32, 19), Duration.ofSeconds(295), "darkjazz2", "Track 4"),
        )
    }

    private fun segment(startSeconds: Long, endSeconds: Long): TrackSegment {
        return TrackSegment(
            start = Duration.ofSeconds(startSeconds),
            end = Duration.ofSeconds(endSeconds),
        )
    }
}
