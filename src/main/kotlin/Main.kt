import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import org.slf4j.bridge.SLF4JBridgeHandler
import java.io.File
import java.util.logging.LogManager

fun main(args: Array<String>) {
    LogManager.getLogManager().reset()
    SLF4JBridgeHandler.install()

    val parser = ArgParser("mp3cut")
    val inputFileName by parser.option(
        ArgType.String,
        shortName = "i",
        description = "Input mp3 file to cut",
    ).required()
    parser.parse(args)

    val inputFile = File(inputFileName)
    if (!inputFile.exists()) {
        throw IllegalArgumentException("Invalid input file (%s)".format(inputFile.absolutePath))
    }
}