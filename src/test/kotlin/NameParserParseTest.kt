import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalTime
import kotlin.test.Ignore

class NameParserParseTest {

    private val nameParser = NameParser()

    //@Test
    fun validLong() {
        // Assemble
        val rawInput = TestUtils.loadContent("/sample_input_long.txt")

        // Act
        val result = nameParser.parse(rawInput)

        // Assert
        Assertions.assertThat(result).isEqualTo(true)
    }

    @Test
    fun valid() {
        // Assemble
        val rawInput = """
            0:00:00 San Holo - Show Me
            0:05:20 Kasbo - Over You (feat. Frida Sundemo)
            0:08:34 Raffaella - Bruce Willis
            0:11:24 Kina - I'm In Love With You
        """.trimIndent()

        // Act
        val result = nameParser.parse(rawInput)

        // Assert
        Assertions.assertThat(result).containsExactly(
            Song(
                start = LocalTime.of(0, 0, 0),
                end = LocalTime.of(0, 5, 20),
                artist = "San Holo",
                song = "Show Me",
            ),
            Song(
                start = LocalTime.of(0, 5, 20),
                end = LocalTime.of(0, 8, 34),
                artist = "Kasbo",
                song = "Over You (feat. Frida Sundemo)",
            ),
            Song(
                start = LocalTime.of(0, 8, 34),
                end = LocalTime.of(0, 11, 24),
                artist = "Raffaella",
                song = "Bruce Willis",
            ),
            Song(
                start = LocalTime.of(0, 11, 24),
                end = LocalTime.of(0, 11, 24),
                artist = "Kina",
                song = "I'm In Love With You",
            ),
        )
    }
}