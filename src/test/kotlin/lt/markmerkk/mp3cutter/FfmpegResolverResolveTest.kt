package lt.markmerkk.mp3cutter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

class FfmpegResolverResolveTest {

    private fun buildResolver(
        osName: String = "Linux",
        pathEnv: String? = null,
        existingBinaries: Set<String>,
    ) = FfmpegResolver(
        osName = osName,
        pathEnv = pathEnv,
        isExecutable = { file -> existingBinaries.contains(file.path) },
    )

    @Test
    fun fromPath() {
        // Assemble
        val resolver = buildResolver(
            pathEnv = "/home/user/bin:/opt/ffmpeg/bin",
            existingBinaries = setOf("/opt/ffmpeg/bin/ffmpeg", "/opt/ffmpeg/bin/ffprobe"),
        )

        // Act
        val result = resolver.resolve(overrideRootPath = null)

        // Assert
        assertThat(result).isEqualTo(
            FfmpegBinaries(ffmpeg = File("/opt/ffmpeg/bin/ffmpeg"), ffprobe = File("/opt/ffmpeg/bin/ffprobe"))
        )
    }

    @Test
    fun linux_knownDirFallback() {
        // Assemble
        val resolver = buildResolver(
            osName = "Linux",
            pathEnv = "/home/user/bin",
            existingBinaries = setOf("/usr/bin/ffmpeg", "/usr/bin/ffprobe"),
        )

        // Act
        val result = resolver.resolve(overrideRootPath = null)

        // Assert
        assertThat(result.ffmpeg).isEqualTo(File("/usr/bin/ffmpeg"))
    }

    @Test
    fun mac_knownDirFallback() {
        // Assemble
        val resolver = buildResolver(
            osName = "Mac OS X",
            existingBinaries = setOf("/opt/homebrew/bin/ffmpeg", "/opt/homebrew/bin/ffprobe"),
        )

        // Act
        val result = resolver.resolve(overrideRootPath = null)

        // Assert
        assertThat(result.ffmpeg).isEqualTo(File("/opt/homebrew/bin/ffmpeg"))
    }

    @Test
    fun override() {
        // Assemble
        val resolver = buildResolver(
            pathEnv = "/usr/bin",
            existingBinaries = setOf(
                "/usr/bin/ffmpeg",
                "/usr/bin/ffprobe",
                "/custom/ffmpeg",
                "/custom/ffprobe",
            ),
        )

        // Act
        val result = resolver.resolve(overrideRootPath = "/custom")

        // Assert
        assertThat(result.ffmpeg).isEqualTo(File("/custom/ffmpeg"))
    }

    @Test
    fun override_missingBinaries() {
        // Assemble
        val resolver = buildResolver(
            pathEnv = "/usr/bin",
            existingBinaries = setOf("/usr/bin/ffmpeg", "/usr/bin/ffprobe"),
        )

        // Act + Assert
        assertThrows<IllegalStateException> {
            resolver.resolve(overrideRootPath = "/custom")
        }
    }

    @Test
    fun ffprobeMissing() {
        // Assemble
        val resolver = buildResolver(
            pathEnv = "/opt/ffmpeg/bin",
            existingBinaries = setOf("/opt/ffmpeg/bin/ffmpeg"),
        )

        // Act + Assert
        assertThrows<IllegalStateException> {
            resolver.resolve(overrideRootPath = null)
        }
    }
}
