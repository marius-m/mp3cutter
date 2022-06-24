package lt.markmerkk

import com.google.common.base.Preconditions
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import java.io.File
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
        val isDryRun by parser.option(
            ArgType.Boolean,
            fullName = "dry-run",
            shortName = "d",
            description = "Prints out results from parsing input txt",
        )
        parser.parse(args)

        val inputFileMap = File(inputFilePathMap)
        Preconditions.checkArgument(
            inputFileMap.exists(),
            "No mapping file provided (%s)"
                .format(inputFileMap.absolutePath),
        )
        val inputFileTrack = File(inputFilePathTrack)
        Preconditions.checkArgument(
            inputFileTrack.exists(),
            "No track file provided (%s)"
                .format(inputFileTrack.absolutePath),
        )
        val nameParser = NameParser()
        val rawText = FileUtils.readFileToString(inputFileMap, Charsets.UTF_8)
        val tracks = nameParser.parse(rawText)
        l.info("Tracks: ${tracks}")
    }

    companion object {
        private val l = LoggerFactory.getLogger(Main::class.java)
    }
}