package lt.markmerkk.mp3cutter.entities

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GpuFromRawTest {

    /**
     * Value as passed in with '--gpu amd'
     */
    @Test
    fun amd() {
        // Assemble
        val inputRaw = "amd"

        // Act
        val result = Gpu.fromRaw(inputRaw)

        // Assert
        assertThat(result).isEqualTo(Gpu.AMD)
    }

    /**
     * Case is ignored, 'AMD' and 'Amd' are expected to resolve the same as 'amd'
     */
    @Test
    fun amd_mixedCase() {
        // Act
        val resultUppercase = Gpu.fromRaw("AMD")
        val resultCapitalized = Gpu.fromRaw("Amd")

        // Assert
        assertThat(resultUppercase).isEqualTo(Gpu.AMD)
        assertThat(resultCapitalized).isEqualTo(Gpu.AMD)
    }

    /**
     * '--gpu' not passed in (null) or empty -> no GPU, ffmpeg picks the encoder itself
     */
    @Test
    fun noValue() {
        // Act
        val resultNull = Gpu.fromRaw(null)
        val resultEmpty = Gpu.fromRaw("")
        val resultBlank = Gpu.fromRaw("  ")

        // Assert
        assertThat(resultNull).isEqualTo(Gpu.NONE)
        assertThat(resultEmpty).isEqualTo(Gpu.NONE)
        assertThat(resultBlank).isEqualTo(Gpu.NONE)
    }

    /**
     * Not supported GPU (yet) -> falls back to no GPU instead of failing
     */
    @Test
    fun unknown() {
        // Act
        val result = Gpu.fromRaw("nvidia")

        // Assert
        assertThat(result).isEqualTo(Gpu.NONE)
    }

    /**
     * No GPU -> nothing is added to ffmpeg command
     */
    @Test
    fun none_args() {
        // Act
        val result = Gpu.NONE

        // Assert
        assertThat(result.globalArgs).isEmpty()
        assertThat(result.videoFilterSuffix).isEmpty()
        assertThat(result.videoCodec).isNull()
    }

    /**
     * AMD -> exact arguments, verified to work with ffmpeg 6.1 on AMD RX 6600:
     * - '-vaapi_device /dev/dri/renderD128' before input, opens GPU through VAAPI
     * - 'format=nv12,hwupload' at the end of video filter, uploads CPU frames to GPU memory
     * - 'h264_vaapi' as video encoder
     */
    @Test
    fun amd_args() {
        // Act
        val result = Gpu.AMD

        // Assert
        assertThat(result.globalArgs).containsExactly("-vaapi_device", "/dev/dri/renderD128")
        assertThat(result.videoFilterSuffix).isEqualTo("format=nv12,hwupload")
        assertThat(result.videoCodec).isEqualTo("h264_vaapi")
    }
}
