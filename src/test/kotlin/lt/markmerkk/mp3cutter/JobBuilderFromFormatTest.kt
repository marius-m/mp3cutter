package lt.markmerkk.mp3cutter

import lt.markmerkk.mp3cutter.entities.ExportFormat
import lt.markmerkk.mp3cutter.entities.Gpu
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class JobBuilderFromFormatTest {

    /**
     * mp3 without GPU
     * Expected audio only builder
     */
    @Test
    fun mp3() {
        // Act
        val result = JobBuilder.fromFormat(ExportFormat.MP3, Gpu.NONE)

        // Assert
        assertThat(result).isInstanceOf(JobSpecMp3::class.java)
    }

    /**
     * mp3 with AMD GPU
     * Expected audio only builder, GPU ignored as there is no video to encode
     */
    @Test
    fun mp3_gpuIgnored() {
        // Act
        val result = JobBuilder.fromFormat(ExportFormat.MP3, Gpu.AMD)

        // Assert
        assertThat(result).isInstanceOf(JobSpecMp3::class.java)
    }

    /**
     * mp4 without GPU
     * Expected video builder where ffmpeg picks the encoder
     */
    @Test
    fun mp4() {
        // Act
        val result = JobBuilder.fromFormat(ExportFormat.MP4, Gpu.NONE)

        // Assert
        assertThat(result).isInstanceOf(JobSpecMp4::class.java)
    }

    /**
     * mp4 with AMD GPU
     * Expected video builder encoding on AMD
     */
    @Test
    fun mp4_gpuAmd() {
        // Act
        val result = JobBuilder.fromFormat(ExportFormat.MP4, Gpu.AMD)

        // Assert
        assertThat(result).isInstanceOf(JobSpecMp4Gpu::class.java)
        assertThat((result as JobSpecMp4Gpu).gpu).isEqualTo(Gpu.AMD)
    }

    /**
     * Invalid format (should be rejected earlier by the command)
     * Expected to throw
     */
    @Test
    fun invalid() {
        // Act + Assert
        assertThatThrownBy { JobBuilder.fromFormat(ExportFormat.INVALID, Gpu.NONE) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
