package lt.markmerkk.mp3cutter

import com.google.common.base.Preconditions
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.required
import lt.markmerkk.mp3cutter.entities.ExportFormat
import lt.markmerkk.mp3cutter.entities.Gpu
import lt.markmerkk.mp3cutter.entities.TrackItem
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Cuts input track into smaller files, using cut points from map file
 */
@OptIn(ExperimentalCli::class)
class CutCommand : Subcommand(
    name = "cut",
    actionDescription = "Cut input track into smaller files using a map file",
) {
    private val inputFilePathMap by option(
        ArgType.String,
        fullName = "map",
        shortName = "m",
        description = "Input map file, where to cut",
    ).required()
    private val inputFilePathTrack by option(
        ArgType.String,
        fullName = "track",
        shortName = "t",
        description = "Input mp3 track file which will be used for cutting",
    ).required()
    private val outputDirPath by option(
        ArgType.String,
        fullName = "output",
        shortName = "o",
        description = "Output directory where to put extracted files",
    ).required()
    private val exportFormatRaw by option(
        ArgType.String,
        fullName = "export-format",
        shortName = "ext",
        description = "Export format. Default 'mp3'",
    )
    private val overrideFfmpegRootPath by option(
        ArgType.String,
        fullName = "override-ffmpeg-path",
        shortName = "ffmpeg",
        description = "Directory containing ffmpeg and ffprobe. Default: resolved from PATH or common install locations",
    )
    private val gpuRaw by option(
        ArgType.String,
        fullName = "gpu",
        description = "GPU to encode video on ('none', 'amd'), only used for 'mp4'. Default 'none'",
    )
    private val _isDryRun by option(
        ArgType.Boolean,
        fullName = "dry-run",
        shortName = "d",
        description = "Prints out results from parsing input txt",
    )

    override fun execute() {
        val isDryRun = _isDryRun == true
        val tracks = parseTracks(inputFilePathMap = inputFilePathMap)
        printTracks(tracks)
        if (!isDryRun) {
            processTracks(
                overrideFfmpegRootPath = overrideFfmpegRootPath,
                exportFormat = parseExportFormatOrThrow(exportFormatRaw),
                gpu = parseGpuOrThrow(gpuRaw),
                tracks = tracks,
                inputFilePathTrack = inputFilePathTrack,
                outputDirPath = outputDirPath,
            )
        }
    }

    /**
     * @return valid [ExportFormat] or throw [IllegalArgumentException]
     */
    @Throws(IllegalArgumentException::class)
    private fun parseExportFormatOrThrow(exportFormatRaw: String?): ExportFormat {
        val exportFormat = ExportFormat.fromRaw(exportFormatRaw)
        if (!exportFormat.isValid()) {
            throw IllegalArgumentException("Invalid export format ($exportFormatRaw)")
        }
        return exportFormat
    }

    /**
     * @return [Gpu] ([Gpu.NONE] when not provided) or throw [IllegalArgumentException] for unknown value
     */
    @Throws(IllegalArgumentException::class)
    private fun parseGpuOrThrow(gpuRaw: String?): Gpu {
        val gpu = Gpu.fromRaw(gpuRaw)
        if (gpuRaw != null && !gpuRaw.equals(gpu.toRaw(), ignoreCase = true)) {
            throw IllegalArgumentException("Invalid GPU ($gpuRaw)")
        }
        return gpu
    }

    private fun parseTracks(inputFilePathMap: String): List<TrackItem> {
        val inputFileMap = File(inputFilePathMap)
        Preconditions.checkArgument(
            inputFileMap.exists(),
            "No mapping file provided (%s)"
                .format(inputFileMap.absolutePath),
        )
        val nameParser = NameParser()
        val rawText = FileUtils.readFileToString(inputFileMap, Charsets.UTF_8)
        val tracks = nameParser.parse(rawText)
        return tracks
    }

    private fun processTracks(
        overrideFfmpegRootPath: String?,
        exportFormat: ExportFormat,
        gpu: Gpu,
        tracks: List<TrackItem>,
        inputFilePathTrack: String,
        outputDirPath: String,
    ) {
        val inputFileTrack = File(inputFilePathTrack)
        val outputDir = File(outputDirPath)
        Preconditions.checkArgument(
            inputFileTrack.exists(),
            "No track file provided (%s)".format(inputFileTrack.absolutePath),
        )
        Preconditions.checkArgument(
            inputFileTrack.isFile,
            "Provided track should be a file (%s)".format(inputFileTrack.absolutePath),
        )
        Preconditions.checkArgument(
            outputDir.exists() && outputDir.isDirectory,
            "Output directory cannot be a file (%s)".format(inputFileTrack.absolutePath),
        )
        val cutter = Mp3Cutter(
            overrideFfmpegRootPath = overrideFfmpegRootPath,
            inputFile = inputFileTrack,
            outputDir = outputDir,
            exportFormat = exportFormat,
            gpu = gpu,
        )
        cutter.cut(tracks)
    }

    private fun printTracks(tracks: List<TrackItem>) {
        val sb = StringBuilder("Found tracks: ")
        tracks.forEach { track -> sb.append("- ${track}\n") }
        l.info(sb.toString())
    }

    companion object {
        private val l = LoggerFactory.getLogger(CutCommand::class.java)
    }
}
