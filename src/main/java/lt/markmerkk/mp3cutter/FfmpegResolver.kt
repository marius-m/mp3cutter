package lt.markmerkk.mp3cutter

import org.slf4j.LoggerFactory
import java.io.File

/**
 * Resolves ffmpeg / ffprobe binaries on the current system
 * Lookup order:
 * - Explicitly provided directory (no fallback if binaries are missing there)
 * - Directories from 'PATH' environment variable
 * - Well known install locations for the OS
 */
class FfmpegResolver(
    private val osName: String = System.getProperty("os.name"),
    private val pathEnv: String? = System.getenv("PATH"),
    private val isExecutable: (File) -> Boolean = { it.isFile && it.canExecute() },
) {

    private val isWindows = osName.startsWith("Windows", ignoreCase = true)
    private val isMac = osName.startsWith("Mac", ignoreCase = true)
    private val binaryExtension = if (isWindows) ".exe" else ""

    /**
     * @return ffmpeg binaries or throw [IllegalStateException] if they cannot be found
     */
    @Throws(IllegalStateException::class)
    fun resolve(overrideRootPath: String?): FfmpegBinaries {
        if (overrideRootPath != null) {
            return binariesInDir(File(overrideRootPath))
                ?: throw IllegalStateException(
                    "No ffmpeg / ffprobe found in provided path (%s)".format(overrideRootPath)
                )
        }
        val searchDirs = (pathDirs() + knownDirs())
            .distinct()
        val binaries = searchDirs
            .firstNotNullOfOrNull { dir -> binariesInDir(dir) }
            ?: throw IllegalStateException(
                "No ffmpeg / ffprobe found. Install ffmpeg or provide its directory with '--override-ffmpeg-path'. Searched: %s"
                    .format(searchDirs.joinToString { it.path })
            )
        l.debug("Resolved ffmpeg binaries: {}", binaries)
        return binaries
    }

    private fun binariesInDir(dir: File): FfmpegBinaries? {
        val ffmpeg = File(dir, "ffmpeg$binaryExtension")
        val ffprobe = File(dir, "ffprobe$binaryExtension")
        if (isExecutable(ffmpeg) && isExecutable(ffprobe)) {
            return FfmpegBinaries(ffmpeg = ffmpeg, ffprobe = ffprobe)
        }
        return null
    }

    private fun pathDirs(): List<File> {
        val separator = if (isWindows) ";" else ":"
        return (pathEnv ?: "")
            .split(separator)
            .filter { it.isNotBlank() }
            .map { File(it) }
    }

    private fun knownDirs(): List<File> {
        val dirs = when {
            isWindows -> emptyList()
            isMac -> listOf("/opt/homebrew/bin", "/usr/local/bin", "/opt/local/bin")
            else -> listOf("/usr/bin", "/usr/local/bin")
        }
        return dirs.map { File(it) }
    }

    companion object {
        private val l = LoggerFactory.getLogger(FfmpegResolver::class.java)
    }
}

data class FfmpegBinaries(
    val ffmpeg: File,
    val ffprobe: File,
)
