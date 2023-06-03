package lt.markmerkk.mp3cutter.entities

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ExportFormatFromRawTest {
    @Test
    fun mp3() {
        // Assemble
        val inputRaw = "mp3"

        // Act
        val result = ExportFormat.fromRaw(inputRaw)

        // Assert
        assertThat(result).isEqualTo(ExportFormat.MP3)
    }

    @Test
    fun mp3_uppercase() {
        // Assemble
        val inputRaw = "MP3"

        // Act
        val result = ExportFormat.fromRaw(inputRaw)

        // Assert
        assertThat(result).isEqualTo(ExportFormat.MP3)
    }

    @Test
    fun mp4() {
        // Assemble
        val inputRaw = "mp4"

        // Act
        val result = ExportFormat.fromRaw(inputRaw)

        // Assert
        assertThat(result).isEqualTo(ExportFormat.MP4)
    }

    @Test
    fun empty() {
        // Assemble
        val inputRaw = ""

        // Act
        val result = ExportFormat.fromRaw(inputRaw)

        // Assert
        assertThat(result).isEqualTo(ExportFormat.INVALID)
    }

    @Test
    fun asNull() {
        // Assemble
        val inputRaw: String? = null

        // Act
        val result = ExportFormat.fromRaw(inputRaw)

        // Assert
        assertThat(result).isEqualTo(ExportFormat.INVALID)
    }
}
