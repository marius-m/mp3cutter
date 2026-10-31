package lt.markmerkk.mp3cutter

import TestUtils
import lt.markmerkk.mp3cutter.entities.SilenceInterval
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration

class SilenceLogParserParseTest {

    private val parser = SilenceLogParser()
    private val totalDuration = Duration.ofMillis(5835250)

    @Test
    fun empty() {
        // Assemble
        val rawInput = ""

        // Act
        val result = parser.parse(rawInput, totalDuration)

        // Assert
        Assertions.assertThat(result).isEmpty()
    }

    @Test
    fun startWithProgressGarbage() {
        // Assemble
        val rawInput = """
            [silencedetect @ 0x65328917e540] silence_start: 259.042trate=N/A speed=20.5x
            [silencedetect @ 0x65328917e540] silence_end: 261.156 | silence_duration: 2.11454
        """.trimIndent()

        // Act
        val result = parser.parse(rawInput, totalDuration)

        // Assert
        Assertions.assertThat(result).containsExactly(
            SilenceInterval(
                start = Duration.ofMillis(259042),
                end = Duration.ofMillis(261156),
            ),
        )
    }

    @Test
    fun unclosedStart() {
        // Assemble
        val rawInput = """
            [silencedetect @ 0x65328917e540] silence_start: 5833.69itrate=N/A speed=20.4x
        """.trimIndent()

        // Act
        val result = parser.parse(rawInput, totalDuration)

        // Assert
        Assertions.assertThat(result).containsExactly(
            SilenceInterval(
                start = Duration.ofMillis(5833690),
                end = totalDuration,
            ),
        )
    }

    @Test
    fun fullLog() {
        // Assemble
        val rawInput = TestUtils.loadContent("/dark-jazz2-silence.txt")

        // Act
        val result = parser.parse(rawInput, totalDuration)

        // Assert
        Assertions.assertThat(result).hasSize(23)
        Assertions.assertThat(result.first()).isEqualTo(
            SilenceInterval(
                start = Duration.ofMillis(7),
                end = Duration.ofMillis(532),
            )
        )
        Assertions.assertThat(result.last()).isEqualTo(
            SilenceInterval(
                start = Duration.ofMillis(5833690),
                end = Duration.ofMillis(5835240),
            )
        )
    }
}
