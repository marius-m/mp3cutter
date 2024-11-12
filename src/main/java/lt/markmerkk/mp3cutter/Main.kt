package lt.markmerkk.mp3cutter

import com.google.common.base.Preconditions
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import lt.markmerkk.mp3cutter.entities.ExportFormat
import lt.markmerkk.mp3cutter.entities.TrackItem
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import java.io.File
import java.lang.IllegalArgumentException
import java.util.logging.LogManager

fun main(args: Array<String>) {
    LogManager.getLogManager().reset()
    SLF4JBridgeHandler.install()

    Main().main(args)
}

class Main {
    fun main(args: Array<String>) {
        val parser = ArgParser("mp3cutter")
        val inputFilePathMap by parser.option(
            ArgType.String,
            fullName = "map",
            shortName = "m",
            description = "Input map file, where to cut",
        ).required()
        val inputFilePathTrack by parser.option(
            ArgType.String,
            fullName = "track",
            shortName = "t",
            description = "Input mp3 track file which will be used for cutting",
        ).required()
        val outputDirPath by parser.option(
            ArgType.String,
            fullName = "output",
            shortName = "o",
            description = "Output directory where to put extracted files",
        ).required()
        val exportFormatRaw by parser.option(
            ArgType.String,
            fullName = "export-format",
            shortName = "ext",
            description = "Export format. Default 'mp3'",
        )
        val overrideFfmpegRootPath by parser.option(
            ArgType.String,
            fullName = "override-ffmpeg-path",
            shortName = "ffmpeg",
            description = "Overrides the default ffmpeg path. Default '${Mp3Cutter.DEFAULT_FFMPEG_PATH}'",
        )
        val _isDryRun by parser.option(
            ArgType.Boolean,
            fullName = "dry-run",
            shortName = "d",
            description = "Prints out results from parsing input txt",
        )
        val isDryRun = _isDryRun == true
        parser.parse(args)
        val tracks = parseTracks(inputFilePathMap = inputFilePathMap)
        printTracks(tracks)
        if (!isDryRun) {
            processTracks(
                overrideFfmpegRootPath = overrideFfmpegRootPath,
                exportFormat = parseExportFormatOrThrow(exportFormatRaw),
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
        )
        cutter.cut(tracks)
    }

    private fun printTracks(tracks: List<TrackItem>) {
        val sb = StringBuilder("Found tracks: ")
        tracks.forEach { track -> sb.append("- ${track}\n") }
        l.info(sb.toString())
    }

    companion object {
        private val l = LoggerFactory.getLogger(Main::class.java)
    }
}