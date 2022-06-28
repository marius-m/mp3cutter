package lt.markmerkk.mp3cutter

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalTime

internal class TimeUtilsParseTimeTest {

    @Test
    fun valid() {
        // Assemble
        val rawTime = "01:41:30"

        // Act
        val result = TimeUtils.parseTime(rawTime)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            LocalTime.of(1, 41, 30)
        )
    }

    @Test
    fun valid_short() {
        // Assemble
        val rawTime = "1:41:30"

        // Act
        val result = TimeUtils.parseTime(rawTime)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            LocalTime.of(1, 41, 30)
        )
    }

    @Test
    fun valid2() {
        // Assemble
        val rawTime = "41:30"

        // Act
        val result = TimeUtils.parseTime(rawTime)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            LocalTime.of(0, 41, 30)
        )
    }

    @Test
    fun valid2_short() {
        // Assemble
        val rawTime = "9:30"

        // Act
        val result = TimeUtils.parseTime(rawTime)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            LocalTime.of(0, 9, 30)
        )
    }
}