package lt.markmerkk.mp3cutter

import lt.markmerkk.mp3cutter.entities.TrackItemRaw
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalTime

class NameParserRegexLineTest {

    private val artistTrackSeparator = "-"

    @Test
    fun invalidEmpty() {
        // Assemble
        val rawInput = ""

        // Act
        val result = NameParser.parseLine(
            artistTrackSeparator = artistTrackSeparator,
            rawLine = rawInput,
        )

        // Assert
        Assertions.assertThat(result).isNull()
    }

    @Test
    fun invalidMalformed() {
        // Assemble
        val rawInput = "asdf"

        // Act
        val result = NameParser.parseLine(
            artistTrackSeparator = artistTrackSeparator,
            rawLine = rawInput,
        )

        // Assert
        Assertions.assertThat(result).isNull()
    }

    @Test
    fun valid() {
        // Assemble
        val rawInput = "01:52:31 San Holo - Show Me"

        // Act
        val result = NameParser.parseLine(
            artistTrackSeparator = artistTrackSeparator,
            rawLine = rawInput,
        )

        // Assert
        Assertions.assertThat(result).isEqualTo(
            TrackItemRaw(
                cutStart = LocalTime.of(1, 52, 31),
                cutEnd = null,
                artist = "San Holo",
                track = "Show Me",
            ),
        )
    }

    @Test
    fun valid_shortHours() {
        // Assemble
        val rawInput = "1:52:31 San Holo - Show Me"

        // Act
        val result = NameParser.parseLine(
            artistTrackSeparator = artistTrackSeparator,
            rawLine = rawInput,
        )

        // Assert
        Assertions.assertThat(result).isEqualTo(
            TrackItemRaw(
                cutStart = LocalTime.of(1, 52, 31),
                cutEnd = null,
                artist = "San Holo",
                track = "Show Me",
            ),
        )
    }

    @Test
    fun valid_noHours() {
        // Assemble
        val rawInput = "52:31 San Holo - Show Me"

        // Act
        val result = NameParser.parseLine(
            artistTrackSeparator = artistTrackSeparator,
            rawLine = rawInput,
        )

        // Assert
        Assertions.assertThat(result).isEqualTo(
            TrackItemRaw(
                cutStart = LocalTime.of(0, 52, 31),
                cutEnd = null,
                artist = "San Holo",
                track = "Show Me",
            ),
        )
    }

    @Test
    fun valid2_shortMinutes() {
        // Assemble
        val rawTime = "1:30"

        // Act
        val result = TimeUtils.parseTime(rawTime)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            LocalTime.of(0, 1, 30)
        )
    }

    @Test
    fun valid_endTime() {
        // Assemble
        val rawInput = "1:52:31 1:55:31 San Holo - Show Me"

        // Act
        val result = NameParser.parseLine(
            artistTrackSeparator = artistTrackSeparator,
            rawLine = rawInput,
        )

        // Assert
        Assertions.assertThat(result).isEqualTo(
            TrackItemRaw(
                cutStart = LocalTime.of(1, 52, 31),
                cutEnd = LocalTime.of(1, 55, 31),
                artist = "San Holo",
                track = "Show Me",
            ),
        )
    }

    @Test
    fun valid_endTime_onlyMinutes() {
        // Assemble
        val rawInput = "52:31 55:31 San Holo - Show Me"

        // Act
        val result = NameParser.parseLine(
            artistTrackSeparator = artistTrackSeparator,
            rawLine = rawInput,
        )

        // Assert
        Assertions.assertThat(result).isEqualTo(
            TrackItemRaw(
                cutStart = LocalTime.of(0, 52, 31),
                cutEnd = LocalTime.of(0, 55, 31),
                artist = "San Holo",
                track = "Show Me",
            ),
        )
    }

    @Test
    fun invalid_endTimeBeforeStart() {
        // Assemble
        val rawInput = "52:31 30:31 San Holo - Show Me"

        // Act
        val result = NameParser.parseLine(
            artistTrackSeparator = artistTrackSeparator,
            rawLine = rawInput,
        )

        // Assert
        Assertions.assertThat(result).isNull()
    }

    @Test
    fun valid_customChars() {
        // Assemble
        val rawInput = "01:52:31 San Holo/Test1 - Show Me (Feat. Test1)"

        // Act
        val result = NameParser.parseLine(
            artistTrackSeparator = artistTrackSeparator,
            rawLine = rawInput,
        )

        // Assert
        Assertions.assertThat(result).isEqualTo(
            TrackItemRaw(
                cutStart = LocalTime.of(1, 52, 31),
                cutEnd = null,
                artist = "San HoloTest1",
                track = "Show Me (Feat. Test1)",
            ),
        )
    }
}