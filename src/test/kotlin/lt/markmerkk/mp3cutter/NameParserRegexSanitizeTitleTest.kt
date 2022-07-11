package lt.markmerkk.mp3cutter

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class NameParserRegexSanitizeTitleTest {

    @Test
    fun regular() {
        // Assemble
        val rawInput = "San Holo"

        // Act
        val result = NameParser.sanitizeTitle(rawTitle = rawInput)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            "San Holo",
        )
    }

    @Test
    fun hasBrackets() {
        // Assemble
        val rawInput = "San Holo (Feat Test)"

        // Act
        val result = NameParser.sanitizeTitle(rawTitle = rawInput)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            "San Holo (Feat Test)",
        )
    }

    @Test
    fun ignore_hasSlash() {
        // Assemble
        val rawInput = "San/Holo"

        // Act
        val result = NameParser.sanitizeTitle(rawTitle = rawInput)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            "SanHolo",
        )
    }
}