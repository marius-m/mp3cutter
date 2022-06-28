package lt.markmerkk.mp3cutter

import TestUtils
import lt.markmerkk.mp3cutter.entities.TrackItem
import lt.markmerkk.mp3cutter.entities.TrackItemRegular
import lt.markmerkk.mp3cutter.entities.TrackItemLast
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalTime

class NameParserParseTest {

    private val nameParser = NameParser()

    @Test
    fun invalidEmpty() {
        // Assemble
        val rawInput = ""

        // Act
        val result: List<TrackItem> = nameParser.parse(rawInput)

        // Assert
        Assertions.assertThat(result).isEmpty()
    }

    @Test
    fun invalidMalformed() {
        // Assemble
        val rawInput = "asdf"

        // Act
        val result: List<TrackItem> = nameParser.parse(rawInput)

        // Assert
        Assertions.assertThat(result).isEmpty()
    }

    @Test
    fun validExternalRes() {
        // Assemble
        val rawInput = TestUtils.loadContent("/sample_input_short.txt")

        // Act
        val result: List<TrackItem> = nameParser.parse(rawInput)

        // Assert
        Assertions.assertThat(result).containsExactly(
            TrackItemRegular(
                startOffset = LocalTime.of(0, 0, 0),
                duration = Duration.ofSeconds(10),
                artist = "Moo2",
                track = "Research1",
            ),
            TrackItemLast(
                startOffset = LocalTime.of(0, 0, 10),
                artist = "Moo2",
                track = "Research2",
            ),
        )
    }

    @Test
    fun valid() {
        // Assemble
        val rawInput = """
            0:00:00 San Holo - Show Me
            0:05:20 Kasbo - Over You (feat. Frida Sundemo)
            0:11:24 Kina - I'm In Love With You
        """.trimIndent()

        // Act
        val result: List<TrackItem> = nameParser.parse(rawInput)

        // Assert
        Assertions.assertThat(result).containsExactly(
            TrackItemRegular.withStartEnd(
                start = LocalTime.of(0, 0, 0),
                end = LocalTime.of(0, 5, 20),
                artist = "San Holo",
                track = "Show Me",
            ),
            TrackItemRegular.withStartEnd(
                start = LocalTime.of(0, 5, 20),
                end = LocalTime.of(0, 11, 24),
                artist = "Kasbo",
                track = "Over You (feat. Frida Sundemo)",
            ),
            TrackItemLast(
                startOffset = LocalTime.of(0, 11, 24),
                artist = "Kina",
                track = "I'm In Love With You",
            ),
        )
    }

    @Test
    fun valid_hasEndCuts() {
        // Assemble
        val rawInput = """
            00:00 03:30 San Holo - Show Me
            05:20 06:00 Kasbo - Over You (feat. Frida Sundemo)
            09:24 10:31 Kina - I'm In Love With You
        """.trimIndent()

        // Act
        val result: List<TrackItem> = nameParser.parse(rawInput)

        // Assert
        Assertions.assertThat(result).containsExactly(
            TrackItemRegular.withStartEnd(
                start = LocalTime.of(0, 0, 0),
                end = LocalTime.of(0, 3, 30),
                artist = "San Holo",
                track = "Show Me",
            ),
            TrackItemRegular.withStartEnd(
                start = LocalTime.of(0, 5, 20),
                end = LocalTime.of(0, 6, 0),
                artist = "Kasbo",
                track = "Over You (feat. Frida Sundemo)",
            ),
            TrackItemRegular.withStartEnd(
                start = LocalTime.of(0, 9, 24),
                end = LocalTime.of(0, 10, 31),
                artist = "Kina",
                track = "I'm In Love With You",
            ),
        )
    }

    @Test()
    fun invalid_hasSameTitle() {
        // Assemble
        val rawInput = """
0:00:00 Moo2 - Research1
0:00:10 Moo2 - Research1
        """.trimIndent()

        // Act
        // Assert
        Assertions.assertThatThrownBy {
            nameParser.parse(rawInput)
        }
    }
}